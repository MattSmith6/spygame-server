package com.github.spygameserver.email;

public class ResetPasswordEmailCreator extends EmailCreator {

	private static final String SUBJECT_MESSAGE = "Spy Game - Password Reset";

	private static final String URL = "http://137.184.180.66/resetPassword.html";

	private static final String HTML_BODY_FORMAT = "<p>Click the following button to reset your password for your Spy Game account:</p>" +
			"<button onclick=\"post('%s', { email: '%s', token: '%s' })\">Reset password</button>";

	private final String verificationToken;

	public ResetPasswordEmailCreator(String playerEmail, String verificationToken) {
		super(playerEmail);

		this.verificationToken = verificationToken;
	}

	@Override
	protected String getSubjectMessage() {
		return SUBJECT_MESSAGE;
	}

	@Override
	protected String getHtmlBodyMessage() {
		return String.format(HTML_BODY_FORMAT, URL, verificationToken, getEncodedPlayerEmail());
	}

}
