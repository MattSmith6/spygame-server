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
import java.util.Map;

public class GameRecordTable extends AbstractTable {
    private static final String NON_TESTING_TABLE_NAME = "game_records";

    private static final String CREATE_TABLE_QUERY = "CREATE TABLE IF NOT EXISTS %s (record_id, game_id INT NOT NULL, eliminator_id INT NOT NULL, eliminatee_id INT NOT NULL, " +
            "elimination_time bigint NOT NULL, PRIMARY KEY (record_id))";
    private static final String INSERT_INTO_QUERY = "INSERT INTO %s (    record_id, game_id,    eliminator_id,  eliminatee_id,    elimination_time) VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE_QUERY = "UPDATE %s SET game_id=?, eliminator_id=?,  eliminatee_id=?,    elimination_time=? WHERE record_id=?";
    private static final String SELECT_QUERY = "SELECT    record_id, game_id,    eliminator_id,  eliminatee_id,    elimination_time FROM %s WHERE game_id=? order by  elimination_time asc";

    public GameRecordTable(boolean useTestTables) {
        super(NON_TESTING_TABLE_NAME, useTestTables);
    }

//first funtion to create Table:
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
        preparedStatement.setString(1, String.valueOf(record.getRecord_id()));
        preparedStatement.setString(2, String.valueOf(record.getGame_id()));
        preparedStatement.setString(3, String.valueOf(record.getEliminator_id()));
        preparedStatement.setString(4, String.valueOf(record.getEliminatee_id()));
        preparedStatement.setString(5, String.valueOf(record.getElimination_time()));
        preparedStatement.executeUpdate();
    } catch (SQLException ex) {
        ex.printStackTrace();
    }
    connectionHandler.closeConnectionIfNecessary();
}

//Second Function Table to select from  table
Map<Integer, game_record> selectGameRecord(ConnectionHandler connectionHandler, int game_id) {
    Connection connection = connectionHandler.getConnection();
    String selectOneQuery = formatQuery(SELECT_QUERY);
    game_record dataObject = null;
    Map<Integer, game_record> objectList = null;
    try (PreparedStatement preparedStatement = connection.prepareStatement(selectOneQuery)) {
        preparedStatement.setString(1, String.valueOf(game_id));

        try (ResultSet resultSet = preparedStatement.executeQuery()) {
            // If there is a result, then this property does exist
            while(resultSet.next()) {

            int record_id = resultSet.getInt(1);
            int gameID = resultSet.getInt(2);
            int eliminatorID = resultSet.getInt(3);
            int eliminateeID = resultSet.getInt(4);
            long eliminationDate =Long.parseLong(resultSet.getString(5));
            dataObject = new game_record(record_id, gameID,eliminatorID,eliminateeID,eliminationDate);
            objectList.put(game_id, dataObject);
            }

        }

    } catch (SQLException ex) {
        ex.printStackTrace();
    }

    connectionHandler.closeConnectionIfNecessary();
    return objectList;
}

//Third Function Table to update the table
public void updateGameRecord(ConnectionHandler connectionHandler,int eliminator_id,int  eliminatee_id, long elimination_time, int game_id, int record_id) {
    Connection connection = connectionHandler.getConnection();
    String updateUsernameQuery = formatQuery(UPDATE_QUERY);

    try (PreparedStatement preparedStatement = connection.prepareStatement(updateUsernameQuery)) {
        preparedStatement.setString(1, String.valueOf(eliminator_id));
        preparedStatement.setString(2, String.valueOf(eliminatee_id));
        preparedStatement.setString(3, String.valueOf(elimination_time));
        preparedStatement.setString(4, String.valueOf(game_id));
        preparedStatement.setString(4, String.valueOf(record_id));

        preparedStatement.executeUpdate();
    } catch (SQLException ex) {
        ex.printStackTrace();
    }

    connectionHandler.closeConnectionIfNecessary();
}


    }







