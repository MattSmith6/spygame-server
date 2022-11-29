package com.github.spygameserver.packet;

import com.github.spygameserver.auth.PlayerEncryptionKey;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;

public abstract class AbstractPacket {

    private final int packetId;

    protected AbstractPacket(int packetId) {
        this.packetId = packetId;
    }

    public int getPacketId() {
        return this.packetId;
    }

    public abstract boolean process(PacketManager packetManager, PlayerEncryptionKey playerEncryptionKey,
                                 BufferedReader bufferedReader, PrintWriter printWriter);

    protected void writeJSONObjectToOutput(PlayerEncryptionKey playerEncryptionKey, JSONObject jsonObject,
                                           PrintWriter printWriter) throws IOException {
        String encryptedObject = playerEncryptionKey.encryptJSONObject(jsonObject);

        printWriter.println(encryptedObject);
    }

    protected JSONObject readJSONObjectFromInput(PlayerEncryptionKey playerEncryptionKey,
                                                 BufferedReader bufferedReader) throws IOException {
        return playerEncryptionKey.decryptJSONObject(bufferedReader.readLine());
    }

}
