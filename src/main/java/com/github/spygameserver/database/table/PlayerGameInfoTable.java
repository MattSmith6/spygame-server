package com.github.spygameserver.database.table;

import com.github.spygameserver.database.ConnectionHandler;
import com.github.spygameserver.player.account.AccountVerificationStatus;
import com.github.spygameserver.player.account.PlayerAccountData;
import com.github.spygameserver.player.account.PlayerVerificationData;
import org.json.JSONObject;
import org.json.JSONArray;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PlayerGameInfoTable extends AbstractTable {

    private static final String NON_TESTING_TABLE_NAME = "player_game_info";

    private static final String CREATE_TABLE_QUERY = "CREATE TABLE IF NOT EXISTS %s (player_id INT NOT NULL, " +
            "current_game_id INT, games_won INT, games_played INT, points_earned INT, eliminations_won INT, " +
            "eliminations_failed INT, eliminations_total INT, FOREIGN KEY (player_id), " +
            "FOREIGN KEY (current_game_id)";

    private static final String GET_CURRENT_NUMBER = "SELECT ? FROM %s WHERE player_id = ?";

    private static final String INSERT_INTO_QUERY = "INSERT INTO %s (player_id)" +
            "VALUES (?)";

    private static final String UPDATE_NUMBER = "UPDATE %s SET ?=? WHERE player_id=?";

    private static final String GET_LEADERBOARD_QUERY = "SELECT player_account.username," +
            "player_game_info.points_earned FROM player_game_info\n" +
            "INNER JOIN player_account ON player_game_info.player_id = player_account.player_id\n" +
            "ORDER BY player_game_info.points_earned DSC \n" +
            "LIMIT ?";


    public PlayerGameInfoTable(boolean useTestTables) {
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

    public int getCurrentNumber(ConnectionHandler connectionHandler, String query, int playerID) {
        Connection connection = connectionHandler.getConnection();
        String selectOneQuery = formatQuery(GET_CURRENT_NUMBER);

        int number = -1;

        try (PreparedStatement preparedStatement = connection.prepareStatement(selectOneQuery)) {
            preparedStatement.setString(1, query);
            preparedStatement.setInt(2, playerID);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                // If there is a result, then this property does exist
                if (resultSet.next()) {
                    number = resultSet.getInt(1);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        connectionHandler.closeConnectionIfNecessary();
        return number;
    }

    public void updateNumber(ConnectionHandler connectionHandler, String field, int number,
                             int player_id) {
        Connection connection = connectionHandler.getConnection();
        String updateUsernameQuery = formatQuery(UPDATE_NUMBER);

        try (PreparedStatement preparedStatement = connection.prepareStatement(updateUsernameQuery)) {
            preparedStatement.setString(1, field);
            preparedStatement.setInt(2, number);
            preparedStatement.setInt(3, player_id);

            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void createPlayer(ConnectionHandler connectionHandler, int player_id) {
        Connection connection = connectionHandler.getConnection();
        String insertIntoQuery = formatQuery(INSERT_INTO_QUERY);

        try (PreparedStatement preparedStatement = connection.prepareStatement(insertIntoQuery)) {
            preparedStatement.setInt(1, player_id);

            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }


public JSONObject getLeaderboard(ConnectionHandler connectionHandler, int lbSize) {
    Connection connection = connectionHandler.getConnection();
    String insertIntoQuery = formatQuery(GET_LEADERBOARD_QUERY);

    JSONObject leaderboard = new JSONObject();
    JSONArray usernames = new JSONArray();
    JSONArray scores = new JSONArray();

    try (PreparedStatement preparedStatement = connection.prepareStatement(insertIntoQuery)) {
        preparedStatement.setInt(1, lbSize);

        try (ResultSet resultSet = preparedStatement.executeQuery()) {
            // If there is a result, then this property does exist
            while (resultSet.next()) {
                usernames.put(resultSet.getString(1));
                scores.put(resultSet.getInt(2));
            }

            leaderboard.put("Usernames", usernames);
            leaderboard.put("Scores", scores);
        }
    } catch (SQLException ex) {
        ex.printStackTrace();


    }
    return leaderboard;
}
}