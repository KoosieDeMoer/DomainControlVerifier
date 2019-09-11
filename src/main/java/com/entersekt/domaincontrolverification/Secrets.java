package com.entersekt.domaincontrolverification;

import java.io.Serializable;

public class Secrets implements Serializable {

	public String verificationFileContents;

	public String password;

	public Secrets(String verificationFileContents, String password) {
		super();
		this.verificationFileContents = verificationFileContents;
		this.password = password;
	}

	public Secrets() {
		super();
	}

	private static final long serialVersionUID = 1L;

}
