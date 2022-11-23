package com.github.spygameserver.database;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Properties;

public class DatabaseCredentialsProcessor {

    private static final String DATABASE_CONNECTION_URL_FORMAT = "jdbc:mysql://%s:%s/%s?useSSL=true&trustCertificateKeyStoreUrl=file:\\%s";

    private static final String HOST_PATH = "host";
    private static final String PORT_PATH = "port";
    private static final String DATABASE_PATH = "database";

    private static final String USERNAME_PATH = "username";
    private static final String PASSWORD_PATH = "password";

    private final File credentialsFile;
    private final Properties properties;

    private final String databasePath;
    private final boolean didFileExist;

    private final String certificatePath;

    public DatabaseCredentialsProcessor(File credentialsFile, String databasePath, File certificate) {
        this.credentialsFile = credentialsFile;
        this.properties = new Properties();

        this.databasePath = databasePath.endsWith(".") ? databasePath : databasePath + ".";

        this.didFileExist = credentialsFile.exists();
        this.certificatePath = certificate.getAbsolutePath();

        if (!didFileExist) {
            createPropertiesFileAndSetDefaults();
            return;
        }

        try (InputStream inputStream = Files.newInputStream(credentialsFile.toPath())) {
            properties.load(inputStream);
        } catch (IOException ex) {
            // This should never occur, as we already checked if the file does not exist
            ex.printStackTrace();
        }

        if (!certificate.exists()) {
            throw new IllegalStateException("Could not find certificate file to connect to remote SQL database.");
        }
    }

    private void createPropertiesFileAndSetDefaults() {
        try {
            credentialsFile.createNewFile();
            setFileDefaults();

            try (FileWriter fileWriter = new FileWriter(credentialsFile)) {
                properties.store(fileWriter, "Auto-generated, configure this file for database connections");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void setFileDefaults() {
        setProperty(HOST_PATH);
        setProperty(PORT_PATH);
        setProperty(DATABASE_PATH);
        setProperty(USERNAME_PATH);
        setProperty(PASSWORD_PATH);
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
        return String.format(DATABASE_CONNECTION_URL_FORMAT, getHost(), getPort(), getDatabase(), certificatePath);
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
