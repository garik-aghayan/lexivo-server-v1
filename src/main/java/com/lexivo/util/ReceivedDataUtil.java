package com.lexivo.util;

public abstract class ReceivedDataUtil {
	public static String createJoinedId(String... ids) {
		if (ids.length == 0) throw new RuntimeException("ids.length cannot be 0");
		if (ids.length == 1) return ids[0];

		return String.join("@", ids);
	}
}
