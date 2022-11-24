package com.github.spygameserver.database;

import com.github.spygameserver.database.impl.AbstractDatabase;
import com.zaxxer.hikari.HikariConfig;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.BiFunction;

/**
 * Class to create a database given the appropriate credentials property file.
 */
public class DatabaseCreator<T extends AbstractDatabase> {

    private static final String RESOURCES_DIRECTORY = "hikari";

    private final DatabaseType databaseType;
    private final boolean useTestTables;

    public DatabaseCreator(DatabaseType databaseType, boolean useTestTables) {
        this.databaseType = databaseType;
        this.useTestTables = useTestTables;
    }

    public T createDatabase(BiFunction<DatabaseConnectionManager, Boolean, T> databaseConstructor) {
        HikariConfig hikariConfig = new HikariConfig(databaseType.getHikariProperties());
        hikariConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");

        DatabaseConnectionManager databaseConnectionManager = new DatabaseConnectionManager(hikariConfig);

        try (Connection connection = databaseConnectionManager.createNewConnection()) {
            // ignore, we're just seeing if a connection could be obtained at all
        } catch (SQLException ex) {
            throw new IllegalArgumentException(ex);
        }

        return databaseConstructor.apply(databaseConnectionManager, useTestTables);
    }

}
