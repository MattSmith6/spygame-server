package com.github.spygameserver.email;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class VerifyOrDisableEmailCreator extends EmailCreator {

	private static final String SUBJECT_MESSAGE = "Spy Game - Verify your Account";

	private static final String URL_VERIFY = "http://137.184.180.66/account/email/verify/";
	private static final String URL_DISABLE = "http://137.184.180.66/account/email/disable/";

	private static final String HTML_BODY_FORMAT = "<p>Click the following button to verify your email to " +
			"start playing Spy Game.</p>" +
			"<button onclick=\"post('%1$s', { email: '%2$s', token: '%3$s' })\">Verify account</button>" +
			"<br><p>Don't recognize this email? Click below to disable your account for Spy Game." +
			" Don't worry, you'll always be able to re-register an account if you do this in error.</p>" +
			"<button onclick=\"post('%4$s', { email: '%2$s', token: '%3$s' })\">Disable account</button>";

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
		return String.format(HTML_BODY_FORMAT, URL_VERIFY, getEncodedPlayerEmail(), verificationToken, URL_DISABLE);
	}

}
