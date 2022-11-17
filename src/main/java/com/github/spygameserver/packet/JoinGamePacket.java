package com.github.spygameserver.packet;

import com.github.spygameserver.auth.PlayerEncryptionKey;
import com.github.spygameserver.database.ConnectionHandler;
import com.github.spygameserver.database.table.GameLobbyTable;
import com.github.spygameserver.packet.AbstractPacket;
import com.github.spygameserver.packet.PacketManager;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

public class ServerHandshakePacket extends AbstractPacket {

    private static final int PACKET_ID = 11;cd

    boolean canJoinGame = true;
    int playerID = 1; //Dummy value
    int currentPlayers;
    String code;
    GameLobbyTable.Pair<> gameStuff =  new GameLobbyTable.Pair<>(gameId, startTime);

    public ServerHandshakePacket() {
        super(PACKET_ID);
    }

    @Override
    public void process(PacketManager packetManager, PlayerEncryptionKey playerEncryptionKey,
                        BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        try {
            // Read object from the reader, can read using #getInt, #getString, etc.
            JSONObject firstReadObject = readJSONObjectFromInput(playerEncryptionKey, bufferedReader);

            code = firstReadObject.getString(invite_code);

            gameStuff = GameLobbyTable.getGameIdFromInviteCode(connectionHandler, code);
            if (gameStuff.getL() == null || gameStuff.getR() != null)
                canJoinGame = false;

            // Set object properties using #put
            JSONObject objectToSend = new JSONObject();

            if (canJoinGame) {
                currentPlayers = GameLobbyTable.getCurrentPlayers(ConnectionHandler, gameStuff.getL()) + 1;
                GameLobbyTable.updateCurrentPlayers(connectionHandler, currentPlayers);

                //put the player id in the game
            }


            objectToSend.put("success", canJoinGame).toBoolean();

            // Write the JSON object to the player's app
            writeJSONObjectToOutput(playerEncryptionKey, objectToSend, bufferedWriter);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}