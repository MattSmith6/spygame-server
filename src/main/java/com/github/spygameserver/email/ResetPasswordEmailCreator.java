package com.github.spygameserver.email;

public class ResetPasswordEmailCreator extends EmailCreator {

	private static final String SUBJECT_MESSAGE = "Spy Game - Password Reset";

	private static final String URL_FORMAT = "http://137.184.180.66/resetPassword.html?token=%s&email=%s";

	private static final String HTML_BODY_FORMAT = "<p>Click this <a href=\"%s\">link</a> to reset your password for your Spy Game account.</p>";

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
		String url = String.format(URL_FORMAT, verificationToken, getEncodedPlayerEmail());
		return String.format(HTML_BODY_FORMAT, url);
	}

}
