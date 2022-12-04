package com.github.spygameserver.packet;

import com.github.spygameserver.auth.PlayerEncryptionKey;
import com.github.spygameserver.packet.auth.ServerHandshakePacket;
import com.github.spygameserver.util.ExceptionHandling;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class ServerPacketReader extends Thread {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerPacketReader.class);

    private final Socket socket;
    private final BufferedReader bufferedReader;
    private final PrintWriter printWriter;

    private final PacketManager packetManager;

    private final PlayerEncryptionKey playerEncryptionKey;

    public ServerPacketReader(Socket socket, PacketManager packetManager) throws IOException {
        this.socket = socket;
        this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.printWriter = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);

        this.packetManager = packetManager;

        this.playerEncryptionKey = new PlayerEncryptionKey();
    }

    @Override
    public void run() {
        while (!socket.isClosed()) {
            LOGGER.info("Awaiting packet...");
            processPacket();
        }

        LOGGER.info("Exited packet processing, awaiting garbage collection");
        ExceptionHandling.closeQuietly(socket, bufferedReader, printWriter);
    }

    private void processPacket() {
        String expectedPacketId;
        try {
            expectedPacketId = bufferedReader.readLine();
            LOGGER.info("Expected packet id: " + expectedPacketId);
        } catch (IOException ex) {
            ExceptionHandling.closeQuietly(socket);
            LOGGER.warn("Exception reading packet id... " + ex.getMessage());
            return;
        }

        int packetId;
        try {
            packetId = Integer.parseInt(expectedPacketId);
            LOGGER.info("Packet id: " + packetId);
        } catch (NumberFormatException ex) {
            LOGGER.warn("Expected integer packet number, received " + expectedPacketId);
            ExceptionHandling.closeQuietly(socket);
            return;
        }

        AbstractPacket abstractPacket = packetManager.getNewPacket(packetId);
        if (abstractPacket == null) {
            LOGGER.warn("Packet id not found.");
            writeError("Packet id not found.");
            ExceptionHandling.closeQuietly(socket);
            return;
        }

        if (!playerEncryptionKey.isInitialized() && !(abstractPacket instanceof ServerHandshakePacket)) {
            LOGGER.warn("Not initialized, must authenticate");
            writeError("Must authenticate before using any other packets.");
            ExceptionHandling.closeQuietly(socket);
            return;
        }

        LOGGER.info("Processing packet...");

        if (!abstractPacket.process(packetManager, playerEncryptionKey, bufferedReader, printWriter)) {
            LOGGER.info("Closing connection because of error in packet processing...");
            ExceptionHandling.closeQuietly(socket);
        }
    }

    private void writeError(String errorMessage) {
        printWriter.println(errorMessage);
    }

}
