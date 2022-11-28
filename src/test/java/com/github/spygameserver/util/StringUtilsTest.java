package com.github.spygameserver.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class StringUtilsTest {

	@Test
	public void testJoinOneParameter() {
		String testString = "abc";
		String[] stringArray = new String[] { testString };

		Assertions.assertEquals(testString, StringUtils.join(stringArray, '&'));
	}

	@Test
	public void testMultipleParameters() {
		String[] stringArray = new String[] { "abc", "def", "ghi", "jkl" };

		Assertions.assertEquals("abc&def&ghi&jkl", StringUtils.join(stringArray, '&'));
	}

}
