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
import java.io.PrintWriter;
import java.sql.Connection;

public class JoinGamePacket extends AbstractPacket {

    private static final int PACKET_ID = 11;

    boolean canJoinGame = true;
    int currentPlayers;
    int playerID;
    String code;
    GameLobbyTable.Pair<Integer, Long> gameStuff = null;

    public JoinGamePacket() {
        super(PACKET_ID);
    }

    @Override
    public boolean process(PacketManager packetManager, PlayerEncryptionKey playerEncryptionKey,
                        BufferedReader bufferedReader, PrintWriter printWriter) {
        try {
            // Read object from the reader, can read using #getInt, #getString, etc.
            JSONObject firstReadObject = readJSONObjectFromInput(playerEncryptionKey, bufferedReader);

            code = firstReadObject.getString("invite_code");

            GameDatabase gameDatabase = packetManager.getGameDatabase();
            GameLobbyTable gameLobbyTable = gameDatabase.getGameLobbyTable();
            PlayerGameInfoTable playerGameInfoTable = gameDatabase.getPlayerGameInfoTable();

            playerID = playerEncryptionKey.getPlayerId();

            ConnectionHandler connectionHandler = gameDatabase.getNewConnectionHandler(false);

            gameStuff = gameLobbyTable.getGameIdFromInviteCode(connectionHandler, code);
            if (gameStuff.getL() == null || gameStuff.getR() != null)
                canJoinGame = false;

            // Set object properties using #put
            JSONObject objectToSend = new JSONObject();

            if (canJoinGame) {
                currentPlayers = gameLobbyTable.getCurrentPlayers(connectionHandler, gameStuff.getL()) + 1;
                gameLobbyTable.updateCurrentPlayers(connectionHandler, currentPlayers, gameStuff.getL());

                //put the player id in the game
                playerGameInfoTable.updateCurrentGameId(connectionHandler, gameStuff.getL(), playerID);
            }

            connectionHandler.setShouldCloseConnectionAfterUse(true);
            connectionHandler.closeConnectionIfNecessary();

            objectToSend.put("success", canJoinGame);

            // Write the JSON object to the player's app
            writeJSONObjectToOutput(playerEncryptionKey, objectToSend, printWriter);
            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
    }
}