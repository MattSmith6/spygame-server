package com.github.spygameserver.packet;

import com.github.spygameserver.auth.PlayerEncryptionKey;
import com.github.spygameserver.packet.auth.ServerHandshakePacket;
import com.github.spygameserver.util.ExceptionHandling;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class ServerPacketReader extends Thread {

    private final Socket socket;
    private final BufferedReader bufferedReader;
    private final BufferedWriter bufferedWriter;

    private final PacketManager packetManager;

    private final PlayerEncryptionKey playerEncryptionKey;

    public ServerPacketReader(Socket socket, PacketManager packetManager) throws IOException {
        this.socket = socket;
        this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

        this.packetManager = packetManager;

        this.playerEncryptionKey = new PlayerEncryptionKey();


    }

    @Override
    public void run() {
        super.run();

        while (!socket.isClosed()) {
            processPacket();
        }
    }

    private void processPacket() {
        String expectedPacketId;
        try {
            expectedPacketId = bufferedReader.readLine();
        } catch (IOException ex) {
            ExceptionHandling.closeQuietly(socket);
            return;
        }

        int packetId;
        try {
            packetId = Integer.parseInt(expectedPacketId);
        } catch (NumberFormatException ex) {
            writeError("Expected integer packet number, received " + expectedPacketId);
            return;
        }

        AbstractPacket abstractPacket = packetManager.getPacket(packetId);
        if (abstractPacket == null) {
            writeError("Packet id not found.");
            return;
        }

        if (!playerEncryptionKey.isInitialized() && !(abstractPacket instanceof ServerHandshakePacket)) {
            writeError("Must authenticate before using any other packets.");
            return;
        }

        abstractPacket.process(packetManager, playerEncryptionKey, bufferedReader, bufferedWriter);
    }

    private void writeError(String errorMessage) {
        try {
            bufferedWriter.write(errorMessage);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}
