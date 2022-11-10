package com.github.spygameserver.packet.auth;

import com.github.spygameserver.auth.PlayerEncryptionKey;
import com.github.spygameserver.packet.AbstractPacket;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

public class ServerHandshakePacket extends AbstractPacket {

    public ServerHandshakePacket(int packetId) {
        super(packetId);
    }

    @Override
    public void process(PlayerEncryptionKey playerEncryptionKey, BufferedReader bufferedReader,
                        BufferedWriter bufferedWriter) {
        try {
            // Read object from the reader, can read using #getInt, #getString, etc.
            JSONObject firstReadObject = readJSONObjectFromInput(playerEncryptionKey, bufferedReader);

            // Set object properties using #put
            JSONObject objectToSend = new JSONObject();

            // Write the JSON object to the player's app
            writeJSONObjectToOutput(playerEncryptionKey, objectToSend, bufferedWriter);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
