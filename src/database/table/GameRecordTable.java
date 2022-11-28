package com.github.spygameserver.database.table;

import com.github.glusk.caesar.Bytes;
import com.github.glusk.srp6_variables.SRP6CustomIntegerVariable;
import com.github.glusk.srp6_variables.SRP6IntegerVariable;
import com.github.spygameserver.auth.PlayerAuthenticationData;
import com.github.spygameserver.database.ConnectionHandler;
import com.github.spygameserver.player.account.AccountVerificationStatus;

import java.math.BigInteger;
import java.nio.ByteOrder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class GameRecordTable extends AbstractTable {
    private static final String NON_TESTING_TABLE_NAME = "game_records";

    private static final String CREATE_TABLE_QUERY = "CREATE TABLE IF NOT EXISTS %s (game_id INT NOT NULL, eliminator_id INT NOT NULL, eliminatee_id INT NOT NULL, " +
            "elimination_time VARCHAR(15) NOT NULL, PRIMARY KEY (game_id))";
    private static final String INSERT_INTO_QUERY = "INSERT INTO %s (    game_id,    eliminator_id,  eliminatee_id,    elimination_time) VALUES (?, ?, ?, ?)";
    private static final String UPDATE_QUERY = "UPDATE %s SET eliminator_id=?,  eliminatee_id=?,    elimination_time=? WHERE game_id=?";
    private static final String SELECT_QUERY = "SELECT     game_id,    eliminator_id,  eliminatee_id,    elimination_time FROM %s WHERE game_id=?";

    public GameRecordTable(boolean useTestTables) {
        super(NON_TESTING_TABLE_NAME, useTestTables);
    }

    //first function to create Table:
    @Override
    protected void createTableIfNotExists(ConnectionHandler connectionHandler) {
        Connection connection = connectionHandler.getConnection();
        String createTableIfNotExistsQuery = formatQuery(CREATE_TABLE_QUERY);
        try (PreparedStatement preparedStatement = connection.prepareStatement(createTableIfNotExistsQuery)) {
            preparedStatement.execute();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        connectionHandler.closeConnectionIfNecessary();
    }

    //Second Function Table to insert into table
    public void insertGameRecord(ConnectionHandler connectionHandler, game_record record) {
        Connection connection = connectionHandler.getConnection();
        String insertIntoQuery = formatQuery(INSERT_INTO_QUERY);

        try (PreparedStatement preparedStatement = connection.prepareStatement(insertIntoQuery)) {
            preparedStatement.setString(1, record.getGame_id());
            preparedStatement.setString(2, record.getEliminator_id());
            preparedStatement.setString(3, record.getEliminatee_id());
            preparedStatement.setString(4, record.getElimination_time());
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        connectionHandler.closeConnectionIfNecessary();
    }

    //Second Function Table to select from  table
    private game_record selectGameRecord(ConnectionHandler connectionHandler, int game_id) {
        Connection connection = connectionHandler.getConnection();
        String selectOneQuery = formatQuery(SELECT_QUERY);
        game_record dataObject = null;
        try (PreparedStatement preparedStatement = connection.prepareStatement(selectOneQuery)) {
            preparedStatement.setString(1, game_id);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                // If there is a result, then this property does exist
                if (!resultSet.next()) {
                    return null;
                }
                int gameID = resultSet.getInt(1);
                int eliminatorID = resultSet.getInt(2);
                int eliminateeID = resultSet.getInt(3);
                String eliminationDate = resultSet.getString(4);

                dataObject = new game_record(gameID,eliminatorID,eliminateeID,eliminationDate);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        connectionHandler.closeConnectionIfNecessary();
        return dataObject;
    }

    //Third Function Table to update the table
    public void updateGameRecord(ConnectionHandler connectionHandler,int eliminator_id,int  eliminatee_id, String elimination_time, int game_id) {
        Connection connection = connectionHandler.getConnection();
        String updateUsernameQuery = formatQuery(UPDATE_QUERY);

        try (PreparedStatement preparedStatement = connection.prepareStatement(updateUsernameQuery)) {
            preparedStatement.setString(1, eliminator_id);
            preparedStatement.setString(2,  eliminatee_id);
            preparedStatement.setString(3, elimination_time);
            preparedStatement.setString(4, game_id);

            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        connectionHandler.closeConnectionIfNecessary();
    }


}






