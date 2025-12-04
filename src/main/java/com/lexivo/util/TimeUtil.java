package com.lexivo.util;

public abstract class TimeUtil {
	public static long getSecondsInMinutes(long minutes) {
		return minutes * 60;
	}

	public static long getMinutesInDays(int days) {
		return (long) days * 24 * 60;
	}
}
