package com.lexivo.controllers;

import com.lexivo.util.HttpResponseStatus;
import com.lexivo.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.lexivo.util.HttpRequestMethod;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
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

	public static void sendResponseWithMessage(HttpExchange exchange, int responseCode, String... messages) throws IOException {
		sendJsonResponse(exchange, responseCode, JsonUtil.toJson(Map.of("message", messages)));
	}

	public void sendRouteDoesNotExistResponse(HttpExchange exchange) throws IOException {
		sendNotFoundResponse(exchange, "Route does not exist");
	}

	public void sendNotFoundResponse(HttpExchange exchange, String... responseMessageList) throws IOException {
		sendResponseWithMessage(exchange, HttpResponseStatus.NOT_FOUND, responseMessageList);
	}

	public static void sendServerSideErrorResponse(HttpExchange exchange) {
		try {
			sendResponseWithMessage(exchange, HttpResponseStatus.SERVER_SIDE_ERROR, "Server side error");
		}
		catch (IOException e) {
			// TODO: Proper logger
			System.err.println(e.getMessage());
		}
	}

	public static void sendBadRequestResponse(HttpExchange exchange, String... messages) throws IOException {
		sendResponseWithMessage(exchange, HttpResponseStatus.BAD_REQUEST, messages);
	}

	public static void sendOkWithMessage(HttpExchange exchange, String message) throws IOException {
		sendJsonResponse(exchange, HttpResponseStatus.OK, JsonUtil.toJson(Map.of("message", message)));
	}

	protected boolean pathsEqual(String path1, String path2) {
		path1 = path1.endsWith("/") ? path1 : path1 + "/";
		path2 = path2.endsWith("/") ? path2 : path2 + "/";
		return path1.equals(path2);
	}
}