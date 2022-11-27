package com.github.spygameserver.database.table;

import com.github.spygameserver.database.ConnectionHandler;
import com.github.spygameserver.player.account.AccountVerificationStatus;
import com.github.spygameserver.player.account.PlayerAccountData;
import com.github.spygameserver.player.account.PlayerVerificationData;
import org.json.JSONObject;
import org.json.JSONArray;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Random;

import netscape.javascript.JSObject;

public class GameLobbyTable extends AbstractTable {

    private static final TableType TABLE_TYPE = TableType.GAME_LOBBY;

    private static final String CREATE_TABLE_QUERY = "CREATE TABLE IF NOT EXISTS %s (game_id INT NOT NULL " +
            "AUTO_INCREMENT, invite_code CHAR(6), is_public INT, game_type INT, max_players INT, game_name " +
            "CHAR(20), current_players INT, start_time BIGINT, end_time BIGINT, PRIMARY KEY (game_id)," +
            " UNIQUE (invite_code))";

    private static final String GAME_FROM_INVITE_CODE_QUERY = "SELECT game_id, start_time FROM %s WHERE invite_code=?";

    private static final String INSERT_INTO_QUERY = "INSERT INTO %s (invite_code, is_public, game_type, " +
            "max_players, game_name, current_players) VALUES (?, ?, ?, ?, ?, ?)";

    private static final String GET_CURRENT_PLAYERS_QUERY = "SELECT current_players FROM %s WHERE game_id=?";
    private static final String GET_MAX_PLAYERS_QUERY = "SELECT max_players FROM %s WHERE game_id=?";
    private static final String UPDATE_CURRENT_PLAYERS_QUERY = "UPDATE %s SET current_players=? WHERE game_id=?";
    private static final String UPDATE_START_TIME = "UPDATE %s SET start_time=? WHERE game_id=?";
    private static final String UPDATE_END_TIME = "UPDATE %s SET end_time=? WHERE game_id=?";
    private static final String SHOW_ALL = "SELECT * FROM %s WHERE invite_code=?";
    private static final String CHECK_INVITE_CODE = "SELECT 1 FROM %s WHERE invite_code=?";
    private static final String GET_INVITE_CODE = "SELECT invite_code FROM %s WHERE game_id=?";
    private static final String GET_PUBLIC_COUNT = "SELECT COUNT (is_public) FROM %s WHERE is_public=1, start_time IS NULL";
    private static final String GET_PUBLIC_GAMES = "SELECT game_id, game_name, max_players," +
            " current_players, game_type, FROM %s WHERE is_public=1, start_time IS NULL";


    public GameLobbyTable() {
        super(TABLE_TYPE);
    }

    @Override
    protected void createTableIfNotExists(ConnectionHandler connectionHandler) {
        Connection connection = connectionHandler.getConnection();
        String createTableQuery = formatQuery(CREATE_TABLE_QUERY);

        try (PreparedStatement preparedStatement = connection.prepareStatement(createTableQuery)) {
            preparedStatement.execute();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        connectionHandler.closeConnectionIfNecessary();
    }

    public String generateInviteCode(ConnectionHandler connectionHandler) {
        Connection connection = connectionHandler.getConnection();

        boolean isUnique = false;
        Integer check = 0;
        String inviteCode= null;

        while (!isUnique) {
            int leftLimit = 97; // letter 'a'
            int rightLimit = 122; // letter 'z'
            int targetStringLength = 6;
            Random random = new Random();
            StringBuilder buffer = new StringBuilder(targetStringLength);
            for (int i = 0; i < targetStringLength; i++) {
                int randomLimitedInt = leftLimit + (int)
                        (random.nextFloat() * (rightLimit - leftLimit + 1));
                buffer.append((char) randomLimitedInt);
            }
            inviteCode = buffer.toString();

            String selectOneQuery = formatQuery(CHECK_INVITE_CODE);

            try (PreparedStatement preparedStatement = connection.prepareStatement(selectOneQuery)) {
                preparedStatement.setString(1, inviteCode);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    // If there is a result, then this property does exist
                    if (resultSet.next()) {
                        check = resultSet.getInt(1);
                    }
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }

            if (check != 1) {
                isUnique = true;
            }
        }

        connectionHandler.closeConnectionIfNecessary();
        return inviteCode;
    }


    public Pair<Integer, Long> getGameIdFromInviteCode(ConnectionHandler connectionHandler, String invite_code) {
        Connection connection = connectionHandler.getConnection();
        String selectOneQuery = formatQuery(GAME_FROM_INVITE_CODE_QUERY);

        Integer gameId = null;
        Long startTime = null;

        try (PreparedStatement preparedStatement = connection.prepareStatement(selectOneQuery)) {
            preparedStatement.setString(1, invite_code);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                // If there is a result, then this property does exist
                if(resultSet.next()) {
                    if(resultSet.getInt(1) != 0)    //There must be some better way to do this
                        gameId = resultSet.getInt(1);
                    if(resultSet.getLong(2) != 0)
                        startTime = resultSet.getLong(2);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        connectionHandler.closeConnectionIfNecessary();
        return new Pair<>(gameId, startTime);
    }

    public int createGame(ConnectionHandler connectionHandler, int is_public,
                           int game_type, int max_players, String gameName) {
        Connection connection = connectionHandler.getConnection();
        String insertIntoQuery = formatQuery(INSERT_INTO_QUERY);

        String inviteCode = generateInviteCode(connectionHandler);

        try (PreparedStatement preparedStatement = connection.prepareStatement(insertIntoQuery, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, inviteCode);
            preparedStatement.setInt(2, is_public);
            preparedStatement.setInt(3, game_type);
            preparedStatement.setInt(4, max_players);
            preparedStatement.setString(5, gameName);
            preparedStatement.setInt(6, 0);

            preparedStatement.executeUpdate();

            ResultSet resultSet = preparedStatement.getGeneratedKeys();
            if(resultSet.next()) {
                return resultSet.getInt(1);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        connectionHandler.closeConnectionIfNecessary();

        return -1;
    }

    public int getCurrentPlayers(ConnectionHandler connectionHandler, int gameID) {
        Connection connection = connectionHandler.getConnection();
        String selectOneQuery = formatQuery(GET_CURRENT_PLAYERS_QUERY);

        int currentPlayers = -1;

        try (PreparedStatement preparedStatement = connection.prepareStatement(selectOneQuery)) {
            preparedStatement.setInt(1, gameID);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                // If there is a result, then this property does exist
                if (resultSet.next()) {
                    currentPlayers = resultSet.getInt(1);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        connectionHandler.closeConnectionIfNecessary();
        return currentPlayers;
    }

    public void checkToStartGame(ConnectionHandler connectionHandler, int currentPlayers, int gameID) {
        Connection connection = connectionHandler.getConnection();
        String selectOneQuery = formatQuery(GET_MAX_PLAYERS_QUERY);

        int maxPlayers = -1;

        try (PreparedStatement preparedStatement = connection.prepareStatement(selectOneQuery)) {
            preparedStatement.setInt(1, gameID);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                // If there is a result, then this property does exist
                if (resultSet.next()) {
                    maxPlayers = resultSet.getInt(1);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        if(maxPlayers == currentPlayers) {
            updateStartTime(connectionHandler, gameID);

            //Maybe do something here?
        }

        connectionHandler.closeConnectionIfNecessary();
    }

    public void updateCurrentPlayers(ConnectionHandler connectionHandler, int currentPlayers, int gameID) {
        Connection connection = connectionHandler.getConnection();
        String updateUsernameQuery = formatQuery(UPDATE_CURRENT_PLAYERS_QUERY);

        try (PreparedStatement preparedStatement = connection.prepareStatement(updateUsernameQuery)) {
            preparedStatement.setInt(1, currentPlayers);
            preparedStatement.setInt(2, gameID);

            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        checkToStartGame(connectionHandler, currentPlayers, gameID);

        connectionHandler.closeConnectionIfNecessary();
    }

    public void updateStartTime(ConnectionHandler connectionHandler, int gameID) {
        Connection connection = connectionHandler.getConnection();
        String updateUsernameQuery = formatQuery(UPDATE_START_TIME);

        long startTime = System.currentTimeMillis();

        try (PreparedStatement preparedStatement = connection.prepareStatement(updateUsernameQuery)) {
            preparedStatement.setLong(1, startTime);
            preparedStatement.setInt(2, gameID);

            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        connectionHandler.closeConnectionIfNecessary();
    }

    public void updateEndTime(ConnectionHandler connectionHandler, int gameID) {
        Connection connection = connectionHandler.getConnection();
        String updateUsernameQuery = formatQuery(UPDATE_END_TIME);

        long endTime = System.currentTimeMillis();

        try (PreparedStatement preparedStatement = connection.prepareStatement(updateUsernameQuery)) {
            preparedStatement.setLong(1, endTime);
            preparedStatement.setInt(2, gameID);

            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        connectionHandler.closeConnectionIfNecessary();
    }

    public game showAll(ConnectionHandler connectionHandler, String inviteCode) {
        Connection connection = connectionHandler.getConnection();
        String selectOneQuery = formatQuery(SHOW_ALL);

        game shownGame = new game();

        try (PreparedStatement preparedStatement = connection.prepareStatement(selectOneQuery)) {
            preparedStatement.setString(1, inviteCode);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                // If there is a result, then this property does exist
                if (resultSet.next()) {
                    shownGame.gameID = resultSet.getInt(1);
                    shownGame.inviteCode = resultSet.getString(2);
                    shownGame.isPublic = resultSet.getInt(3);
                    shownGame.gameType = resultSet.getInt(4);
                    shownGame.maxPlayers = resultSet.getInt(5);
                    shownGame.gameName = resultSet.getString(6);
                    shownGame.currentPlayers = resultSet.getInt(7);
                    shownGame.startTime = resultSet.getLong(8);
                    if(resultSet.wasNull())
                        shownGame.startTime = null;
                    shownGame.endTime = resultSet.getLong(9);
                    if(resultSet.wasNull())
                        shownGame.endTime = null;
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        connectionHandler.closeConnectionIfNecessary();
        return shownGame;
    }

    public String getInviteCode(ConnectionHandler connectionHandler, int gameID) {
        Connection connection = connectionHandler.getConnection();
        String selectOneQuery = formatQuery(GET_INVITE_CODE);

        String inviteCode = null;

        try (PreparedStatement preparedStatement = connection.prepareStatement(selectOneQuery)) {
            preparedStatement.setInt(1, gameID);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                // If there is a result, then this property does exist
                if (resultSet.next()) {
                    inviteCode = resultSet.getString(1);
                }
            }
        } catch (SQLException ex) {
        ex.printStackTrace();
    }

        connectionHandler.closeConnectionIfNecessary();
        return inviteCode;
    }

    public int numPublicGames(ConnectionHandler connectionHandler) {
        Connection connection = connectionHandler.getConnection();
        String selectOneQuery = formatQuery(GET_PUBLIC_COUNT);

        int count = 0;

        try (PreparedStatement preparedStatement = connection.prepareStatement(selectOneQuery)) {

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                // If there is a result, then this property does exist
                if (resultSet.next()) {
                    count = resultSet.getInt(1);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        connectionHandler.closeConnectionIfNecessary();
        return count;
    }

    public JSONObject getPublicGames(ConnectionHandler connectionHandler) {
        Connection connection = connectionHandler.getConnection();
        String selectOneQuery = formatQuery(GET_PUBLIC_GAMES);

        JSONObject publicGames = new JSONObject();
        int count = 0;
        JSONArray gameIDs = new JSONArray();
        JSONArray gameNames = new JSONArray();
        JSONArray maxPlayers = new JSONArray();
        JSONArray currentPlayers = new JSONArray();
        JSONArray gameTypes = new JSONArray();

        try (PreparedStatement preparedStatement = connection.prepareStatement(selectOneQuery)) {

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                // If there is a result, then this property does exist
                count = numPublicGames(connectionHandler);

                while (resultSet.next()) {
                    gameIDs.put(resultSet.getInt(1));
                    gameNames.put(resultSet.getString(2));
                    maxPlayers.put(resultSet.getInt(3));
                    currentPlayers.put(resultSet.getInt(4));
                    gameTypes.put(resultSet.getInt(5));
                }

                publicGames.put("Count", count);
                publicGames.put("IDs", gameIDs);
                publicGames.put("Max Players", maxPlayers);
                publicGames.put("Current Players", currentPlayers);
                publicGames.put("Game Types", gameTypes);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        connectionHandler.closeConnectionIfNecessary();
        return publicGames;
    }

    public static class game {
        int gameID = -1;
        String inviteCode = null;
        int isPublic = -1;
        int gameType = -1;
        int maxPlayers = -1;
        String gameName = null;
        int currentPlayers = -1;
        Long startTime = null;
        Long endTime = null;
    }

    public static class Pair<L, R> {

        private final L l;
        private final R r;

        public Pair(L l, R r) {
            this.l = l;
            this.r = r;
        }

        public L getL() {
            return l;
        }

        public R getR() {
            return r;
        }

    }

}