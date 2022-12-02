package com.github.spygameserver.player.account;

public class PlayerVerificationData {

    private final int playerId;
    private final AccountVerificationStatus accountVerificationStatus;

    public PlayerVerificationData(int playerId, AccountVerificationStatus accountVerificationStatus) {
        this.playerId = playerId;
        this.accountVerificationStatus = accountVerificationStatus;
    }

    public int getPlayerId() {
        return playerId;
    }

    public AccountVerificationStatus getAccountVerificationStatus() {
        return accountVerificationStatus;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof PlayerVerificationData)) {
            return false;
        }

        PlayerVerificationData otherVerificationData = (PlayerVerificationData) other;

        return getPlayerId() == otherVerificationData.getPlayerId() &&
                getAccountVerificationStatus() == otherVerificationData.getAccountVerificationStatus();
    }

}
