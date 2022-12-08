package com.github.spygameserver.database.table;

import com.github.glusk.caesar.Bytes;
import com.github.glusk.srp6_variables.SRP6CustomIntegerVariable;
import com.github.glusk.srp6_variables.SRP6IntegerVariable;
import com.github.spygameserver.auth.PlayerAuthenticationData;
import com.github.spygameserver.database.ConnectionHandler;

import java.math.BigInteger;
import java.nio.ByteOrder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * A class designed to insert, update, or select data from the authentication table in the authentication database.
 */
public class AuthenticationTable extends AbstractTable {

    private static final TableType TABLE_TYPE = TableType.PLAYER_AUTHENTICATION;

    private static final String CREATE_TABLE_QUERY = "CREATE TABLE IF NOT EXISTS %s (player_id INT NOT NULL, " +
            "salt BINARY(32) NOT NULL, verifier BINARY(128) NOT NULL, PRIMARY KEY (player_id))";

    private static final String INSERT_INTO_QUERY = "INSERT INTO %s VALUES (?, ?, ?)";
    private static final String UPDATE_QUERY = "UPDATE %s SET salt=?, verifier=? WHERE player_id=?";
    private static final String SELECT_QUERY = "SELECT salt, verifier FROM %s WHERE player_id=?";

    public AuthenticationTable() {
        super(TABLE_TYPE);
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
            preparedStatement.setBytes(2, playerAuthenticationData.getSalt().asArray());
            preparedStatement.setBytes(3, playerAuthenticationData.getVerifier().bytes(ByteOrder.BIG_ENDIAN).asArray());

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
            preparedStatement.setBytes(1, playerAuthenticationData.getSalt().asArray());
            preparedStatement.setBytes(2, playerAuthenticationData.getVerifier().bytes(ByteOrder.BIG_ENDIAN).asArray());
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
                    Bytes salt = Bytes.wrapped(resultSet.getBytes(1));

                    SRP6IntegerVariable verifier = new SRP6CustomIntegerVariable(Bytes.wrapped(resultSet.getBytes(2)),
                            ByteOrder.BIG_ENDIAN);
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
