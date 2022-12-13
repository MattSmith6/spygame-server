package com.github.spygameserver.auth.website;

import com.github.spygameserver.auth.website.email.EmailVerifier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Checks that valid domain names (csun.edu and my.csun.edu) are accepted as valid.
 * Also checks that invalid domain names (not the above) are not valid.
 */
public class EmailVerifierTest {

    private static final String INVALID_EMAIL = "bob@gmail.com";
    private static final String VALID_EMAIL_1 = "bob@my.csun.edu";
    private static final String VALID_EMAIL_2 = "bob@csun.edu";

    @Test
    public void testValidEmailAddresses() {
        Assertions.assertTrue(EmailVerifier.isEmailFromCSUN(VALID_EMAIL_1));
        Assertions.assertTrue(EmailVerifier.isEmailFromCSUN(VALID_EMAIL_2));
    }

    @Test
    public void testInvalidEmailAddresses() {
        Assertions.assertFalse(EmailVerifier.isEmailFromCSUN(INVALID_EMAIL));
    }

}
