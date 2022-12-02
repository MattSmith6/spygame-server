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
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Base64;

public class ServerHandshakePacket extends AbstractPacket {

    private static final int PACKET_ID = 0;

    private final GameDatabase gameDatabase;
    private final AuthenticationDatabase authenticationDatabase;

    public ServerHandshakePacket(GameDatabase gameDatabase, AuthenticationDatabase authenticationDatabase) {
        super(PACKET_ID);

        this.gameDatabase = gameDatabase;
        this.authenticationDatabase = authenticationDatabase;
    }

    @Override
    public void process(PacketManager packetManager, PlayerEncryptionKey playerEncryptionKey,
                        BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        JSONObject usernamePacket = readUnencryptedObjectFromInput(bufferedReader);

        if (usernamePacket == null) {
            writeErrorObject(bufferedWriter, "Bad handshake, no username object found");
            return;
        }

        String username = usernamePacket.getString("I");

        if (username == null) {
            writeErrorObject(bufferedWriter, "Bad handshake, I is null in username object");
        }

        ConnectionHandler connectionHandler = gameDatabase.getNewConnectionHandler(true);
        Integer playerId = gameDatabase.getPlayerAccountTable().getPlayerIdByUsername(connectionHandler, username);

        if (playerId == null) {
            writeErrorObject(bufferedWriter, "bad_record_mac");
            return;
        }

        ServerAuthenticationHandshake handshake = new ServerAuthenticationHandshake(username, authenticationDatabase);
        JSONObject responseToPlayerHello = handshake.respondToHello(playerId);

        writeUnencryptedObjectToOutput(bufferedWriter, responseToPlayerHello);

        if (responseToPlayerHello.has("error")) {
            return;
        }

        JSONObject keyExchangeObject = readUnencryptedObjectFromInput(bufferedReader);

        if (keyExchangeObject == null) {
            writeErrorObject(bufferedWriter, "Bad handshake, no key exchange object");
            return;
        }

        if (!keyExchangeObject.has("A")) {
            writeErrorObject(bufferedWriter, "Bad handshake, no A in key exchange object");
            return;
        }

        if (!keyExchangeObject.has("M1")) {
            writeErrorObject(bufferedWriter, "Bad handshake, no M1 in key exchange object");
            return;
        }

        SRP6IntegerVariable A = new SRP6CustomIntegerVariable(keyExchangeObject.getBigInteger("A"));
        Bytes M1 = Bytes.wrapped(Base64.getDecoder().decode(keyExchangeObject.getString("M1")));

        JSONObject responseToKeyExchange = handshake.respondToKeyExchange(A, M1);
        writeUnencryptedObjectToOutput(bufferedWriter, responseToKeyExchange);

        if (responseToKeyExchange.has("error")) {
            return;
        }

        playerEncryptionKey.initialize(playerId, handshake.getPremasterSecret());
    }

    private void writeErrorObject(BufferedWriter bufferedWriter, String message) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("error", message);

        writeUnencryptedObjectToOutput(bufferedWriter, jsonObject);
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

    private void writeUnencryptedObjectToOutput(BufferedWriter bufferedWriter, JSONObject jsonObject) {
        try {
            bufferedWriter.write(jsonObject.toString());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}
