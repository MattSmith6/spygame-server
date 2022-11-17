package com.github.spygameserver.database;

import com.github.spygameserver.DatabaseRequiredTest;
import com.github.spygameserver.database.impl.AuthenticationDatabase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;

// This class, by proxy, also tests DatabaseCredentialsProcessor
public class DatabaseCreatorTest implements DatabaseRequiredTest {

    private DatabaseCreator<AuthenticationDatabase> getNewDatabaseCreator(File file, String databasePath) {
        return new DatabaseCreator<>(file, databasePath, true);
    }

    @Test
    public void checkExceptionAndCreationForNonexistentFile() {
        File nonExistentFile = getNonExistantCredentialsFile();

        // We want the file to not be present to check errors and file creation
        if (nonExistentFile.exists()) {
            nonExistentFile.delete();
        }

        DatabaseCreator<AuthenticationDatabase> databaseCreator = getNewDatabaseCreator(nonExistentFile, "empty");

        // Check that the system errors when creating the database from an empty file
        Assertions.assertThrows(IllegalStateException.class,
                () -> databaseCreator.createDatabaseFromFile(AuthenticationDatabase::new),
                "IllegalStateException should be thrown if the database credentials file did not exist");

        // Check that the template file was created before the exception was thrown
        Assertions.assertTrue(nonExistentFile.exists(), "Template should be created for file if it didn't exist");
    }

    @Test
    public void checkExceptionOnInvalidCredentials() {
        File invalidCredentialsFile = getInvalidCredentialsFile();
        DatabaseCreator<AuthenticationDatabase> databaseCreator = getNewDatabaseCreator(invalidCredentialsFile, "invalid");

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> databaseCreator.createDatabaseFromFile(AuthenticationDatabase::new),
                "IllegalArgumentException should be thrown for any invalid database credentials");
    }

    @Test
    public void checkNotNullConnectionOnValidCredentials() {
        File databaseCredentials = getValidCredentialsFile();
        DatabaseCreator<AuthenticationDatabase> databaseCreator = getNewDatabaseCreator(databaseCredentials, "auth_db");

        AuthenticationDatabase authenticationDatabase = databaseCreator.createDatabaseFromFile(AuthenticationDatabase::new);
        ConnectionHandler connectionHandler = authenticationDatabase.getNewConnectionHandler(false);

        try (Connection connection = connectionHandler.getConnection()) {
            // ignore but make sure to close, checking if there's an error
        } catch (SQLException ex) {
            Assertions.fail("Error creating the SQL connection with valid database credentials.");
        }
    }

    @Override
    public void closeOpenConnections() {
        // ignored, no actual connections are used here, just the creations of databases
    }

}
