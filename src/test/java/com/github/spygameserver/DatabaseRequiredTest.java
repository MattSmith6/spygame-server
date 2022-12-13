package com.github.spygameserver;

import com.github.spygameserver.database.ConnectionHandler;
import com.github.spygameserver.database.DatabaseConnectionManager;
import com.github.spygameserver.database.DatabaseCreator;
import com.github.spygameserver.database.DatabaseType;
import com.github.spygameserver.database.impl.AbstractDatabase;
import com.github.spygameserver.database.impl.AuthenticationDatabase;
import com.github.spygameserver.database.impl.GameDatabase;
import com.github.spygameserver.database.table.TableType;
import com.github.spygameserver.util.ExceptionHandling;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * An interface used for tests for which databases are required. Contains several functions to make access
 * to databases easier.
 */
public interface DatabaseRequiredTest {

    /**
     * Gets the database of the specified type, when provided the constructor reference for a DatabaseConnectionManager.
     * @param databaseType the type of database to be creating
     * @param databaseConstructor the function that takes a DatabaseConnectionManager and returns an AbstractDatabase of type <T>
     * @return the AbstractDatabase of type <T>
     * @param <T> the type of database to return
     */
    default <T extends AbstractDatabase> T getDatabase(DatabaseType databaseType,
                                                       Function<DatabaseConnectionManager, T> databaseConstructor) {
        DatabaseCreator<T> databaseCreator = new DatabaseCreator<>(databaseType);
        return databaseCreator.createDatabase(databaseConstructor);
    }

    /**
     * Gets a new instance of a game database setup only to use test tables
     * @return a new instance of GameDatabase
     */
    default GameDatabase getGameDatabase() {
        TableType.setUseTestTables(true);
        return getDatabase(DatabaseType.GAME, GameDatabase::new);
    }

    /**
     * Gets a new instance of authentication database setup only to use test tables
     * @return a new instance of AuthenticationDatabase
     */
    default AuthenticationDatabase getAuthenticationDatabase() {
        TableType.setUseTestTables(true);
        return getDatabase(DatabaseType.AUTHENTICATION, AuthenticationDatabase::new);
    }

    /**
     * A method that must be implemented by the subclasses. Reminds the implementor of new tests to close their
     * database/Connection references at the end of the tests.
     */
    void closeOpenConnections();

    /**
     * 
     * @param connectionHandler
     */
    default void closeOpenConnections(ConnectionHandler connectionHandler) {
        connectionHandler.closeAbsolutely();
    }

    default void closeOpenConnections(AutoCloseable... autoCloseables) {
        ExceptionHandling.closeQuietly(autoCloseables);
    }

}
