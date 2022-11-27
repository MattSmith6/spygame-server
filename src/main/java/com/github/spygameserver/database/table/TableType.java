package com.github.spygameserver.database.table;

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
