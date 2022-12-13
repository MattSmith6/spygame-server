package com.github.spygameserver.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * A test class designed to test the functionality of StringUtils#join, which joins a list of strings by a character separator.
 */
public class StringUtilsTest {

	/**
	 * Test the StringUtils join method with a singular parameter (should have no character appended)
	 */
	@Test
	public void testJoinOneParameter() {
		Assertions.assertEquals("abc", StringUtils.join('&', "abc"));
	}

	/**
	 * Test the StringUtils join method with multiple parameters (each parameter should be separated by the separator)
	 */
	@Test
	public void testMultipleParameters() {
		Assertions.assertEquals("abc&def&ghi&jkl", StringUtils.join('&', "abc", "def", "ghi", "jkl"));
	}

}
