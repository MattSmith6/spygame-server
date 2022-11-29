package com.github.spygameserver.util;

public class StringUtils {

	// Inspiration taken from Apache Commons #join
	public static String join(char separator, String... parameters) {
		if (parameters.length == 1) {
			return parameters[0];
		}

		StringBuilder parameterBuilder = new StringBuilder();

		for (int i = 0; i < parameters.length - 1; i++) {
			parameterBuilder.append(parameters[i]);
			parameterBuilder.append(separator);
		}

		parameterBuilder.append(parameters[parameters.length - 1]);

		return parameterBuilder.toString();
	}
}
