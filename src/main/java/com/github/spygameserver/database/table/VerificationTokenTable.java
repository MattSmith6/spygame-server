package com.github.spygameserver.database.table;

import com.github.spygameserver.database.ConnectionHandler;

import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class VerificationTokenTable extends AbstractTable {

	private static final TableType TABLE_TYPE = TableType.VERIFICATION_TOKEN;
	public static final int TOKEN_LENGTH = 64;

	private static final String CREATE_TABLE_QUERY = "CREATE TABLE IF NOT EXISTS %s (id INT NOT NULL AUTO_INCREMENT, " +
			"player_id INT NOT NULL, verification_token CHAR(%s) NOT NULL, PRIMARY KEY (id), UNIQUE (verification_token))";

	private static final String INSERT_INTO_QUERY = "INSERT INTO %s VALUES (?, ?)";
	private static final String GET_PLAYER_ID_FROM_TOKEN_QUERY = "SELECT player_id FROM %s WHERE verification_token=?";

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

	public void addNewVerificationTokenForPlayer(ConnectionHandler connectionHandler, int playerId) {
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
