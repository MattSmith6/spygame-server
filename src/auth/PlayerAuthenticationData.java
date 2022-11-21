package com.github.spygameserver.auth;

import com.github.glusk.caesar.Bytes;
import com.github.glusk.srp6_variables.SRP6CustomIntegerVariable;

import java.nio.ByteOrder;

public class PlayerAuthenticationData {

    private final int playerId;
    private final SRP6CustomIntegerVariable salt;
    private final SRP6CustomIntegerVariable verifier;

    public PlayerAuthenticationData(int playerId, SRP6CustomIntegerVariable salt, SRP6CustomIntegerVariable verifier) {
        this.playerId = playerId;
        this.salt = salt;
        this.verifier = verifier;
    }

    public int getPlayerId() {
        return playerId;
    }

    public SRP6CustomIntegerVariable getSalt() {
        return salt;
    }

    public byte[] getSaltByteArray() {
        return transformSRP6VariableToByteArray(getSalt());
    }

    public SRP6CustomIntegerVariable getVerifier() {
        return verifier;
    }

    public byte[] getVerifierByteArray() {
        return transformSRP6VariableToByteArray(getVerifier());
    }

    private byte[] transformSRP6VariableToByteArray(SRP6CustomIntegerVariable customIntegerVariable) {
        return customIntegerVariable.bytes(ByteOrder.BIG_ENDIAN).asArray();
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof PlayerAuthenticationData)) {
            return false;
        }

        PlayerAuthenticationData otherAuthenticationData = (PlayerAuthenticationData) other;

        return getPlayerId() == otherAuthenticationData.getPlayerId() && getSalt().equals(otherAuthenticationData.getSalt())
                && getVerifier().equals(otherAuthenticationData.getVerifier());
    }

}
