package com.github.spygameserver.packet;

import com.github.spygameserver.auth.PlayerEncryptionKey;
import com.github.spygameserver.database.ConnectionHandler;
import com.github.spygameserver.database.impl.GameDatabase;
import com.github.spygameserver.database.table.GameLobbyTable;
import com.github.spygameserver.database.table.PlayerGameInfoTable;
import com.github.spygameserver.packet.AbstractPacket;
import com.github.spygameserver.packet.PacketManager;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.sql.Connection;

public class PlayerEliminatedPacket extends AbstractPacket {

    private static final int PACKET_ID = 14;

    int eliminatorID, playerID, gameID, maxPlayers, eliminatedPlayers, ended = 0;

    public PlayerEliminatedPacket() {
        super(PACKET_ID);
    }

    @Override
    public void process(PacketManager packetManager, PlayerEncryptionKey playerEncryptionKey,
                        BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        try {
            // Read object from the reader, can read using #getInt, #getString, etc.
            JSONObject firstReadObject = readJSONObjectFromInput(playerEncryptionKey, bufferedReader);

            eliminatorID = firstReadObject.getInt("eliminator_id");

            GameDatabase gameDatabase = packetManager.getGameDatabase();
            GameLobbyTable gameLobbyTable = gameDatabase.getGameLobbyTable();
            PlayerGameInfoTable playerGameInfoTable = gameDatabase.getPlayerGameInfoTable();

            playerID = playerEncryptionKey.getPlayerId();

            //Update Game Record Table

            ConnectionHandler connectionHandler = gameDatabase.getNewConnectionHandler(false);

            gameID = playerGameInfoTable.getCurrentNumber(connectionHandler, "current_game_id", playerID);
            maxPlayers = gameLobbyTable.getCurrentPlayers(connectionHandler, gameID);

            //Check eliminated players vs max/current players
            if(maxPlayers == (eliminatedPlayers + 1))
            {
                gameLobbyTable.updateEndTime(connectionHandler, gameID);
                ended = 1;
            }

            // Set object properties using #put
            JSONObject objectToSend = new JSONObject();

            connectionHandler.setShouldCloseConnectionAfterUse(true);
            connectionHandler.closeConnectionIfNecessary();

            objectToSend.put("ended", ended);

            // Write the JSON object to the player's app
            writeJSONObjectToOutput(playerEncryptionKey, objectToSend, bufferedWriter);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}