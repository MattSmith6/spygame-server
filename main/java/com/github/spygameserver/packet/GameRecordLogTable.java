package com.github.spygameserver.packet;

import com.github.spygameserver.auth.PlayerEncryptionKey;
import com.github.spygameserver.packet.AbstractPacket;
import com.github.spygameserver.packet.PacketManager;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

public class ExamplePacket extends AbstractPacket {

    private static final int PACKET_ID = 31; //pick the number for the package
    //variable info;
    String invitationCode;
    int currentPlayer;
    int playerID;

    public ServerHandshakePacket() {
        super(PACKET_ID);
    }

    @Override
    public void process(PacketManager packetManager, PlayerEncryptionKey playerEncryptionKey,
                        BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        try {
            // Read object from the reader, can read using #getInt, #getString, etc.
            JSONObject firstReadObject = readJSONObjectFromInput(playerEncryptionKey, bufferedReader);
            //firstReadObject.getInt/String("field name", info);
            this.invitationCode = firstReadObject.getString("invite_code");
            this.currentPlayer = firstReadObject.getInt("current_player");
            this.playerID = playerEncryptionKey.getPlayerId();

            // Set object properties using #put
            JSONObject objectToSend = new JSONObject();
            //firstReadObject.put("field name", info);
            objectToSend.put("invitationCode", this.invitationCode);
            objectToSend.put("current_player", this.currentPlayer);
            objectToSend.put("playerID", this.playerID);

            // Write the JSON object to the player's app
            writeJSONObjectToOutput(playerEncryptionKey, objectToSend, bufferedWriter);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}