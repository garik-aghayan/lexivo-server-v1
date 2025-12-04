package com.lexivo.controllers;

import com.google.gson.JsonIOException;
import com.lexivo.util.HttpResponseStatus;
import com.lexivo.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.lexivo.util.HttpRequestMethod;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
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
		} catch (IOException ioe) {
			// TODO: Replace with a proper logger
			System.err.println(ioe.getMessage());
			sendServerSideErrorResponse(exchange);
		}
	}

	protected void get(HttpExchange exchange) throws IOException {
		sendNotFoundResponse(exchange);
	}

	protected void post(HttpExchange exchange) throws IOException {
		sendNotFoundResponse(exchange);
	}

	protected void put(HttpExchange exchange) throws IOException {
		sendNotFoundResponse(exchange);
	}
	protected void delete(HttpExchange exchange) throws IOException {
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
		Map<String, String> jsonMap = new HashMap<>();
		jsonMap.put("message", responseMessage);
		sendJsonResponse(exchange, HttpResponseStatus.NOT_FOUND, JsonUtil.toJson(jsonMap));
	}

	protected void sendServerSideErrorResponse(HttpExchange exchange) {
		try {
//			TODO: Update
			Map<String, String> jsonMap = new HashMap<>();
			jsonMap.put("message", "Server side error");
			sendJsonResponse(exchange, HttpResponseStatus.SERVER_SIDE_ERROR, JsonUtil.toJson(jsonMap));
		}
		catch (IOException e) {
			// TODO: Proper logger
			System.err.println(e.getMessage());
		}
	}

	protected void sendBadRequestResponse(HttpExchange exchange, String message) throws IOException {
		Map<String, String> jsonMap = new HashMap<>();
		jsonMap.put("message", message);
		sendJsonResponse(exchange, HttpResponseStatus.BAD_REQUEST, JsonUtil.toJson(jsonMap));
	}

	protected boolean pathsEqual(String path1, String path2) {
		path1 = path1.endsWith("/") ? path1 : path1 + "/";
		path2 = path2.endsWith("/") ? path2 : path2 + "/";
		return path1.equals(path2);
	}

	protected <T> T getRequestBody(HttpExchange exchange, Class<T> clazz) throws IOException {
		InputStream requestBodyStream = exchange.getRequestBody();
		String requestBodyString = new String(requestBodyStream.readAllBytes(), StandardCharsets.UTF_8);

		try {
			return JsonUtil.fromJson(requestBodyString, clazz);
		}
		catch (Exception e) {
			return null;
		}
	}
}