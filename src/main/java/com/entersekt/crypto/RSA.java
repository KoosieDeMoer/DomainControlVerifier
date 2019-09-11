package com.entersekt.crypto;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.input.ReaderInputStream;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.bouncycastle.jcajce.provider.asymmetric.x509.CertificateFactory;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RSA {

	private static final char[] KEYSTORE_PASSWORD = "zO1BYeoF0wIvAZ7YZvP29m3D3JVaVP7npDdGOQx3".toCharArray();
	private static final String KEY_STORE_FILENAME = "domain.jks";
	private KeyStore keyStore;

	private static final Logger log = LoggerFactory.getLogger(RSA.class);

	public static void main(String[] args) throws Exception {
		Security.insertProviderAt(new BouncyCastleProvider(), 1);

		(new RSA()).convertDomainCredentialsToJKS();

	}

	private void convertDomainCredentialsToJKS() throws Exception {

		PublicKey publicKey = getPublicKey("public.pem");

		X509Certificate cert = getCert("domain.crt");

		PrivateKey privateKey = getPrivateKey("key.pem");

		keyStore = KeyStore.getInstance("PKCS12");

		FileInputStream fileInputStream = null;
		try {
			fileInputStream = new FileInputStream(KEY_STORE_FILENAME);
		} catch (FileNotFoundException e) {
			// this is fine
			log.info("Creating the keystore for first time");
		}
		keyStore.load(fileInputStream, KEYSTORE_PASSWORD);
	}

	private static String getKey(String filename) throws IOException {
		// Read key from file
		String strKeyPEM = "";
		BufferedReader br = new BufferedReader(new FileReader(filename));
		String line;
		while ((line = br.readLine()) != null) {
			strKeyPEM += line + "\n";
		}
		br.close();
		return strKeyPEM;
	}

	public static RSAPrivateKey getPrivateKey(String filename) throws IOException, GeneralSecurityException {
		String privateKeyPEM = getKey(filename);
		return getPrivateKeyFromString(privateKeyPEM);
	}

	public static RSAPrivateKey getPrivateKeyFromString(String key) throws IOException, GeneralSecurityException {
		String privateKeyPEM = key;
		privateKeyPEM = privateKeyPEM.replace("-----BEGIN PRIVATE KEY-----\n", "");
		privateKeyPEM = privateKeyPEM.replace("-----END PRIVATE KEY-----", "");
		byte[] encoded = Base64.decodeBase64(privateKeyPEM);
		KeyFactory kf = KeyFactory.getInstance("RSA");
		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
		RSAPrivateKey privKey = (RSAPrivateKey) kf.generatePrivate(keySpec);
		return privKey;
	}

	public static RSAPublicKey getPublicKey(String filename) throws IOException, GeneralSecurityException {
		String publicKeyPEM = getKey(filename);
		return getPublicKeyFromString(publicKeyPEM);
	}

	public static RSAPublicKey getPublicKeyFromString(String key) throws IOException, GeneralSecurityException {
		String publicKeyPEM = key;
		publicKeyPEM = publicKeyPEM.replace("-----BEGIN PUBLIC KEY-----\n", "");
		publicKeyPEM = publicKeyPEM.replace("-----END PUBLIC KEY-----", "");
		byte[] encoded = Base64.decodeBase64(publicKeyPEM);
		KeyFactory kf = KeyFactory.getInstance("RSA");
		RSAPublicKey pubKey = (RSAPublicKey) kf.generatePublic(new X509EncodedKeySpec(encoded));
		return pubKey;
	}

	public X509Certificate getCert(String filename) throws Exception {
		String certPEM = getKey(filename);
		return convertToX509Certificate(certPEM);
	}

	private X509Certificate convertToX509Certificate(String pem) throws Exception {
		CertificateFactory certificateFactory = new CertificateFactory();
		return (X509Certificate) certificateFactory.engineGenerateCertificate(new ReaderInputStream(new StringReader(
				pem), "UTF-8"));
	}

	public String registerSignedCert(String pem) throws Exception {
		X509Certificate cert = convertToX509Certificate(pem);
		String alias = extractAlias(cert);

		X509Certificate[] chain = new X509Certificate[2];
		chain[0] = cert;
		chain[1] = (X509Certificate) null; // todo the intermediate cert

		Key principalPrivateKey = getPrivateKey("@@@"); //
		keyStore.setKeyEntry(alias, principalPrivateKey, KEYSTORE_PASSWORD, chain);
		putKeyStore();

		return alias;

	}

	public String extractAlias(X509Certificate cert) throws CertificateEncodingException {
		return new JcaX509CertificateHolder(cert).getSubject().getRDNs(BCStyle.CN)[0].getFirst().getValue().toString();
	}

	public void putKeyStore() throws Exception {
		FileOutputStream fileOutputStream = new FileOutputStream(KEY_STORE_FILENAME);
		keyStore.store(fileOutputStream, KEYSTORE_PASSWORD);
		fileOutputStream.flush();
		fileOutputStream.close();
	}

}
