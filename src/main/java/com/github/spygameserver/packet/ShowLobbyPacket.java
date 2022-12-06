package com.github.spygameserver.packet;

import com.github.spygameserver.auth.PlayerEncryptionKey;
import com.github.spygameserver.database.ConnectionHandler;
import com.github.spygameserver.database.impl.GameDatabase;
import com.github.spygameserver.database.table.GameLobbyTable;
import com.github.spygameserver.database.table.PlayerGameInfoTable;
import com.github.spygameserver.packet.AbstractPacket;
import com.github.spygameserver.packet.PacketManager;
import org.json.JSONObject;
import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ShowLobbyPacket extends AbstractPacket {

    private static final int PACKET_ID = 15;

    String inviteCode;
    GameLobbyTable.game currentGame = null;
    GameLobbyTable.Pair<Integer, Long> gameStuff = null;

    public ShowLobbyPacket() {
        super(PACKET_ID);
    }

    @Override
    public void process(PacketManager packetManager, PlayerEncryptionKey playerEncryptionKey,
                        BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        try {
            // Read object from the reader, can read using #getInt, #getString, etc.
            JSONObject firstReadObject = readJSONObjectFromInput(playerEncryptionKey, bufferedReader);

            inviteCode = firstReadObject.getString("invite_code");

            // Set object properties using #put
            JSONObject objectToSend = new JSONObject();

            GameDatabase gameDatabase = packetManager.getGameDatabase();
            PlayerGameInfoTable gameInfoTable = gameDatabase.getPlayerGameInfoTable();
            GameLobbyTable gameLobbyTable = gameDatabase.getGameLobbyTable();

            ConnectionHandler connectionHandler = gameDatabase.getNewConnectionHandler(true);

            gameStuff = gameLobbyTable.getGameIdFromInviteCode(connectionHandler, inviteCode);

            objectToSend = gameInfoTable.getCurrentLobby(connectionHandler, gameStuff.getL());

            currentGame = gameLobbyTable.showAll(connectionHandler, inviteCode);

            objectToSend.put("game_name", currentGame.getGameName());
            objectToSend.put("current_players", currentGame.getCurrentPlayers());
            objectToSend.put("max_players", currentGame.getMaxPlayers());

            // Write the JSON object to the player's app
            writeJSONObjectToOutput(playerEncryptionKey, objectToSend, bufferedWriter);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
