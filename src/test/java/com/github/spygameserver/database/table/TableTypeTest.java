package com.github.spygameserver.database.table;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TableTypeTest {

	@Test
	public void testNotUsingTestingTableNames() {
		TableType.setUseTestTables(false);

		for (TableType tableType : TableType.values()) {
			Assertions.assertEquals(tableType.name().toLowerCase(), tableType.getTableName());
		}
	}

	@Test
	public void testUsingTestingTableNames() {
		TableType.setUseTestTables(true);

		for (TableType tableType : TableType.values()) {
			Assertions.assertEquals(TableType.TESTING_TABLE_PREFIX + tableType.name().toLowerCase(),
					tableType.getTableName());
		}
	}

}
