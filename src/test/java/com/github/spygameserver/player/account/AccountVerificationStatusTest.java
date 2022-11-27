package com.github.spygameserver.player.account;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AccountVerificationStatusTest {

    @Test
    public void testSQLStringifiedEnum() {
        String realValue = AccountVerificationStatus.toSQLStringifiedEnum();
        String expectedValue = "('AWAITING_VERIFICATION', 'DISABLED', 'VERIFIED')";

        Assertions.assertEquals(expectedValue, realValue);
    }

}
