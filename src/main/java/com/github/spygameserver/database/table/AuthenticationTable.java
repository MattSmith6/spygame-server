package com.github.spygameserver.database.table;

import com.github.spygameserver.auth.PlayerAuthenticationData;
import com.github.spygameserver.database.ConnectionHandler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AuthenticationTable extends AbstractTable {

    private static final String NON_TESTING_TABLE_NAME = "player_authentication";

    private static final String CREATE_TABLE_QUERY = "CREATE TABLE IF NOT EXISTS %s (player_id INT NOT NULL, " +
            "salt CHAR(32) NOT NULL, verifier TEXT(256) NOT NULL, PRIMARY KEY (player_id))";

    private static final String INSERT_INTO_QUERY = "INSERT INTO %s VALUES (?, ?, ?)";
    private static final String UPDATE_QUERY = "UPDATE %s SET salt=?, verifier=? WHERE player_id=?";
    private static final String SELECT_QUERY = "SELECT salt, verifier FROM %s WHERE player_id=?";

    public AuthenticationTable(boolean useTestTables) {
        super(NON_TESTING_TABLE_NAME, useTestTables);
    }

    @Override
    public void createTableIfNotExists(ConnectionHandler connectionHandler) {
        Connection connection = connectionHandler.getConnection();
        String createTableIfNotExistsQuery = formatQuery(CREATE_TABLE_QUERY);

        try (PreparedStatement preparedStatement = connection.prepareStatement(createTableIfNotExistsQuery)) {
            preparedStatement.execute();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        connectionHandler.closeConnectionIfNecessary();
    }

    public void addPlayerAuthenticationRecord(ConnectionHandler connectionHandler, PlayerAuthenticationData playerAuthenticationData) {
        Connection connection = connectionHandler.getConnection();
        String insertPlayerRecordQuery = formatQuery(INSERT_INTO_QUERY);

        try (PreparedStatement preparedStatement = connection.prepareStatement(insertPlayerRecordQuery)) {
            preparedStatement.setInt(1, playerAuthenticationData.getPlayerId());
            preparedStatement.setString(2, playerAuthenticationData.getSalt());
            preparedStatement.setString(3, playerAuthenticationData.getVerifier());

            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        connectionHandler.closeConnectionIfNecessary();
    }

    public void updatePlayerAuthenticationRecord(ConnectionHandler connectionHandler, PlayerAuthenticationData playerAuthenticationData) {
        Connection connection = connectionHandler.getConnection();
        String updatePlayerRecordTheory = formatQuery(UPDATE_QUERY);

        try (PreparedStatement preparedStatement = connection.prepareStatement(updatePlayerRecordTheory)) {
            preparedStatement.setString(1, playerAuthenticationData.getSalt());
            preparedStatement.setString(2, playerAuthenticationData.getVerifier());
            preparedStatement.setInt(3, playerAuthenticationData.getPlayerId());

            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        connectionHandler.closeConnectionIfNecessary();
    }

    public PlayerAuthenticationData getPlayerAuthenticationRecord(ConnectionHandler connectionHandler, int playerId) {
        Connection connection = connectionHandler.getConnection();
        String selectSaltAndVerifierQuery = formatQuery(SELECT_QUERY);

        PlayerAuthenticationData playerAuthenticationData = null;

        try (PreparedStatement preparedStatement = connection.prepareStatement(selectSaltAndVerifierQuery)) {
            preparedStatement.setInt(1, playerId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    String salt = resultSet.getString(1);
                    String verifier = resultSet.getString(2);

                    playerAuthenticationData = new PlayerAuthenticationData(playerId, salt, verifier);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        connectionHandler.closeConnectionIfNecessary();
        return playerAuthenticationData;
    }

}
