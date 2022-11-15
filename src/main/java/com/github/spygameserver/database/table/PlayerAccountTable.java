package com.github.spygameserver.database.table;

import com.github.spygameserver.database.ConnectionHandler;
import com.github.spygameserver.player.account.AccountVerificationStatus;
import com.github.spygameserver.player.account.PlayerAccountData;
import com.github.spygameserver.player.account.PlayerVerificationData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PlayerAccountTable extends AbstractTable {

    private static final String NON_TESTING_TABLE_NAME = "player_account";

    private static final String CREATE_TABLE_QUERY = "CREATE TABLE IF NOT EXISTS %s (player_id INT NOT NULL" +
            " AUTO_INCREMENT, email VARCHAR(60) NOT NULL, username VARCHAR(16), verification_status ENUM %s" +
            " NOT NULL, PRIMARY KEY (player_id), UNIQUE (email))";

    private static final String DOES_USERNAME_EXIST_QUERY = "SELECT 1 FROM %s WHERE username=?";
    private static final String DOES_EMAIL_EXIST_QUERY = "SELECT 1 FROM %s WHERE email=?";

    private static final String INSERT_INTO_QUERY = "INSERT INTO %s (email, verification_status) VALUES (?, ?)";
    private static final String UPDATE_USERNAME_QUERY = "UPDATE %s SET username=?, verification_status=? WHERE email=?";

    private static final String PLAYER_ACCOUNT_DATA_BY_ID_QUERY = "SELECT * FROM %s WHERE player_id=?";
    private static final String PLAYER_ACCOUNT_DATA_BY_EMAIL_QUERY = "SELECT * FROM %s WHERE email=?";

    private static final String PLAYER_VERIFICATION_DATA_QUERY = "SELECT player_id, verification_status FROM %s" +
            " WHERE username=?";

    public PlayerAccountTable(boolean useTestTables) {
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

    public boolean doesUsernameAlreadyExist(ConnectionHandler connectionHandler, String username) {
        return doesUniquePropertyAlreadyExist(connectionHandler, DOES_USERNAME_EXIST_QUERY, username);
    }

    public boolean doesEmailAlreadyExist(ConnectionHandler connectionHandler, String email) {
        return doesUniquePropertyAlreadyExist(connectionHandler, DOES_EMAIL_EXIST_QUERY, email);
    }

    private boolean doesUniquePropertyAlreadyExist(ConnectionHandler connectionHandler, String query, String property) {
        Connection connection = connectionHandler.getConnection();
        String selectOneQuery = formatQuery(query);

        boolean result = false;

        try (PreparedStatement preparedStatement = connection.prepareStatement(selectOneQuery)) {
            preparedStatement.setString(1, property);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                // If there is a result, then this property does exist
                result = resultSet.next();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        connectionHandler.closeConnectionIfNecessary();
        return result;
    }

    // Sets the player's account with no username, the user should select a username and password for next step
    public void addVerifiedEmail(ConnectionHandler connectionHandler, String email) {
        Connection connection = connectionHandler.getConnection();
        String insertIntoQuery = formatQuery(INSERT_INTO_QUERY);

        try (PreparedStatement preparedStatement = connection.prepareStatement(insertIntoQuery)) {
            preparedStatement.setString(1, email);
            preparedStatement.setString(2, AccountVerificationStatus.CHOOSE_USERNAME.name());

            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        connectionHandler.closeConnectionIfNecessary();
    }

    // Sets the player's username associated with their email, authentication information is generated at this step also
    public void addUsernameToPlayerAccount(ConnectionHandler connectionHandler, String email, String username) {
        Connection connection = connectionHandler.getConnection();
        String updateUsernameQuery = formatQuery(UPDATE_USERNAME_QUERY);

        try (PreparedStatement preparedStatement = connection.prepareStatement(updateUsernameQuery)) {
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, AccountVerificationStatus.VERIFIED.name());
            preparedStatement.setString(3, email);

            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        connectionHandler.closeConnectionIfNecessary();
    }

    public PlayerAccountData getPlayerAccountData(ConnectionHandler connectionHandler, int playerId) {
        return getPlayerAccountData(connectionHandler, PLAYER_ACCOUNT_DATA_BY_ID_QUERY,
                preparedStatement -> preparedStatement.setInt(1, playerId));
    }

    public PlayerAccountData getPlayerAccountDataByEmail(ConnectionHandler connectionHandler, String email) {
        return getPlayerAccountData(connectionHandler, PLAYER_ACCOUNT_DATA_BY_EMAIL_QUERY,
                preparedStatement -> preparedStatement.setString(1, email));
    }

    private PlayerAccountData getPlayerAccountData(ConnectionHandler connectionHandler, String query,
                                                   SQLStatementConsumer sqlStatementConsumer) {
        Connection connection = connectionHandler.getConnection();
        String playerAccountDataQuery = formatQuery(query);

        PlayerAccountData playerAccountData = null;

        try (PreparedStatement preparedStatement = connection.prepareStatement(playerAccountDataQuery)) {
            sqlStatementConsumer.consume(preparedStatement);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                // If no record exists, we should return a null object
                if (!resultSet.next()) {
                    return null;
                }

                int playerId = resultSet.getInt(1);
                String email = resultSet.getString(2);
                String username = resultSet.getString(3);

                String nameOfAccountVerificationStatus = resultSet.getString(4);
                AccountVerificationStatus accountVerificationStatus = AccountVerificationStatus
                        .valueOf(nameOfAccountVerificationStatus);

                playerAccountData = new PlayerAccountData(playerId, email, username, accountVerificationStatus);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        connectionHandler.closeConnectionIfNecessary();
        return playerAccountData;
    }

    public PlayerVerificationData getPlayerVerificationInfo(ConnectionHandler connectionHandler, String username) {
        Connection connection = connectionHandler.getConnection();
        String playerVerificationDataQuery = formatQuery(PLAYER_VERIFICATION_DATA_QUERY);

        PlayerVerificationData playerVerificationData = null;

        try (PreparedStatement preparedStatement = connection.prepareStatement(playerVerificationDataQuery)) {
            preparedStatement.setString(1, username);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                // If no record exists, we should return a null object
                if (!resultSet.next()) {
                    return null;
                }

                int playerId = resultSet.getInt(1);

                String nameOfAccountVerificationStatus = resultSet.getString(2);
                AccountVerificationStatus accountVerificationStatus = AccountVerificationStatus
                        .valueOf(nameOfAccountVerificationStatus);

                playerVerificationData = new PlayerVerificationData(playerId, accountVerificationStatus);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        connectionHandler.closeConnectionIfNecessary();
        return playerVerificationData;
    }

    private interface SQLStatementConsumer {

        void consume(PreparedStatement preparedStatement) throws SQLException;

    }

}
