package com.github.spygameserver.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class StringUtilsTest {

	@Test
	public void testJoinOneParameter() {
		Assertions.assertEquals("abc", StringUtils.join('&', "abc"));
	}

	@Test
	public void testMultipleParameters() {
		Assertions.assertEquals("abc&def&ghi&jkl", StringUtils.join('&', "abc", "def", "ghi", "jkl"));
	}

}
