package com.github.spygameserver;

import com.github.spygameserver.database.ConnectionHandler;

import java.io.File;

public interface DatabaseRequiredTest {

    String VALID_CREDENTIALS_FILE = "database_credentials.properties";
    String INVALID_CREDENTIALS_FILE = "invalid_database_credentials.properties";
    String NON_EXISTANT_CREDENTIALS_FILE = "non_existant.properties";

    default File getValidCredentialsFile() {
        return getResource(VALID_CREDENTIALS_FILE);
    }

    default File getInvalidCredentialsFile() {
        return getResource(INVALID_CREDENTIALS_FILE);
    }

    default File getNonExistantCredentialsFile() {
        return getNonExistantResource(VALID_CREDENTIALS_FILE, NON_EXISTANT_CREDENTIALS_FILE);
    }

    default File getResource(String resourceName) {
        return new File(getClass().getClassLoader().getResource(resourceName).getFile());
    }

    default File getNonExistantResource(String realResourceName, String fakeResourceName) {
        File resourceDirectory = getResource(realResourceName).getParentFile();
        return new File(resourceDirectory, fakeResourceName);
    }

    void closeOpenConnections();

    default void closeOpenConnections(ConnectionHandler connectionHandler) {
        connectionHandler.setShouldCloseConnectionAfterUse(true);
        connectionHandler.closeConnectionIfNecessary();
    }

}
