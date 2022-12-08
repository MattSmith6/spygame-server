package com.github.spygameserver.database.table;

import com.github.spygameserver.database.ConnectionHandler;

import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * A class designed to insert, update, and select data for the verification token table in the authentication database.
 */
public class VerificationTokenTable extends AbstractTable {

	private static final TableType TABLE_TYPE = TableType.VERIFICATION_TOKEN;
	public static final int TOKEN_LENGTH = 64;

	private static final String CREATE_TABLE_QUERY = "CREATE TABLE IF NOT EXISTS %s (id INT NOT NULL AUTO_INCREMENT, " +
			"player_id INT NOT NULL, verification_token CHAR(%s) NOT NULL, PRIMARY KEY (id), UNIQUE (verification_token))";

	private static final String INSERT_INTO_QUERY = "INSERT INTO %s (player_id, verification_token) VALUES (?, ?)";
	private static final String GET_PLAYER_ID_FROM_TOKEN_QUERY = "SELECT player_id FROM %s WHERE verification_token=?";
	private static final String GET_TOKEN_FROM_PLAYER_ID_QUERY = "SELECT verification_token FROM %s WHERE player_id=?";

	private static final String DELETE_TOKEN_QUERY = "DELETE FROM %s WHERE verification_token=?";

	private final SecureRandom secureRandom;

	public VerificationTokenTable() {
		super(TABLE_TYPE);

		this.secureRandom = new SecureRandom();
	}

	@Override
	protected void createTableIfNotExists(ConnectionHandler connectionHandler) {
		Connection connection = connectionHandler.getConnection();
		String createTableQuery = String.format(CREATE_TABLE_QUERY, getTableName(), TOKEN_LENGTH);

		try (PreparedStatement preparedStatement = connection.prepareStatement(createTableQuery)) {
			preparedStatement.execute();
		} catch (SQLException ex) {
			ex.printStackTrace();
		}

		connectionHandler.closeConnectionIfNecessary();
	}

	public String addNewVerificationTokenForPlayer(ConnectionHandler connectionHandler, int playerId) {
		Connection connection = connectionHandler.getConnection();
		String insertIntoQuery = formatQuery(INSERT_INTO_QUERY);

		String token = generateUniqueVerificationToken(connectionHandler);

		try (PreparedStatement preparedStatement = connection.prepareStatement(insertIntoQuery)) {
			preparedStatement.setInt(1, playerId);
			preparedStatement.setString(2, token);

			preparedStatement.executeUpdate();
		} catch (SQLException ex) {
			ex.printStackTrace();
		}

		connectionHandler.closeConnectionIfNecessary();
		return token;
	}

	private String generateNewVerificationToken() {
		StringBuilder stringBuilder = new StringBuilder();

		for (int i = 0; i < TOKEN_LENGTH; i++) {
			char randomCharacter = (char) ('A' + secureRandom.nextInt(27));
			stringBuilder.append(randomCharacter);
		}

		return stringBuilder.toString();
	}

	private String generateUniqueVerificationToken(ConnectionHandler connectionHandler) {
		String newToken = generateNewVerificationToken();

		// If the player id for the randomly generated token does not exist, then this token is unique and can be used
		if (getPlayerIdFromVerificationTokenInternal(connectionHandler, newToken, false) == null) {
			return newToken;
		}

		// Try the method again
		return generateUniqueVerificationToken(connectionHandler);
	}

	public Integer getPlayerIdFromVerificationToken(ConnectionHandler connectionHandler, String verificationToken) {
		return getPlayerIdFromVerificationTokenInternal(connectionHandler, verificationToken, true);
	}

	private Integer getPlayerIdFromVerificationTokenInternal(ConnectionHandler connectionHandler, String verificationToken,
	                                                         boolean closeConnectionAfterUse) {
		Connection connection = connectionHandler.getConnection();
		String getPlayerIdFromVerificationTokenQuery = formatQuery(GET_PLAYER_ID_FROM_TOKEN_QUERY);

		Integer playerId = null;

		try (PreparedStatement preparedStatement = connection.prepareStatement(getPlayerIdFromVerificationTokenQuery)) {
			preparedStatement.setString(1, verificationToken);

			ResultSet resultSet = preparedStatement.executeQuery();

			if (resultSet.next()) {
				playerId = resultSet.getInt(1);
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
		}

		if (closeConnectionAfterUse) {
			connectionHandler.closeConnectionIfNecessary();
		}

		return playerId;
	}

	public String getVerificationTokenFromPlayerId(ConnectionHandler connectionHandler, int playerId) {
		Connection connection = connectionHandler.getConnection();
		String getTokenFromPlayerIdQuery = formatQuery(GET_TOKEN_FROM_PLAYER_ID_QUERY);

		String token = null;

		try (PreparedStatement preparedStatement = connection.prepareStatement(getTokenFromPlayerIdQuery)) {
			preparedStatement.setInt(1, playerId);

			ResultSet resultSet = preparedStatement.executeQuery();

			if (resultSet.next()) {
				token = resultSet.getString(1);
			}
		} catch (SQLException ex) {
			ex.printStackTrace();
		}

		connectionHandler.closeConnectionIfNecessary();
		return token;
	}

	public void deleteVerificationToken(ConnectionHandler connectionHandler, String verificationToken) {
		Connection connection = connectionHandler.getConnection();
		String deleteTokenQuery = formatQuery(DELETE_TOKEN_QUERY);

		try (PreparedStatement preparedStatement = connection.prepareStatement(deleteTokenQuery)) {
			preparedStatement.setString(1, verificationToken);

			preparedStatement.executeUpdate();
		} catch (SQLException ex) {
			ex.printStackTrace();
		}

		connectionHandler.closeConnectionIfNecessary();
	}

}
