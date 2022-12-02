package com.github.spygameserver.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

// For now, this class is designed as a way to open up connections. If there is time, this may include a way
// to open a connection pool, where connections are shared between a certain number of threads
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
