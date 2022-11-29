package com.github.spygameserver.packet;

import com.github.spygameserver.auth.PlayerEncryptionKey;
import com.github.spygameserver.database.ConnectionHandler;
import com.github.spygameserver.database.impl.GameDatabase;
import com.github.spygameserver.database.table.GameLobbyTable;
import com.github.spygameserver.packet.AbstractPacket;
import com.github.spygameserver.packet.PacketManager;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;

public class ShowPublicGamesPacket extends AbstractPacket {

    private static final int PACKET_ID = 13;

    int numPublicGames= 0;

    public ShowPublicGamesPacket() {
        super(PACKET_ID);
    }

    @Override
    public boolean process(PacketManager packetManager, PlayerEncryptionKey playerEncryptionKey,
                        BufferedReader bufferedReader, PrintWriter printWriter) {
        try {
            // Read object from the reader, can read using #getInt, #getString, etc.
            JSONObject firstReadObject = readJSONObjectFromInput(playerEncryptionKey, bufferedReader);

            GameDatabase gameDatabase = packetManager.getGameDatabase();
            GameLobbyTable gameLobbyTable = gameDatabase.getGameLobbyTable();
            ConnectionHandler connectionHandler = gameDatabase.getNewConnectionHandler(false);

            // Set object properties using #put
            JSONObject objectToSend = new JSONObject();

            objectToSend = gameLobbyTable.getPublicGames(connectionHandler);

            connectionHandler.setShouldCloseConnectionAfterUse(true);
            connectionHandler.closeConnectionIfNecessary();

            // Write the JSON object to the player's app
            writeJSONObjectToOutput(playerEncryptionKey, objectToSend, printWriter);
            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
    }
}