package com.github.spygameserver;

import com.github.spygameserver.auth.website.SparkWebsiteHandler;
import com.github.spygameserver.database.DatabaseCreator;
import com.github.spygameserver.database.DatabaseType;
import com.github.spygameserver.database.impl.AuthenticationDatabase;
import com.github.spygameserver.database.impl.GameDatabase;
import com.github.spygameserver.database.table.TableType;
import com.github.spygameserver.packet.PacketManager;
import com.github.spygameserver.packet.ServerPacketReader;
import com.github.spygameserver.util.ExceptionHandling;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class SpyGameServer {

    public static Logger LOGGER = LoggerFactory.getLogger(SpyGameServer.class);;
    private static final int SOCKET_PORT = 6532;

    private static volatile boolean run = true;

    public static void main(String[] args) {
        // Use production tables, not the testing tables
        TableType.setUseTestTables(false);

        DatabaseCreator<GameDatabase> gameDatabaseCreator = new DatabaseCreator<>(DatabaseType.GAME);
        GameDatabase gameDatabase = gameDatabaseCreator.createDatabase(GameDatabase::new);

        DatabaseCreator<AuthenticationDatabase> authDatabaseCreator = new DatabaseCreator<>(DatabaseType.AUTHENTICATION);
        AuthenticationDatabase authenticationDatabase = authDatabaseCreator.createDatabase(AuthenticationDatabase::new);

        SparkWebsiteHandler sparkWebsiteHandler = new SparkWebsiteHandler(gameDatabase, authenticationDatabase);

        PacketManager packetManager = new PacketManager(gameDatabase, authenticationDatabase);
        ServerSocket serverSocket = null;

        try {
            serverSocket = new ServerSocket(SOCKET_PORT);
        } catch (IOException ex) {
            ex.printStackTrace();
            System.exit(-1);
        }

        while (run) {
            // Keep running program unless we receive the exit command

            try {
                Socket socket = serverSocket.accept();
                LOGGER.info("Accepted connection from " + socket.getInetAddress().toString());
                Thread serverPacketReader = new ServerPacketReader(socket, packetManager);

                serverPacketReader.start();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        // Shutdown the website and close connection pools for to the database
        sparkWebsiteHandler.shutdown();
        ExceptionHandling.closeQuietly(gameDatabase, authenticationDatabase);
    }

}
