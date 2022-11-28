package com.github.spygameserver.email;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class VerifyOrDisableEmailCreator extends EmailCreator {

	private static final String SUBJECT_MESSAGE = "Spy Game - Verify your Account";

	private static final String URL_VERIFY_FORMAT = "http://137.184.180.66/account/email/verify/?token=%s&email=%s";
	private static final String URL_DISABLE_FORMAT = "http://137.184.180.66/account/email/disable/?token=%s&email=%s";

	private static final String HTML_BODY_FORMAT = "<p>Click this <a href=\"%s\">link</a> to verify your email to " +
			"start playing Spy Game.</p>" +
			"<br><p>Don't recognize this email? Click <a href=\"%s\">here</a> to disable your account for Spy Game." +
			" Don't worry, you'll always be able to re-register an account if you do this in error.</p>";

	private final String verificationToken;

	public VerifyOrDisableEmailCreator(String playerEmail, String verificationToken) {
		super(playerEmail);

		this.verificationToken = verificationToken;
	}

	@Override
	protected String getSubjectMessage() {
		return SUBJECT_MESSAGE;
	}

	@Override
	protected String getHtmlBodyMessage() {
		String verifyUrl = String.format(URL_VERIFY_FORMAT, verificationToken, getEncodedPlayerEmail());
		String disableUrl = String.format(URL_DISABLE_FORMAT, verificationToken, getEncodedPlayerEmail());

		return String.format(HTML_BODY_FORMAT, verifyUrl, disableUrl);
	}

}
