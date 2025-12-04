package com.lexivo.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public abstract class JsonUtil {
	private static final Gson gson = new GsonBuilder().serializeNulls().create();

	public static String toJson(Object obj) {
		return gson.toJson(obj);
	}

	public static <T> T fromJson(String jsonString, Class<T> clazz) {
		return gson.fromJson(jsonString, clazz);
	}
}
