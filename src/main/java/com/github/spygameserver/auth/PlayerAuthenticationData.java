package com.github.spygameserver.auth;

public class PlayerAuthenticationData {

    private final int playerId;
    private final String salt;
    private final String verifier;

    public PlayerAuthenticationData(int playerId, String salt, String verifier) {
        this.playerId = playerId;
        this.salt = salt;
        this.verifier = verifier;
    }

    public int getPlayerId() {
        return playerId;
    }

    public String getSalt() {
        return salt;
    }

    public String getVerifier() {
        return verifier;
    }

}
