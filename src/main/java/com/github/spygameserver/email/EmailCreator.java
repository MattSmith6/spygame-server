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
	private static final String HTML_EMAIL_FORMAT = "<html><head>" +
			"\n" +
			"<script type=\"text/javascript\">\n" +
			"    // Source code for this method can be attributed to: https://stackoverflow.com/questions/133925/javascript-post-request-like-a-form-submit,\n" +
			"    // this method was modified to meet the needs of the project while not having to create common code\n" +
			"    /**\n" +
			"     * sends a request to the specified url from a form. this will change the window location.\n" +
			"     * @param {object} params the parameters to add to the url\n" +
			"     */\n" +
			"    function post(action, params) {\n" +
			"\n" +
			"        // The rest of this code assumes you are not using a library.\n" +
			"        // It can be made less verbose if you use one.\n" +
			"        const form = document.createElement('form');\n" +
			"\n" +
			"        form.method = 'post';\n" +
			"        form.action = action;\n" +
			"\n" +
			"        for (const key in params) {\n" +
			"            if (params.hasOwnProperty(key)) {\n" +
			"                const hiddenField = document.createElement('input');\n" +
			"                hiddenField.type = 'hidden';\n" +
			"                hiddenField.name = key;\n" +
			"                hiddenField.value = params[key];\n" +
			"\n" +
			"                form.appendChild(hiddenField);\n" +
			"            }\n" +
			"        }\n" +
			"\n" +
			"        document.body.appendChild(form);\n" +
			"        form.submit();\n" +
			"    }\n" +
			"</script></head>" +
			"<body><h2>Hey there %s,</h2><br>" +
			"%s<br><br>" +
			"<p>This is an automated message sent from the Spy Game team. No responses to this email will be seen by humans.</p></body></html>";

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
