package com.github.spygameserver.database.table;

import com.github.glusk.srp6_variables.SRP6CustomIntegerVariable;
import com.github.spygameserver.auth.PlayerAuthenticationData;
import com.github.spygameserver.database.ConnectionHandler;

import java.nio.ByteOrder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AuthenticationTable extends AbstractTable {

    private static final String NON_TESTING_TABLE_NAME = "player_authentication";

    private static final String CREATE_TABLE_QUERY = "CREATE TABLE IF NOT EXISTS %s (player_id INT NOT NULL, " +
            "salt BINARY(16) NOT NULL, verifier BINARY(128) NOT NULL, PRIMARY KEY (player_id))";

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
            preparedStatement.setBytes(2, playerAuthenticationData.getSaltByteArray());
            preparedStatement.setBytes(3, playerAuthenticationData.getVerifierByteArray());

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
            preparedStatement.setBytes(1, playerAuthenticationData.getSaltByteArray());
            preparedStatement.setBytes(2, playerAuthenticationData.getVerifierByteArray());
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
                    SRP6CustomIntegerVariable salt = new SRP6CustomIntegerVariable(resultSet.getBytes(1), ByteOrder.BIG_ENDIAN);
                    SRP6CustomIntegerVariable verifier = new SRP6CustomIntegerVariable(resultSet.getBytes(2), ByteOrder.BIG_ENDIAN);

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
