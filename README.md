# DomainControlVerifier
Get LetsEncrypt Certificate without Certbot

This service should be used when your environment does not fit the certbot model, ie keystore is not in a standard location, certbot not available, etc.

The service should be used along with https://gethttpsforfree.com, a well-documented wrapper site for LetsEncrypt (part of Step 4. Verify Ownership - Option 2 File-based). This service MUST run as HTTP on port 80 in the domain root (or as HTTPS on port 443 with HTTP:80 redirecting here)

Protect the password - anyone who has it while this service is running can generate valid certs for the domain.
