package com.lexivo.controllers;

import com.google.gson.reflect.TypeToken;
import com.lexivo.util.HttpResponseStatus;
import com.lexivo.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.lexivo.util.HttpRequestMethod;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public abstract class Controller implements HttpHandler {
	protected final String routeBasePath;
	public Controller(String routeBasePath) {
		this.routeBasePath = routeBasePath;
	}

	@Override
	public void handle(HttpExchange exchange) {
		String reqMethod = exchange.getRequestMethod();
		try {
			switch (reqMethod) {
				case HttpRequestMethod.GET -> get(exchange);
				case HttpRequestMethod.POST -> post(exchange);
				case HttpRequestMethod.PUT -> put(exchange);
				case HttpRequestMethod.DELETE -> delete(exchange);
				case null, default -> sendNotFoundResponse(exchange);
			}
		} catch (Exception ioe) {
			// TODO: Replace with a proper logger
			System.err.println(ioe.getMessage());
			sendServerSideErrorResponse(exchange);
		}
	}

	protected void get(HttpExchange exchange) throws IOException, SQLException {
		sendNotFoundResponse(exchange);
	}

	protected void post(HttpExchange exchange) throws IOException, SQLException {
		sendNotFoundResponse(exchange);
	}

	protected void put(HttpExchange exchange) throws IOException, SQLException {
		sendNotFoundResponse(exchange);
	}
	protected void delete(HttpExchange exchange) throws IOException, SQLException {
		sendNotFoundResponse(exchange);
	}

	public static void sendJsonResponse(HttpExchange exchange, int responseCode, String jsonResponse) throws IOException {
		exchange.sendResponseHeaders(responseCode, jsonResponse.length());
		OutputStream os = exchange.getResponseBody();
		os.write(jsonResponse.getBytes());
		os.close();
	}

	protected void sendNotFoundResponse(HttpExchange exchange) throws IOException {
		sendNotFoundResponse(exchange, "Route does not exist");
	}

	protected void sendNotFoundResponse(HttpExchange exchange, String responseMessage) throws IOException {
		sendJsonResponse(exchange, HttpResponseStatus.NOT_FOUND, JsonUtil.toJson(Map.of("message", responseMessage)));
	}

	public static void sendServerSideErrorResponse(HttpExchange exchange) {
		try {
			sendJsonResponse(exchange, HttpResponseStatus.SERVER_SIDE_ERROR, JsonUtil.toJson(Map.of("message", "Server side error")));
		}
		catch (IOException e) {
			// TODO: Proper logger
			System.err.println(e.getMessage());
		}
	}

	public static void sendBadRequestResponse(HttpExchange exchange, String message) throws IOException {
		sendJsonResponse(exchange, HttpResponseStatus.BAD_REQUEST, JsonUtil.toJson(Map.of("message", message)));
	}

	public static void sendUnauthorizedResponse(HttpExchange exchange, String message) throws IOException {
		sendJsonResponse(exchange, HttpResponseStatus.UNAUTHORIZED, JsonUtil.toJson(Map.of("message", message)));
	}

	protected boolean pathsEqual(String path1, String path2) {
		path1 = path1.endsWith("/") ? path1 : path1 + "/";
		path2 = path2.endsWith("/") ? path2 : path2 + "/";
		return path1.equals(path2);
	}

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

			String errorMsg = null;

			for (String field : fieldsToCheck) {
				Object value = requestBody.get(field);
				if (value == null || (value instanceof String && ((String) value).isBlank())) {
					errorMsg = errorMsg == null ? "" : errorMsg;
					errorMsg += field + " is missing;";
				}
			}
			if (errorMsg != null) {
				Controller.sendBadRequestResponse(exchange, errorMsg);
				return null;
			}
			return JsonUtil.fromJson(reqBodyString, clazz);
		}
		catch (IOException ioe) {
			// TODO: Handle
			System.out.println(ioe.getMessage());
			Controller.sendServerSideErrorResponse(exchange);
			return null;
		}
	}
}