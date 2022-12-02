package com.github.spygameserver.database;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public enum DatabaseType {

	GAME("game_db.properties"),
	AUTHENTICATION("auth_db.properties");

	private static final String HIKARI_RESOURCE_PATH = "hikari";
	private final String propertiesFileName;

	DatabaseType(String propertiesFileName) {
		this.propertiesFileName = propertiesFileName;
	}

	public Properties getHikariProperties() {
		Properties properties = new Properties();

		try {
			InputStream inputStream = getClass().getClassLoader().getResourceAsStream(HIKARI_RESOURCE_PATH
					+ File.separator + propertiesFileName);
			if (inputStream != null) {
				properties.load(inputStream);
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return properties;
	}

}
