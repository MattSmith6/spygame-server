package com.github.spygameserver.player.account;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * A quick test to make sure that the AccountVerificationStatus is correctly being converted to the SQL enum type.
 *
 * This test will need to be dynamically updated if the AccountVerificationStatus values ever change order, or there is an addition.
 */
public class AccountVerificationStatusTest {

    private static final String CURRENT_VERIFICATION_STATUS_VALUES = "('AWAITING_VERIFICATION', 'DISABLED', 'VERIFIED')";

    @Test
    public void testSQLStringifiedEnum() {
        String realValue = AccountVerificationStatus.toSQLStringifiedEnum();
        Assertions.assertEquals(CURRENT_VERIFICATION_STATUS_VALUES, realValue);
    }

}
