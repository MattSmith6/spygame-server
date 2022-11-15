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
            "current_players INT, start_time BIGINT, end_time BIGINT, PRIMARY KEY (game_id), UNIQUE (invite_code))";

    private static final String GAME_FROM_INVITE_CODE_QUERY = "SELECT game_id, start_time FROM %s WHERE invite_code=?";

    private static final String INSERT_INTO_QUERY = "INSERT INTO %s (invite_code, is_public, game_type, " +
            "max_players, current_players) VALUES (?, ?, ?, ?, ?)";

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

    public Pair<Integer, Long> getGameIdFromInviteCode(ConnectionHandler connectionHandler, String invite_code) {
        Connection connection = connectionHandler.getConnection();
        String selectOneQuery = formatQuery(GAME_FROM_INVITE_CODE_QUERY);

        Integer gameId = null;
        Long startTime = null;

        try (PreparedStatement preparedStatement = connection.prepareStatement(selectOneQuery)) {
            preparedStatement.setString(1, invite_code);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                // If there is a result, then this property does exist
                if(resultSet.next()) {
                    gameId = resultSet.getInt(1);
                    startTime = resultSet.getLong(2);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        connectionHandler.closeConnectionIfNecessary();
        return new Pair<>(gameId, startTime);
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
            preparedStatement.setInt(5, 0);

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

    public void updateStartTime(ConnectionHandler connectionHandler, long start_time) {
        Connection connection = connectionHandler.getConnection();
        String updateUsernameQuery = formatQuery(UPDATE_START_TIME);

        try (PreparedStatement preparedStatement = connection.prepareStatement(updateUsernameQuery)) {
            preparedStatement.setLong(1, start_time);

            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void updateEndTime(ConnectionHandler connectionHandler, long end_time) {
        Connection connection = connectionHandler.getConnection();
        String updateUsernameQuery = formatQuery(UPDATE_END_TIME);

        try (PreparedStatement preparedStatement = connection.prepareStatement(updateUsernameQuery)) {
            preparedStatement.setLong(1, end_time);

            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public static class Pair<L, R> {

        private final L l;
        private final R r;

        public Pair(L l, R r) {
            this.l = l;
            this.r = r;
        }

        public L getL() {
            return l;
        }

        public R getR() {
            return r;
        }

    }

}