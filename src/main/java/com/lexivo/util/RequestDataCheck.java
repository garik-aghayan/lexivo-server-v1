package com.lexivo.util;

import com.lexivo.controllers.Controller;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class RequestDataCheck {
	private static String getRequestBodyString(HttpExchange exchange) throws IOException {
		InputStream requestBodyStream = exchange.getRequestBody();
		return new String(requestBodyStream.readAllBytes(), StandardCharsets.UTF_8);
	}

	public static <T> T getCheckedRequestBody(HttpExchange exchange, List<String> fieldsToCheck, Class<T> clazz) {
		try {
			String reqBodyString = getRequestBodyString(exchange);

			@SuppressWarnings("unchecked")
			Map<String , ?> requestBody = JsonUtil.fromJson(reqBodyString, Map.class);

			if (requestBody == null) {
				Controller.sendBadRequestResponse(exchange, "Invalid request body");
				return null;
			}

			List<String> errorMsgList = new ArrayList<>();

			for (String field : fieldsToCheck) {
				Object value = requestBody.get(field);
				if (value == null || (value instanceof String && ((String) value).isBlank())) {
					errorMsgList.add(field + " is missing");
				}
			}
			if (!errorMsgList.isEmpty()) {
				Controller.sendResponseWithMessage(exchange, HttpResponseStatus.BAD_REQUEST, errorMsgList.toArray(new String[0]));
				return null;
			}
			return JsonUtil.fromJson(reqBodyString, clazz);
		}
		catch (IOException ioe) {
			// TODO: proper logger
			System.out.println(ioe.getMessage());
			Controller.sendServerSideErrorResponse(exchange);
			return null;
		}
	}

	public static boolean doesPasswordMeetRequirements(String password) {
		return password.matches("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).{8,32}$");
	}
}
