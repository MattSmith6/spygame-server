package com.github.spygameserver.database;

import com.github.spygameserver.database.impl.AbstractDatabase;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.BiFunction;

/**
 * Class to create a database given the appropriate credentials property file.
 */
public class DatabaseCreator<T extends AbstractDatabase> {

    private final String filePath;
    private final DatabaseCredentialsProcessor databaseCredentialsProcessor;
    private final boolean useTestTables;

    public DatabaseCreator(File directory, String fileName, String databasePath, File certificate, boolean useTestTables) {
        this(new File(directory, fileName), databasePath, certificate, useTestTables);
    }

    public DatabaseCreator(File credentialsFile, String databasePath, File certificate, boolean useTestTables) {
        this.filePath = credentialsFile.getPath();
        this.databaseCredentialsProcessor = new DatabaseCredentialsProcessor(credentialsFile, databasePath, certificate);
        this.useTestTables = useTestTables;
    }

    public T createDatabaseFromFile(BiFunction<DatabaseConnectionManager, Boolean, T> databaseConstructor) {
        if (!databaseCredentialsProcessor.didFileExistOnStartup()) {
            String errorMessage = "Configuration file did not exist. Configure the database credentials in the file "
                    + "located at %s and rerun the program.";

            throw new IllegalStateException(String.format(errorMessage, filePath));
        }

        DatabaseConnectionManager databaseConnectionManager = new DatabaseConnectionManager(databaseCredentialsProcessor);

        try (Connection connection = databaseConnectionManager.createNewConnection()) {
            // ignore, we're just seeing if a connection could be obtained at all
        } catch (SQLException ex) {
            throw new IllegalArgumentException(ex);
        }

        return databaseConstructor.apply(databaseConnectionManager, useTestTables);
    }

}
