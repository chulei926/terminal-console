package com.leichu.terminal.console.interactive.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegUtils {


	public static boolean match(String content, String regex) {
		if (StringUtils.isBlank(content) || StringUtils.isBlank(regex)) {
			return false;
		}
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(content);
		return matcher.find();
	}
}
