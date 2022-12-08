package com.github.spygameserver.email;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;

import javax.activation.CommandMap;
import javax.activation.MailcapCommandMap;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public abstract class EmailCreator {

	private static final String EMAIL_FORMAT = "Hey %s,\n\n" +
			"%s";

	private static final String RESOURCE_NAME = "gmail_credentials.properties";

	private final String serverEmail;
	private final String playerEmail;
	private final String password;

	protected EmailCreator(String playerEmail) {
		Properties gmailCredentials = getGmailCredentials();

		this.serverEmail = gmailCredentials.getProperty("email");
		this.playerEmail = playerEmail;
		this.password = gmailCredentials.getProperty("password");

		initializeMailCap();
	}

	private void initializeMailCap() {
		// https://stackoverflow.com/questions/21856211/javax-activation-unsupporteddatatypeexception-no-object-dch-for-mime-type-multi
		MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
		mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
		mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
		mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
		mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
		mc.addMailcap("message/rfc822;; x-java-content- handler=com.sun.mail.handlers.message_rfc822");
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
		return getEncodedString(getPlayerEmail());
	}

	protected String getEncodedString(String string) {
		return URLEncoder.encode(string, StandardCharsets.UTF_8);
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
		htmlEmail.setTextMsg(getMessage());

		Thread.currentThread().setContextClassLoader( getClass().getClassLoader() );
		htmlEmail.send();
	}

	private String getMessage() {
		return String.format(EMAIL_FORMAT, getPlayerFirstName(), getMessageBody());
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

	protected abstract String getMessageBody();

}
