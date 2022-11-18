package com.github.spygameserver.packet;

import com.github.spygameserver.auth.PlayerEncryptionKey;
import com.github.spygameserver.database.table.GameLobbyTable;
import com.github.spygameserver.packet.AbstractPacket;
import com.github.spygameserver.packet.PacketManager;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LeaderboardPacket extends AbstractPacket {

    private static final int PACKET_ID = 12;

    int leaderboardSize = 5;
    string usernames[];
    int scores[];
    int i;

    public LeaderboardPacket() {
        super(PACKET_ID);
    }

    @Override
    public void process(PacketManager packetManager, PlayerEncryptionKey playerEncryptionKey,
                        BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        try {
            // Read object from the reader, can read using #getInt, #getString, etc.
            JSONObject firstReadObject = readJSONObjectFromInput(playerEncryptionKey, bufferedReader);

            // Set object properties using #put
            JSONObject objectToSend = new JSONObject();

            connectionHandler.closeConnectionIfNecessary();

            for(i = 0; i < leaderboardSize; i++)
            {
                ;
            }

            // Write the JSON object to the player's app
            writeJSONObjectToOutput(playerEncryptionKey, objectToSend, bufferedWriter);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
