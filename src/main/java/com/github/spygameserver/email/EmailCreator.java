package com.github.spygameserver.email;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public abstract class EmailCreator {
	private static final String HTML_EMAIL_FORMAT = "<html><h2>Hey there %s,</h2><br>" +
			"%s<br><br>" +
			"<p>This is an automated message sent from the Spy Game team. No responses to this email will be seen by humans.</p></html>";

	private static final String RESOURCE_NAME = "gmail_credentials.properties";

	private final String serverEmail;
	private final String playerEmail;
	private final String password;

	protected EmailCreator(String playerEmail) {
		Properties gmailCredentials = getGmailCredentials();

		this.serverEmail = gmailCredentials.getProperty("email");
		this.playerEmail = playerEmail;
		this.password = gmailCredentials.getProperty("password");
	}

	private Properties getGmailCredentials() {
		Properties properties = new Properties();

		try {
			InputStream inputStream = getClass().getClassLoader().getResourceAsStream(RESOURCE_NAME);
			if (inputStream != null) {
				properties.load(inputStream);
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		return properties;
	}

	protected String getPlayerEmail() {
		return playerEmail;
	}

	protected String getEncodedPlayerEmail() {
		return URLEncoder.encode(getPlayerEmail(), StandardCharsets.UTF_8);
	}

	// Heavily pulled from documentation at: https://commons.apache.org/proper/commons-email/userguide.html
	public void sendNewEmail() throws EmailException {
		HtmlEmail htmlEmail = new HtmlEmail();

		htmlEmail.setHostName("smtp.googlemail.com");
		htmlEmail.setSmtpPort(465);

		htmlEmail.setAuthenticator(new DefaultAuthenticator(serverEmail, password));
		htmlEmail.setSSLOnConnect(true);

		htmlEmail.setFrom(serverEmail);
		htmlEmail.addTo(playerEmail);

		htmlEmail.setSubject(getSubjectMessage());
		htmlEmail.setHtmlMsg(getHtmlMessage());

		// This should never occur, as gmail does support HTML messages
		htmlEmail.setTextMsg("Your email client does not support HTML messages");
		htmlEmail.send();
	}

	private String getHtmlMessage() {
		return String.format(HTML_EMAIL_FORMAT, getPlayerFirstName(), getHtmlBodyMessage());
	}

	protected String getPlayerFirstName() {
		// This should never occur for a CSUN email address, but this might occur for a test email
		if (!playerEmail.contains(".")) {
			return playerEmail.split("@")[0];
		}

		String lowercaseFirstName = playerEmail.split("\\.")[0];
		return Character.toUpperCase(lowercaseFirstName.charAt(0)) + lowercaseFirstName.substring(1);
	}

	protected abstract String getSubjectMessage();

	protected abstract String getHtmlBodyMessage();

}
