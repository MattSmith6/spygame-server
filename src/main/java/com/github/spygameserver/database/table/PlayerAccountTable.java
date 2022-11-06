package com.github.spygameserver.database.table;

import com.github.spygameserver.database.ConnectionHandler;
import com.github.spygameserver.player.account.AccountVerificationStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PlayerAccountTable extends AbstractTable {

    private static final String NON_TESTING_TABLE_NAME = "player_account";

    private static final String CREATE_TABLE_QUERY = "CREATE TABLE IF NOT EXISTS %s (player_id INT NOT NULL" +
            " AUTO_INCREMENT, username VARCHAR(16), email VARCHAR(60) NOT NULL, verification_status ENUM %s," +
            " PRIMARY KEY (player_id))";

    public PlayerAccountTable(boolean useTestTables) {
        super(NON_TESTING_TABLE_NAME, useTestTables);
    }

    @Override
    protected boolean createTableIfNotExists(ConnectionHandler connectionHandler) {
        Connection connection = connectionHandler.getConnection();
        String createTableQuery = String.format(CREATE_TABLE_QUERY, NON_TESTING_TABLE_NAME,
                AccountVerificationStatus.toSQLStringifiedEnum());

        try (PreparedStatement preparedStatement = connection.prepareStatement(createTableQuery)) {
            return preparedStatement.execute();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return false;
    }



}
