package com.github.spygameserver.packet;

import com.github.spygameserver.auth.PlayerEncryptionKey;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

public abstract class AbstractPacket {

    private final int packetId;

    protected AbstractPacket(int packetId) {
        this.packetId = packetId;
    }

    public int getPacketId() {
        return this.packetId;
    }

    public abstract void process(PacketManager packetManager, PlayerEncryptionKey playerEncryptionKey,
                                 BufferedReader bufferedReader, BufferedWriter bufferedWriter);

    protected void writeJSONObjectToOutput(PlayerEncryptionKey playerEncryptionKey, JSONObject jsonObject,
                                           BufferedWriter bufferedWriter) throws IOException {
        String encryptedObject = playerEncryptionKey.encryptJSONObject(jsonObject);
        bufferedWriter.write(encryptedObject);
    }

    protected JSONObject readJSONObjectFromInput(PlayerEncryptionKey playerEncryptionKey,
                                                 BufferedReader bufferedReader) throws IOException {
        String readObject = bufferedReader.readLine();
        return playerEncryptionKey.decryptJSONObject(readObject);
    }

}
