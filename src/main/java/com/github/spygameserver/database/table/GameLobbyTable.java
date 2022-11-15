package com.github.spygameserver.database.table;

import com.github.spygameserver.database.ConnectionHandler;
import com.github.spygameserver.player.account.AccountVerificationStatus;
import com.github.spygameserver.player.account.PlayerAccountData;
import com.github.spygameserver.player.account.PlayerVerificationData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class GameLobbyTable extends AbstractTable {

    private static final String NON_TESTING_TABLE_NAME = "game_lobby";

    private static final String CREATE_TABLE_QUERY = "CREATE TABLE IF NOT EXISTS %s (game_id INT NOT NULL" +
            "AUTO_INCREMENT, invite_code CHAR(6), is_public INT, game_type ENUM, max_players INT," +
            "current_players INT, start_time INT, end_time INT, PRIMARY KEY (game_id), UNIQUE (invite_code))";

    private static final String DOES_INVITE_CODE_EXIST_QUERY = "SELECT 1 FROM %s WHERE invite_code=?";

    private static final String INSERT_INTO_QUERY = "INSERT INTO %s (invite_code, is_public, game_type, " +
            "max_players, current_players, start_time, end_time) VALUES (?, ?, ?, ?, 0, -1, -1)";

    private static final String UPDATE_CURRENT_PLAYERS_QUERY = "UPDATE %s SET current_players=? WHERE game_id=?";
    private static final String UPDATE_START_TIME = "UPDATE %s SET start_time=? WHERE game_id=?";
    private static final String UPDATE_END_TIME = "UPDATE %s SET end_time=? WHERE game_id=?";


    public GameLobbyTable(boolean useTestTables) {
        super(NON_TESTING_TABLE_NAME, useTestTables);
    }

    @Override
    protected void createTableIfNotExists(ConnectionHandler connectionHandler) {
        Connection connection = connectionHandler.getConnection();
        String createTableQuery = String.format(CREATE_TABLE_QUERY, getTableName(),
                AccountVerificationStatus.toSQLStringifiedEnum());

        try (PreparedStatement preparedStatement = connection.prepareStatement(createTableQuery)) {
            preparedStatement.execute();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        connectionHandler.closeConnectionIfNecessary();
    }

    public boolean checkInviteCode(ConnectionHandler connectionHandler, String invite_code) {
        return doesUniquePropertyAlreadyExist(connectionHandler, DOES_INVITE_CODE_EXIST_QUERY, invite_code);
    }

    private boolean doesUniquePropertyAlreadyExist(ConnectionHandler connectionHandler, String query, String property) {
        Connection connection = connectionHandler.getConnection();
        String selectOneQuery = formatQuery(query);

        try (PreparedStatement preparedStatement = connection.prepareStatement(selectOneQuery)) {
            preparedStatement.setString(1, property);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                // If there is a result, then this property does exist
                return resultSet.next();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return false;
    }

    public void createGame(ConnectionHandler connectionHandler, String invite_code, int is_public,
                           int game_type, int max_players) {
        Connection connection = connectionHandler.getConnection();
        String insertIntoQuery = formatQuery(INSERT_INTO_QUERY);

        try (PreparedStatement preparedStatement = connection.prepareStatement(insertIntoQuery)) {
            preparedStatement.setString(1, invite_code);
            preparedStatement.setInt(2, is_public);
            preparedStatement.setInt(3, game_type);
            preparedStatement.setInt(4, max_players);

            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void checkInviteCode(ConnectionHandler connectionHandler, String invite_code) {
        Connection connection = connectionHandler.getConnection();
        String updateUsernameQuery = formatQuery(DOES_INVITE_CODE_EXIST_QUERY);

        try (PreparedStatement preparedStatement = connection.prepareStatement(updateUsernameQuery)) {
            preparedStatement.setString(1, invite_code);

            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void updateCurrentPlayers(ConnectionHandler connectionHandler, int current_players) {
        Connection connection = connectionHandler.getConnection();
        String updateUsernameQuery = formatQuery(UPDATE_CURRENT_PLAYERS_QUERY);

        try (PreparedStatement preparedStatement = connection.prepareStatement(updateUsernameQuery)) {
            preparedStatement.setInt(1, current_players);

            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void updateStartTime(ConnectionHandler connectionHandler, int start_time) {
        Connection connection = connectionHandler.getConnection();
        String updateUsernameQuery = formatQuery(UPDATE_START_TIME);

        try (PreparedStatement preparedStatement = connection.prepareStatement(updateUsernameQuery)) {
            preparedStatement.setInt(1, start_time);

            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void updateEndTime(ConnectionHandler connectionHandler, int end_time) {
        Connection connection = connectionHandler.getConnection();
        String updateUsernameQuery = formatQuery(UPDATE_END_TIME);

        try (PreparedStatement preparedStatement = connection.prepareStatement(updateUsernameQuery)) {
            preparedStatement.setInt(1, end_time);

            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private interface SQLStatementConsumer {

        void consume(PreparedStatement preparedStatement) throws SQLException;

    }

}