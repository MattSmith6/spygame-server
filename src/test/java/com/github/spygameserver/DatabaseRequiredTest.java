package com.github.spygameserver;

import com.github.spygameserver.database.ConnectionHandler;
import com.github.spygameserver.database.DatabaseConnectionManager;
import com.github.spygameserver.database.DatabaseCreator;
import com.github.spygameserver.database.impl.AbstractDatabase;
import com.github.spygameserver.database.impl.AuthenticationDatabase;
import com.github.spygameserver.database.impl.GameDatabase;

import java.io.File;
import java.util.function.BiFunction;

public interface DatabaseRequiredTest {

    String VALID_CREDENTIALS_FILE = "database_credentials.properties";
    String INVALID_CREDENTIALS_FILE = "invalid_database_credentials.properties";
    String NON_EXISTANT_CREDENTIALS_FILE = "non_existant.properties";

    String SQL_CERTIFICATE_FILE = "ca-certificate.crt";

    default File getValidCredentialsFile() {
        return getResource(VALID_CREDENTIALS_FILE);
    }

    default File getInvalidCredentialsFile() {
        return getResource(INVALID_CREDENTIALS_FILE);
    }

    default File getNonExistantCredentialsFile() {
        return getNonExistantResource(VALID_CREDENTIALS_FILE, NON_EXISTANT_CREDENTIALS_FILE);
    }

    default File getCertificateFile() {
        return getResource(SQL_CERTIFICATE_FILE);
    }

    default File getResource(String resourceName) {
        return new File(getClass().getClassLoader().getResource(resourceName).getFile());
    }

    default File getNonExistantResource(String realResourceName, String fakeResourceName) {
        File resourceDirectory = getResource(realResourceName).getParentFile();
        return new File(resourceDirectory, fakeResourceName);
    }

    default <T extends AbstractDatabase> T getDatabase(DatabaseType databaseType,
                                                       BiFunction<DatabaseConnectionManager, Boolean, T> databaseConstructor) {
        DatabaseCreator<T> databaseCreator = new DatabaseCreator<>(getValidCredentialsFile(),
                databaseType.getDatabasePath(), getCertificateFile(), true);
        return databaseCreator.createDatabaseFromFile(databaseConstructor);
    }

    default GameDatabase getGameDatabase() {
        return getDatabase(DatabaseType.GAME, GameDatabase::new);
    }

    default AuthenticationDatabase getAuthenticationDatabase() {
        return getDatabase(DatabaseType.AUTHENTICATION, AuthenticationDatabase::new);
    }

    void closeOpenConnections();

    default void closeOpenConnections(ConnectionHandler connectionHandler) {
        connectionHandler.closeAbsolutely();
    }

}
