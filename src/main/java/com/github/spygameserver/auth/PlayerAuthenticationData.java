package com.github.spygameserver.auth;

import com.github.glusk.caesar.Bytes;
import com.github.glusk.caesar.PlainText;
import com.github.glusk.caesar.hashing.Hash;
import com.github.glusk.srp6_variables.SRP6CustomIntegerVariable;
import com.github.glusk.srp6_variables.SRP6IntegerVariable;

import java.nio.ByteOrder;
import java.security.SecureRandom;

public class PlayerAuthenticationData {

    private final int playerId;
    private final Bytes salt;
    private final SRP6IntegerVariable verifier;

    public PlayerAuthenticationData(int playerId, Bytes salt, SRP6IntegerVariable verifier) {
        this.playerId = playerId;
        this.salt = salt;
        this.verifier = verifier;
    }

    public PlayerAuthenticationData(int playerId, String username, String password) {
        this.playerId = playerId;

        PlainText usernameText = new PlainText(username);
        PlainText usernamePasswordDelimiter = new PlainText(":");
        PlainText passwordText = new PlainText(password);

        Bytes firstHash = new Hash(ServerAuthenticationHandshake.IMD, usernameText, usernamePasswordDelimiter, passwordText);

        // Generate random salt
        SecureRandom secureRandom = new SecureRandom();
        Bytes salt = Bytes.wrapped(secureRandom.generateSeed(32));

        Bytes verifier = new Hash(ServerAuthenticationHandshake.IMD, salt, firstHash);

        this.salt = salt;
        this.verifier = new SRP6CustomIntegerVariable(verifier, ByteOrder.BIG_ENDIAN);
    }

    public int getPlayerId() {
        return playerId;
    }

    public Bytes getSalt() {
        return salt;
    }

    public byte[] getSaltByteArray() {
        return salt.asArray();
    }

    public SRP6IntegerVariable getVerifier() {
        return verifier;
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
