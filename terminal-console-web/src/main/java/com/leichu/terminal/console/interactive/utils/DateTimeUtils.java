package com.leichu.terminal.console.interactive.utils;

import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * 日期时间工具类.
 *
 * @author ffchul.
 * @since 2023-07-30.
 */
public class DateTimeUtils extends DateUtils {

	public static final Logger logger = LoggerFactory.getLogger(DateTimeUtils.class);

	public static void sleep(Integer seconds) {
		try {
			Thread.sleep(seconds * 1000);
		} catch (InterruptedException e) {
			logger.error("Thread sleep error!", e);
		}
	}

	public static void sleep(Long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			logger.error("Thread sleep error!", e);
		}
	}

	public static Long getInterval4Seconds(Date start, Date end) {
		LocalDateTime ldt = start.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
		LocalDateTime rdt = end.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
		return Duration.between(ldt, rdt).getSeconds();
	}
}
