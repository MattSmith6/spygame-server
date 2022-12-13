package com.github.spygameserver.auth;

import com.github.glusk.caesar.Bytes;
import com.github.glusk.caesar.PlainText;
import com.github.glusk.caesar.hashing.Hash;
import com.github.glusk.srp6_variables.SRP6CustomIntegerVariable;
import com.github.glusk.srp6_variables.SRP6IntegerVariable;
import com.github.glusk.srp6_variables.SRP6PrivateKey;
import com.github.glusk.srp6_variables.SRP6Verifier;

import java.nio.ByteOrder;
import java.security.SecureRandom;

/**
 * Represents the data that is inserted into or retrieved from the PlayerAuthenticationTable. Includes references
 * to a player id, a salt, and a verifier. The salt and verifier are used as SRP-6 variables for authentication.
 */
public class PlayerAuthenticationData {

    private final int playerId;
    private final Bytes salt;
    private final SRP6IntegerVariable verifier;

    public PlayerAuthenticationData(int playerId, Bytes salt, SRP6IntegerVariable verifier) {
        this.playerId = playerId;
        this.salt = salt;
        this.verifier = verifier;
    }

    /**
     * The constructor to generate the necessary SRP-6 variables (salt and verifier) from a provided username and password
     * @param playerId the player id
     * @param username the username
     * @param password the password
     */
    public PlayerAuthenticationData(int playerId, String username, String password) {
        this.playerId = playerId;

        PlainText I = new PlainText(username);
        PlainText P = new PlainText(password);

        SecureRandom rng = new SecureRandom();

        // Setup the randomly generated salt, s, and the verifier from the login credentials, v
        Bytes s = Bytes.wrapped(rng.generateSeed(32));

        SRP6IntegerVariable x = new SRP6PrivateKey(ServerAuthenticationHandshake.IMD, s, I, P,
                ServerAuthenticationHandshake.BYTE_ORDER);
        SRP6IntegerVariable v = new SRP6Verifier(ServerAuthenticationHandshake.N, ServerAuthenticationHandshake.g, x);

        this.salt = s;
        this.verifier = v;
    }

    public int getPlayerId() {
        return playerId;
    }

    public Bytes getSalt() {
        return salt;
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
