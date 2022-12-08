package com.github.spygameserver.database.table;

import com.github.spygameserver.database.ConnectionHandler;
import com.github.spygameserver.player.account.AccountVerificationStatus;
import com.github.spygameserver.player.account.PlayerAccountData;
import com.github.spygameserver.player.account.PlayerVerificationData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class PlayerAccountTable extends AbstractTable {

    private static final TableType TABLE_TYPE = TableType.PLAYER_ACCOUNT;

    private static final String CREATE_TABLE_QUERY = "CREATE TABLE IF NOT EXISTS %s (player_id INT NOT NULL" +
            " AUTO_INCREMENT, email VARCHAR(60) NOT NULL, username VARCHAR(16), verification_status ENUM %s" +
            " NOT NULL, PRIMARY KEY (player_id), UNIQUE (email), UNIQUE (username))";

    private static final String DOES_USERNAME_EXIST_QUERY = "SELECT 1 FROM %s WHERE username=?";
    private static final String DOES_EMAIL_EXIST_QUERY = "SELECT 1 FROM %s WHERE email=?";

    private static final String INSERT_INTO_QUERY = "INSERT INTO %s (email, username, verification_status) VALUES " +
            "(?, ?, ?)";
    private static final String UPDATE_DISABLED_ACCOUNT = "UPDATE %s SET username=?, verification_status=? WHERE player_id=?";

    private static final String UPDATE_VERIFICATION_STATUS_QUERY = "UPDATE %s SET verification_status=? WHERE player_id=?";

    private static final String PLAYER_ACCOUNT_DATA_BY_ID_QUERY = "SELECT * FROM %s WHERE player_id=?";
    private static final String PLAYER_ACCOUNT_DATA_BY_EMAIL_QUERY = "SELECT * FROM %s WHERE email=?";

    private static final String GET_ID_BY_USERNAME_QUERY = "SELECT player_id FROM %s WHERE username=?";

    private static final String PLAYER_VERIFICATION_DATA_QUERY = "SELECT player_id, verification_status FROM %s" +
            " WHERE username=?";

    public PlayerAccountTable() {
        super(TABLE_TYPE);
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
    public int createPlayerAccount(ConnectionHandler connectionHandler, String email, String username) {
        Connection connection = connectionHandler.getConnection();
        String insertIntoQuery = formatQuery(INSERT_INTO_QUERY);

        int playerId = -1;

        try (PreparedStatement preparedStatement = connection.prepareStatement(insertIntoQuery, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, email);
            preparedStatement.setString(2, username);
            preparedStatement.setString(3, AccountVerificationStatus.AWAITING_VERIFICATION.name());

            preparedStatement.executeUpdate();

            ResultSet resultSet = preparedStatement.getGeneratedKeys();
            if (resultSet.next()) {
                playerId = resultSet.getInt(1);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        connectionHandler.closeConnectionIfNecessary();
        return playerId;
    }

    public void updateDisabledAccount(ConnectionHandler connectionHandler, String username, int playerId) {
        Connection connection = connectionHandler.getConnection();
        String updateDisabledAccountQuery = formatQuery(UPDATE_DISABLED_ACCOUNT);

        try (PreparedStatement preparedStatement = connection.prepareStatement(updateDisabledAccountQuery)) {
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, AccountVerificationStatus.AWAITING_VERIFICATION.name());
            preparedStatement.setInt(3, playerId);

            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        connectionHandler.closeConnectionIfNecessary();
    }

    public void updatePlayerVerificationStatus(ConnectionHandler connectionHandler,
                                               AccountVerificationStatus accountVerificationStatus, int playerId) {
        Connection connection = connectionHandler.getConnection();
        String updateVerificationStatusQuery = formatQuery(UPDATE_VERIFICATION_STATUS_QUERY);

        try (PreparedStatement preparedStatement = connection.prepareStatement(updateVerificationStatusQuery)) {
            preparedStatement.setString(1, accountVerificationStatus.name());
            preparedStatement.setInt(2, playerId);

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
                if (resultSet.next()) {
                    int playerId = resultSet.getInt(1);
                    String email = resultSet.getString(2);
                    String username = resultSet.getString(3);

                    String nameOfAccountVerificationStatus = resultSet.getString(4);
                    AccountVerificationStatus accountVerificationStatus = AccountVerificationStatus
                            .valueOf(nameOfAccountVerificationStatus);

                    playerAccountData = new PlayerAccountData(playerId, email, username, accountVerificationStatus);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        connectionHandler.closeConnectionIfNecessary();
        return playerAccountData;
    }

    public Integer getPlayerIdByUsername(ConnectionHandler connectionHandler, String username) {
        Connection connection = connectionHandler.getConnection();
        String getPlayerIdByUsernameQuery = formatQuery(GET_ID_BY_USERNAME_QUERY);

        Integer playerId = null;

        try (PreparedStatement preparedStatement = connection.prepareStatement(getPlayerIdByUsernameQuery)) {
            preparedStatement.setString(1, username);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    playerId = resultSet.getInt(1);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        connectionHandler.closeConnectionIfNecessary();
        return playerId;
    }

    public PlayerVerificationData getPlayerVerificationInfo(ConnectionHandler connectionHandler, String username) {
        Connection connection = connectionHandler.getConnection();
        String playerVerificationDataQuery = formatQuery(PLAYER_VERIFICATION_DATA_QUERY);

        PlayerVerificationData playerVerificationData = null;

        try (PreparedStatement preparedStatement = connection.prepareStatement(playerVerificationDataQuery)) {
            preparedStatement.setString(1, username);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    int playerId = resultSet.getInt(1);

                    String nameOfAccountVerificationStatus = resultSet.getString(2);
                    AccountVerificationStatus accountVerificationStatus = AccountVerificationStatus
                            .valueOf(nameOfAccountVerificationStatus);

                    playerVerificationData = new PlayerVerificationData(playerId, accountVerificationStatus);
                }
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
