package com.github.spygameserver.player.account;

public enum AccountVerificationStatus {

    AWAITING_VERIFICATION,
    DISABLED,
    VERIFIED;

    public static String toSQLStringifiedEnum() {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append('(');

        for (AccountVerificationStatus accountVerificationStatus : values()) {
            stringBuilder.append('\'');
            stringBuilder.append(accountVerificationStatus.name());
            stringBuilder.append("', ");
        }

        // We need to remove the last ", " separators
        String stringedVersion = stringBuilder.substring(0, stringBuilder.length() - 2);
        return stringedVersion + ")";
    }

}
