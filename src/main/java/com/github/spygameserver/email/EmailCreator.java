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

	// The format for these email, "Hey <name>, \n\n <message body>"
	private static final String EMAIL_FORMAT = "Hey %s,\n\n" +
			"%s";

	// The resource that holds the gmail credentials used to email players: email and password
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
		// The following resource was used to set the
		// https://stackoverflow.com/questions/21856211/javax-activation-unsupporteddatatypeexception-no-object-dch-for-mime-type-multi
		MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
		mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
		mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
		mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
		mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
		mc.addMailcap("message/rfc822;; x-java-content- handler=com.sun.mail.handlers.message_rfc822");
	}

	/**
	 *
	 * @return
	 */
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

	// Escape necessary characters (like the @ symbol) by encoding a string to UTF-8
	protected String getEncodedString(String string) {
		return URLEncoder.encode(string, StandardCharsets.UTF_8);
	}

	// Heavily pulled from documentation at: https://commons.apache.org/proper/commons-email/userguide.html
	public void sendNewEmail() throws EmailException {
		HtmlEmail htmlEmail = new HtmlEmail();

		// Setup google host, port, authentication, and SSL
		htmlEmail.setHostName("smtp.googlemail.com");
		htmlEmail.setSmtpPort(465);
		htmlEmail.setAuthenticator(new DefaultAuthenticator(serverEmail, password));
		htmlEmail.setSSLOnConnect(true);

		// Set the from as the server email, the to as the player email
		htmlEmail.setFrom(serverEmail);
		htmlEmail.addTo(playerEmail);

		// Set the subject message and body of the email to be the information defined in the subclass
		htmlEmail.setSubject(getSubjectMessage());
		htmlEmail.setTextMsg(getMessage());

		// Fix errors with SMTP, by setting the context class loader
		Thread.currentThread().setContextClassLoader(getClass().getClassLoader());

		// Send the email
		htmlEmail.send();
	}

	private String getMessage() {
		return String.format(EMAIL_FORMAT, getPlayerFirstName(), getMessageBody());
	}

	/**
	 * Gets the first name of a player (for CSUN emails, this is separated by the first . character).
	 * If there is no . character in the email, return the name of the email address (before the @ character).
	 * @return the first name, or email of the character if the first name is not accessible
	 */
	protected String getPlayerFirstName() {
		// This should never occur for a CSUN email address, but this might occur for a test email
		if (!playerEmail.contains(".")) {
			return playerEmail.split("@")[0];
		}

		String lowercaseFirstName = playerEmail.split("\\.")[0];
		return Character.toUpperCase(lowercaseFirstName.charAt(0)) + lowercaseFirstName.substring(1);
	}

	/**
	 * Gets the subject of the email for the specific subclass
	 * @return the subject of the email for the specific subclass
	 */
	protected abstract String getSubjectMessage();

	/**
	 * Gets the body of the email, outside of the hello header, for the specific subclass
	 * @return the body of the email, outside of the hello header, for the specific subclass
	 */
	protected abstract String getMessageBody();

}
