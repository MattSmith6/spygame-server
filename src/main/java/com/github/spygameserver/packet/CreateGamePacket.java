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

public class CreateGamePacket extends AbstractPacket {

    private static final int PACKET_ID = 16;

    int playerID, currentGame, gameType, maxPlayers, isPublic;
    boolean canCreateGame = false;
    String gameName, inviteCode;

    public CreateGamePacket() {
        super(PACKET_ID);
    }

    @Override
    public void process(PacketManager packetManager, PlayerEncryptionKey playerEncryptionKey,
                        BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        try {
            // Read object from the reader, can read using #getInt, #getString, etc.
            JSONObject firstReadObject = readJSONObjectFromInput(playerEncryptionKey, bufferedReader);

            gameType = firstReadObject.getInt("game_type");
            maxPlayers = firstReadObject.getInt("maw_players");
            isPublic = firstReadObject.getInt("is_public");
            gameType = firstReadObject.getString("game_name");

            GameDatabase gameDatabase = packetManager.getGameDatabase();
            GameLobbyTable gameLobbyTable = gameDatabase.getGameLobbyTable();
            PlayerGameInfoTable playerGameInfoTable = gameDatabase.getPlayerGameInfoTable();

            playerID = playerEncryptionKey.getPlayerId();

            ConnectionHandler connectionHandler = gameDatabase.getNewConnectionHandler(false);

            currentGame = playerGameInfoTable.getCurrentNumber(connectionHandler, "current_game_id", playerID);

            // Set object properties using #put
            JSONObject objectToSend = new JSONObject();

            if (currentGame != 0) {
                objectToSend.put("success", canCreateGame);
            }
            else {
                canCreateGame = true;
                objectToSend.put("success", canCreateGame);

                gameLobbyTable.createGame(connectionHandler, isPublic, gameType, maxPlayers, gameName);
                inviteCode = gameLobbyTable.generateInviteCode(connectionHandler);

                objectToSend.put("invite_code", inviteCode);
            }

            connectionHandler.setShouldCloseConnectionAfterUse(true);
            connectionHandler.closeConnectionIfNecessary();

            // Write the JSON object to the player's app
            writeJSONObjectToOutput(playerEncryptionKey, objectToSend, bufferedWriter);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}