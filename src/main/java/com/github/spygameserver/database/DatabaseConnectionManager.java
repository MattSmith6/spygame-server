package com.github.spygameserver.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * A class used to create new connections that are pooled by HikariCP. This class must be closed after the program
 * executes to prevent resources from leaking, as HikariCP pools must be closed internally.
 */
public class DatabaseConnectionManager implements AutoCloseable {

    private final HikariDataSource hikariDataSource;

    public DatabaseConnectionManager(HikariConfig hikariConfig) {
        this.hikariDataSource = new HikariDataSource(hikariConfig);
    }

    public Connection createNewConnection() throws SQLException {
        return hikariDataSource.getConnection();
    }

    @Override
    public void close() {
        hikariDataSource.close();
    }

}
