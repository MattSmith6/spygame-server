package com.github.spygameserver.packet.auth;

import com.github.glusk.caesar.Bytes;
import com.github.glusk.srp6_variables.SRP6CustomIntegerVariable;
import com.github.glusk.srp6_variables.SRP6IntegerVariable;
import com.github.spygameserver.auth.PlayerEncryptionKey;
import com.github.spygameserver.auth.ServerAuthenticationHandshake;
import com.github.spygameserver.database.ConnectionHandler;
import com.github.spygameserver.database.impl.AuthenticationDatabase;
import com.github.spygameserver.database.impl.GameDatabase;
import com.github.spygameserver.packet.AbstractPacket;
import com.github.spygameserver.packet.PacketManager;
import com.github.spygameserver.packet.ServerPacketReader;
import com.github.spygameserver.player.account.AccountVerificationStatus;
import com.github.spygameserver.player.account.PlayerVerificationData;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteOrder;
import java.util.Base64;

/**
 * The packet that handles the authentication handshake between the player and the server.
 */
public class ServerHandshakePacket extends AbstractPacket {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerPacketReader.class);

    private static final int PACKET_ID = 0;

    public ServerHandshakePacket() {
        super(PACKET_ID);
    }

    @Override
    public boolean process(PacketManager packetManager, PlayerEncryptionKey playerEncryptionKey,
                        BufferedReader bufferedReader, PrintWriter printWriter) {
        LOGGER.info("Trying to read first unencrypted object...");
        JSONObject usernamePacket = readUnencryptedObjectFromInput(bufferedReader);

        // Username sanity checks to ensure correct packet communication
        if (usernamePacket == null || !usernamePacket.has("I")) {
            LOGGER.warn("No username packet, writing bad handshake error");
            writeErrorObject(printWriter, "Bad handshake, no username object found");
            return false;
        }

        String username = usernamePacket.getString("I");

        // Ensure that the username isn't null
        if (username == null) {
            LOGGER.warn("No I in username object, writing bad handshake...");
            writeErrorObject(printWriter, "Bad handshake, I is null in username object");
            return false;
        }

        GameDatabase gameDatabase = packetManager.getGameDatabase();
        AuthenticationDatabase authenticationDatabase = packetManager.getAuthenticationDatabase();

        // Obtain player verification data from the username
        ConnectionHandler connectionHandler = gameDatabase.getNewConnectionHandler(true);
        PlayerVerificationData playerVerificationData = gameDatabase.getPlayerAccountTable()
                .getPlayerVerificationData(connectionHandler, username);

        // Ensure that the account exists
        if (playerVerificationData == null) {
            LOGGER.warn("No matching player id, writing bad handshake...");
            writeErrorObject(printWriter, "bad_record_mac");
            return false;
        }

        // Ensure that the account is verified before attempting to login
        if (playerVerificationData.getAccountVerificationStatus() != AccountVerificationStatus.VERIFIED) {
            LOGGER.warn("Account not verified, writing bad handshake...");
            writeErrorObject(printWriter, "bad_record_mac");
            return false;
        }

        int playerId = playerVerificationData.getPlayerId();

        // Create the authentication object to feed the player id for the account
        ServerAuthenticationHandshake handshake = new ServerAuthenticationHandshake(username, authenticationDatabase);
        JSONObject responseToPlayerHello = handshake.respondToHello(playerId);

        writeUnencryptedObjectToOutput(printWriter, responseToPlayerHello);
        LOGGER.info("Wrote response to player hello in output");

        if (responseToPlayerHello.has("error")) {
            LOGGER.warn("Error in handshake: " + responseToPlayerHello.getString("error"));
            writeErrorObject(printWriter, responseToPlayerHello.getString("error"));
            return false;
        }

        JSONObject keyExchangeObject = readUnencryptedObjectFromInput(bufferedReader);
        LOGGER.info("Read key exchange output");

        if (keyExchangeObject == null) {
            LOGGER.warn("Bad handshake, no key exchange");
            writeErrorObject(printWriter, "Bad handshake, no key exchange object");
            return false;
        }

        if (!keyExchangeObject.has("A")) {
            LOGGER.warn("Bad handshake, no A");
            writeErrorObject(printWriter, "Bad handshake, no A in key exchange object");
            return false;
        }

        if (!keyExchangeObject.has("M1")) {
            LOGGER.warn("Bad handshake, no M1");
            writeErrorObject(printWriter, "Bad handshake, no M1 in key exchange object");
            return false;
        }

        // Get the response variables out of the key exchange object sent by the player
        SRP6IntegerVariable A = new SRP6CustomIntegerVariable(getDecodedBytes(keyExchangeObject, "A"), ByteOrder.BIG_ENDIAN);
        Bytes M1 = getDecodedBytes(keyExchangeObject, "M1");

        JSONObject responseToKeyExchange = handshake.respondToKeyExchange(A, M1);

        if (responseToKeyExchange.has("error")) {
            LOGGER.warn("Bad response to key exchange: " + responseToKeyExchange.getString("error"));
            writeErrorObject(printWriter, responseToKeyExchange.getString("error"));
            return false;
        }

        writeUnencryptedObjectToOutput(printWriter, responseToKeyExchange);
        LOGGER.info("Wrote server proof to output");

        playerEncryptionKey.initialize(playerId, handshake.getPremasterSecret());
        LOGGER.info("Initialized premaster secret");

        return true;
    }

    private Bytes getDecodedBytes(JSONObject jsonObject, String key) {
        return Bytes.wrapped(Base64.getDecoder().decode(jsonObject.getString(key)));
    }

    private void writeErrorObject(PrintWriter printWriter, String message) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("error", message);

        writeUnencryptedObjectToOutput(printWriter, jsonObject);
    }

    private JSONObject readUnencryptedObjectFromInput(BufferedReader bufferedReader) {
        try {
            JSONTokener jsonTokener = new JSONTokener(bufferedReader.readLine());
            return new JSONObject(jsonTokener);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return null;
    }

    private void writeUnencryptedObjectToOutput(PrintWriter printWriter, JSONObject jsonObject) {
        printWriter.println(jsonObject.toString());
    }

}
