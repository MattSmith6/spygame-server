package com.github.spygameserver.database.table;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * A class that ensures that the table names match the expected table name when toggling the useTestTables setter.
 */
public class TableTypeTest {

	/**
	 * Ensures that all table names match the production table names: the enum's name to lower case
	 * e.g. PLAYER_AUTHENTICATION -> player_authentication
	 */
	@Test
	public void testNotUsingTestingTableNames() {
		TableType.setUseTestTables(false);

		for (TableType tableType : TableType.values()) {
			Assertions.assertEquals(tableType.name().toLowerCase(), tableType.getTableName());
		}
	}

	/**
	 * Ensures that all table names match the testing table names: PREFIX + the enum's name to lower case
	 * e.g. PLAYER_AUTHENTICATION -> test_player_authentication
	 */
	@Test
	public void testUsingTestingTableNames() {
		TableType.setUseTestTables(true);

		for (TableType tableType : TableType.values()) {
			Assertions.assertEquals(TableType.TESTING_TABLE_PREFIX + tableType.name().toLowerCase(),
					tableType.getTableName());
		}
	}

}
