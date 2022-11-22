package com.github.spygameserver.auth.website;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class GoogleEmailVerifierTest {

    private static final String INVALID_EMAIL = "bob@gmail.com";
    private static final String VALID_EMAIL_1 = "bob@my.csun.edu";
    private static final String VALID_EMAIL_2 = "bob@csun.edu";

    @Test
    public void testValidEmailAddresses() {
        Assertions.assertTrue(GoogleEmailVerifier.isEmailFromCSUN(VALID_EMAIL_1));
        Assertions.assertTrue(GoogleEmailVerifier.isEmailFromCSUN(VALID_EMAIL_2));
    }

    @Test
    public void testInvalidEmailAddresses() {
        Assertions.assertFalse(GoogleEmailVerifier.isEmailFromCSUN(INVALID_EMAIL));
    }

}
