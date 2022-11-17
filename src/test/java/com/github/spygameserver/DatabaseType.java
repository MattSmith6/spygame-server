package com.github.spygameserver;

public enum DatabaseType {

    GAME("game_db"),
    AUTHENTICATION("auth_db");

    private final String databasePath;

    DatabaseType(String databasePath) {
        this.databasePath = databasePath;
    }

    public String getDatabasePath() {
        return databasePath;
    }
}
