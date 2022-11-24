package com.github.spygameserver;

import com.github.spygameserver.auth.website.SparkWebsiteHandler;
import com.github.spygameserver.database.DatabaseCreator;
import com.github.spygameserver.database.DatabaseType;
import com.github.spygameserver.database.impl.AuthenticationDatabase;
import com.github.spygameserver.database.impl.GameDatabase;
import com.github.spygameserver.util.ExceptionHandling;

import java.util.Scanner;

public class SpyGameServer {

    public static void main(String[] args) {
        DatabaseCreator<GameDatabase> gameDatabaseCreator = new DatabaseCreator<>(DatabaseType.GAME, false);
        GameDatabase gameDatabase = gameDatabaseCreator.createDatabase(GameDatabase::new);

        DatabaseCreator<AuthenticationDatabase> authDatabaseCreator = new DatabaseCreator<>(DatabaseType.AUTHENTICATION, false);
        AuthenticationDatabase authenticationDatabase = authDatabaseCreator.createDatabase(AuthenticationDatabase::new);

        SparkWebsiteHandler sparkWebsiteHandler = new SparkWebsiteHandler(gameDatabase, authenticationDatabase);

        Scanner scanner = new Scanner(System.in);
        while (!scanner.nextLine().trim().equals("exit")) {
            // Keep running program unless we receive the exit command
        }

        // Shutdown the website and close connection pools for to the database
        sparkWebsiteHandler.shutdown();
        ExceptionHandling.closeQuietly(gameDatabase, authenticationDatabase);
    }

}
