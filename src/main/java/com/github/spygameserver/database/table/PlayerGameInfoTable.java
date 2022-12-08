package com.github.spygameserver.database.table;

import com.github.spygameserver.database.ConnectionHandler;
import org.json.JSONObject;
import org.json.JSONArray;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * A class used to insert, update, and select data for the player game info table in the game database.
 */
public class PlayerGameInfoTable extends AbstractTable {

    private static final TableType TABLE_TYPE = TableType.PLAYER_GAME_INFO;

    private static final String CREATE_TABLE_QUERY = "CREATE TABLE IF NOT EXISTS %s (record_id INT NOT NULL " +
            "AUTO_INCREMENT, player_id INT NOT NULL, " +
            "current_game_id INT, games_won INT, games_played INT, points_earned INT, eliminations_won INT, " +
            "eliminations_failed INT, eliminations_total INT, PRIMARY KEY (record_id), " +
            "FOREIGN KEY (player_id) REFERENCES %s (player_id), " +
            "FOREIGN KEY (current_game_id) REFERENCES %s (game_id) ON UPDATE CASCADE)";

    private static final String GET_CURRENT_NUMBER = "SELECT %s FROM %s WHERE player_id = ?";

    private static final String INSERT_INTO_QUERY = "INSERT INTO %s (player_id)" +
            " VALUES (?)";

    private static final String UPDATE_NUMBER = "UPDATE %s SET %s=? WHERE player_id=?";

    // Where %1$s is the reusable replacement for player account table, %2$s is replacement for player game info table
    private static final String GET_LEADERBOARD_QUERY = "SELECT %1$s.username," +
            "%2$s.points_earned FROM %2$s\n" +
            "INNER JOIN %1$s ON %2$s.player_id = %1$s.player_id\n" +
            "ORDER BY %2$s.points_earned DSC \n" +
            "LIMIT ?";


    public PlayerGameInfoTable() {
        super(TABLE_TYPE);
    }

    @Override
    protected void createTableIfNotExists(ConnectionHandler connectionHandler) {
        Connection connection = connectionHandler.getConnection();
        String createTableQuery = String.format(CREATE_TABLE_QUERY, getTableName(), TableType.PLAYER_ACCOUNT.getTableName(),
                TableType.GAME_LOBBY.getTableName());

        try (PreparedStatement preparedStatement = connection.prepareStatement(createTableQuery)) {
            preparedStatement.execute();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        connectionHandler.closeConnectionIfNecessary();
    }

    public int getCurrentNumber(ConnectionHandler connectionHandler, String field, int playerID) {
        Connection connection = connectionHandler.getConnection();
        String selectOneQuery = String.format(GET_CURRENT_NUMBER, field, getTableName());

        int number = -1;

        try (PreparedStatement preparedStatement = connection.prepareStatement(selectOneQuery)) {
            preparedStatement.setInt(1, playerID);

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
        String updateUsernameQuery = String.format(UPDATE_NUMBER, getTableName(), field);

        try (PreparedStatement preparedStatement = connection.prepareStatement(updateUsernameQuery)) {
            preparedStatement.setInt(1, number);
            preparedStatement.setInt(2, player_id);

            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void updateCurrentGameId(ConnectionHandler connectionHandler, int gameId, int playerId) {
        disableKeyChecks(connectionHandler);
        updateNumber(connectionHandler, "current_game_id", gameId, playerId);
        enableKeyChecks(connectionHandler);
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
        String insertIntoQuery = String.format(GET_LEADERBOARD_QUERY, TableType.PLAYER_ACCOUNT.getTableName(), getTableName());

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