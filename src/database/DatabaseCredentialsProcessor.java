package com.github.spygameserver.database;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Properties;

public class DatabaseCredentialsProcessor {

    private static final String DATABASE_CONNECTION_URL_FORMAT = "jdbc:mysql://%s:%s/%s";

    private static final String HOST_PATH = "host";
    private static final String PORT_PATH = "port";
    private static final String DATABASE_PATH = "database";

    private static final String USERNAME_PATH = "username";
    private static final String PASSWORD_PATH = "password";

    private final File file;
    private final Properties properties;

    private final String databasePath;
    private final boolean didFileExist;

    public DatabaseCredentialsProcessor(File file, String databasePath) {
        this.file = file;
        this.properties = new Properties();

        this.databasePath = databasePath.endsWith(".") ? databasePath : databasePath + ".";

        this.didFileExist = file.exists();
        if (!didFileExist) {
            createPropertiesFileAndSetDefaults();
            return;
        }

        try (InputStream inputStream = Files.newInputStream(file.toPath())) {
            properties.load(inputStream);
        } catch (IOException ex) {
            // This should never occur, as we already checked if the file does not exist
            ex.printStackTrace();
        }
    }

    private void createPropertiesFileAndSetDefaults() {
        try {
            file.createNewFile();

            setProperty(HOST_PATH);
            setProperty(PORT_PATH);
            setProperty(DATABASE_PATH);
            setProperty(USERNAME_PATH);
            setProperty(PASSWORD_PATH);

            try (FileWriter fileWriter = new FileWriter(file)) {
                properties.store(fileWriter, "Auto-generated, configure this file for database connections");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void setProperty(String secondaryPath) {
        properties.setProperty(databasePath + secondaryPath, secondaryPath.toUpperCase());
    }

    public boolean didFileExistOnStartup() {
        return didFileExist;
    }

    private String getProperty(String secondaryPath) {
        return properties.getProperty(databasePath + secondaryPath);
    }

    public String getDatabaseConnectionUrl() {
        return String.format(DATABASE_CONNECTION_URL_FORMAT, getHost(), getPort(), getDatabase());
    }

    public String getHost() {
        return getProperty(HOST_PATH);
    }

    public String getPort() {
        return getProperty(PORT_PATH);
    }

    public String getDatabase() {
        return getProperty(DATABASE_PATH);
    }

    public String getUsername() {
        return getProperty(USERNAME_PATH);
    }

    public String getPassword() {
        return getProperty(PASSWORD_PATH);
    }

}
