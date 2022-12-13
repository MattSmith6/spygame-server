package com.github.spygameserver.email;

import com.github.spygameserver.util.StringUtils;

/**
 * Sends an email to the player with a link to reset their password. The link redirects to the resetPassword.html,
 * which validates the normal password and the confirmed password match before sending a post request to the
 * account/reset/doReset path to update the player's account credentials. More information can be found in the
 *
 */
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
		// The GET HTTP request parameters for this URL: email=<encoded email>&token=<encoded token> parameters
		String getParameters = StringUtils.join('&', "email=" + getEncodedPlayerEmail(),
				"token=" + getEncodedString(verificationToken));

		// Return the formatted body of the email, which includes the URL?getParameters
		return String.format(HTML_BODY_FORMAT, URL, getParameters);
	}

}
