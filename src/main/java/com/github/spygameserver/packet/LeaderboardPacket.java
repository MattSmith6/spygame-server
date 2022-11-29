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
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LeaderboardPacket extends AbstractPacket {

    private static final int PACKET_ID = 12;

    int lbsize = 5;

    public LeaderboardPacket() {
        super(PACKET_ID);
    }

    @Override
    public boolean process(PacketManager packetManager, PlayerEncryptionKey playerEncryptionKey,
                        BufferedReader bufferedReader, PrintWriter printWriter) {
        try {
            // Read object from the reader, can read using #getInt, #getString, etc.
            JSONObject firstReadObject = readJSONObjectFromInput(playerEncryptionKey, bufferedReader);

            // Set object properties using #put
            JSONObject objectToSend = new JSONObject();

            GameDatabase gameDatabase = packetManager.getGameDatabase();
            PlayerGameInfoTable gameInfoTable = gameDatabase.getPlayerGameInfoTable();

            ConnectionHandler connectionHandler = gameDatabase.getNewConnectionHandler(true);

            objectToSend = gameInfoTable.getLeaderboard(connectionHandler, lbsize);

            // Write the JSON object to the player's app
            writeJSONObjectToOutput(playerEncryptionKey, objectToSend, printWriter);

            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
    }
}
