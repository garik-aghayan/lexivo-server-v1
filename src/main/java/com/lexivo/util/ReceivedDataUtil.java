package com.lexivo.util;

public abstract class ReceivedDataUtil {
	private static final String ID_JOINING_ELM = "@";

	public static String createJoinedId(String... ids) {
		if (ids.length == 0) throw new RuntimeException("ids.length cannot be 0");
		if (ids.length == 1) return ids[0];

		return String.join(ID_JOINING_ELM, ids);
	}

	public static String[] separateJoinedId(String joinedId) {
		if (joinedId == null) return new String[]{};
		return joinedId.split(ID_JOINING_ELM);
	}
}
