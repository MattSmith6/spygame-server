package com.github.spygameserver.email;

import com.github.spygameserver.util.StringUtils;

public class ResetPasswordEmailCreator extends EmailCreator {

	private static final String SUBJECT_MESSAGE = "Spy Game - Password Reset";

	private static final String URL = "http://137.184.180.66/resetPassword.html";

	private static final String HTML_BODY_FORMAT = "Use the following link to reset your password: %s?%s";

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
	protected String getMessageBody() {
		String getParameters = StringUtils.join('&', "email=" + getEncodedPlayerEmail(), "token=" + getEncodedString(verificationToken));
		return String.format(HTML_BODY_FORMAT, URL, getParameters);
	}

}
