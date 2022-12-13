package com.github.spygameserver.email;

import com.github.spygameserver.util.StringUtils;

/**
 * Sends an email to the player with links to verify or disable their account.
 */
public class VerifyOrDisableEmailCreator extends EmailCreator {

	private static final String SUBJECT_MESSAGE = "Spy Game - Verify your Account";

	private static final String URL_VERIFY = "http://137.184.180.66/account/email/verify";
	private static final String URL_DISABLE = "http://137.184.180.66/account/email/disable";

	private static final String BODY_FORMAT = "Here's your verification link: %1$s?%2$s\n\n" +
			"Don't recognize this email? Here's a link to disable this Spy Game account: %3$s?%2$s\n" +
			"(Don't worry, you'll always be able to re-register an account if you do this in error)";

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
	protected String getMessageBody() {
		String getParameters = StringUtils.join('&', "email=" + getEncodedPlayerEmail(), "token=" + getEncodedString(verificationToken));
		return String.format(BODY_FORMAT, URL_VERIFY, getParameters, URL_DISABLE);
	}

}
