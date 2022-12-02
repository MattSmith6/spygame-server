package com.github.spygameserver.database.table;
import com.github.spygameserver.DatabaseRequiredTest;
import com.github.spygameserver.database.ConnectionHandler;
import com.github.spygameserver.database.DatabaseCreator;
import com.github.spygameserver.database.impl.GameDatabase;
import com.github.spygameserver.player.account.AccountVerificationStatus;
import com.github.spygameserver.player.account.PlayerAccountData;
import com.github.spygameserver.player.account.PlayerVerificationData;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.File;
import java.util.Map;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GameRecordTableTest implements DatabaseRequiredTest  {
    //class parameters
    private static final int record_id = 1001;
    private static final int  game_id = 2001;
    private static final int  eliminator_id = 3001;
    private static final int  eliminatee_id = 4001;
    private static final long elimination_time = 898888999;

    private GameDatabase gameDatabase;
    private GameRecordTable gameRecordTable;
    private ConnectionHandler connectionHandler;
    @BeforeAll
    public void setupConnection() {
        gameDatabase = getGameDatabase();

        gameRecordTable = new GameRecordTable(true);
        connectionHandler = gameDatabase.getNewConnectionHandler(false);

        // Make sure no data persists, since these fields need to be unique
        gameRecordTable.dropTableSecure(connectionHandler);
        gameRecordTable.initialize(connectionHandler);
    }

    @Test
    public void testAllPaths() {
        game_record record = new game_record(record_id,game_id, eliminator_id,eliminatee_id,elimination_time);
        gameRecordTable.insertGameRecord( connectionHandler,   record);
        Map<Integer, game_record>  map = gameRecordTable.selectGameRecord( connectionHandler, record.getGame_id());
        gameRecordTable.updateGameRecord(connectionHandler, record.getEliminator_id(), record.getEliminatee_id(), record.getElimination_time(), record.getGame_id(), record.getRecord_id());

    }
    @AfterAll
    @Override
    public void closeOpenConnections() {
        closeOpenConnections(gameDatabase);
        closeOpenConnections(connectionHandler);
    }
}
