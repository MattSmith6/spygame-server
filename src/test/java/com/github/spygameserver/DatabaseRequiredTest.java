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

public interface DatabaseRequiredTest {

    default <T extends AbstractDatabase> T getDatabase(DatabaseType databaseType,
                                                       Function<DatabaseConnectionManager, T> databaseConstructor) {
        DatabaseCreator<T> databaseCreator = new DatabaseCreator<>(databaseType, true);
        return databaseCreator.createDatabase(databaseConstructor);
    }

    default GameDatabase getGameDatabase() {
        TableType.setUseTestTables(true);
        return getDatabase(DatabaseType.GAME, GameDatabase::new);
    }

    default AuthenticationDatabase getAuthenticationDatabase() {
        TableType.setUseTestTables(true);
        return getDatabase(DatabaseType.AUTHENTICATION, AuthenticationDatabase::new);
    }

    void closeOpenConnections();

    default void closeOpenConnections(ConnectionHandler connectionHandler) {
        connectionHandler.closeAbsolutely();
    }

    default void closeOpenConnections(AutoCloseable... autoCloseables) {
        ExceptionHandling.closeQuietly(autoCloseables);
    }

}
