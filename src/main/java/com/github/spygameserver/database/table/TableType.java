package com.github.spygameserver.database.table;

/**
 * The table type used to get the table name, given the globally set variable for when to use test tables.
 * If useTestTables is true, it appends 'test_' to the table name to not conflict with real production data.
 */
public enum TableType {

	GAME_LOBBY,
	GAME_RECORD,
	PLAYER_ACCOUNT,
	PLAYER_AUTHENTICATION,
	PLAYER_GAME_INFO,
	VERIFICATION_TOKEN;

	public static final String TESTING_TABLE_PREFIX = "test_";
	private static boolean useTestTables = false;

	private final String nonTestingTableName;

	TableType() {
		this.nonTestingTableName = name().toLowerCase();
	}

	public String getTableName() {
		return useTestTables ? TESTING_TABLE_PREFIX + nonTestingTableName : nonTestingTableName;
	}

	public static void setUseTestTables(boolean useTestTables1) {
		useTestTables = useTestTables1;
	}

	public static boolean isUsingTestTables() {
		return useTestTables;
	}

}
