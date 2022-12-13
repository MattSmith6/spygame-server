package com.github.spygameserver.database;

import com.github.spygameserver.database.impl.AbstractDatabase;
import com.zaxxer.hikari.HikariConfig;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Class to create a database of type T given the appropriate credentials property file.
 */
public class DatabaseCreator<T extends AbstractDatabase> {

    private final DatabaseType databaseType;

    public DatabaseCreator(DatabaseType databaseType) {
        this.databaseType = databaseType;
    }

    /**
     * Creates a database of type T based off the given constructor.
     * @param databaseConstructor the constructor to produce a database of type T given a DatabaseConnectionManager
     * @return the database of type T
     */
    public T createDatabase(Function<DatabaseConnectionManager, T> databaseConstructor) {
        // Obtains a HikariConfig from the given database type and sets the driver, creates a new database connection
        HikariConfig hikariConfig = new HikariConfig(databaseType.getHikariProperties());
        hikariConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");

        DatabaseConnectionManager databaseConnectionManager = new DatabaseConnectionManager(hikariConfig);

        try (Connection connection = databaseConnectionManager.createNewConnection()) {
            // If we error here, then the connection credentials we have are invalid and we should throw an internal error
            // So that the credentials can be modified by the developer at compile time
        } catch (SQLException ex) {
            throw new IllegalArgumentException(ex);
        }

        // Returns the databse of type T
        return databaseConstructor.apply(databaseConnectionManager);
    }

}
