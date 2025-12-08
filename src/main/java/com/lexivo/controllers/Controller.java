package com.lexivo.controllers;

import com.lexivo.db.Db;
import com.lexivo.schema.EmailConfirmationCodeData;
import com.lexivo.schema.Log;
import com.lexivo.schema.User;
import com.lexivo.util.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.Map;

public abstract class Controller implements HttpHandler {
	protected final String path;
	public Controller(String path) {
		this.path = path;
	}

	@Override
	public void handle(HttpExchange exchange) {
		String reqMethod = exchange.getRequestMethod();
		String uriPath = exchange.getRequestURI().getPath();
		try {
			if (!pathsEqual(path, uriPath)) {
				sendRouteDoesNotExistResponse(exchange);
				return;
			}
			switch (reqMethod) {
				case HttpRequestMethod.GET -> get(exchange);
				case HttpRequestMethod.POST -> post(exchange);
				case HttpRequestMethod.PUT -> put(exchange);
				case HttpRequestMethod.DELETE -> delete(exchange);
				case null, default -> sendNotFoundResponse(exchange);
			}
		} catch (Exception ioe) {
			Log.exception(
					"Server side IOException in controllers.Controller.handle",
					"Request method: " + exchange.getRequestMethod(),
					"URI.path: " + exchange.getRequestURI().getPath(),
					"URI.query: " + exchange.getRequestURI().getQuery(),
					ioe.getMessage());
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

	protected static void sendConfirmationCodeEmailAndResponse(HttpExchange exchange, User user, int responseCode) throws IOException {
		EmailConfirmationCodeData codeData = new EmailConfirmationCodeData(user.getEmail(), Randomizer.getEmailConfirmationCode());
		boolean success = Db.emailConfirmationCodes().addConfirmationCode(codeData);
		if (!success) {
			sendServerSideErrorResponse(exchange);
			return;
		};

		Email.sendConfirmationCode(user.getEmail(), codeData.getCode());

		Controller.sendResponseWithMessage(exchange, responseCode, "Confirm your email within 10 minutes");
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
		catch (IOException ioe) {
			Log.exception("Server side IOException in controllers.Controller.sendServerSideErrorResponse", ioe.getMessage());
		}
	}

	public static void sendBadRequestResponse(HttpExchange exchange, String... messages) throws IOException {
		sendResponseWithMessage(exchange, HttpResponseStatus.BAD_REQUEST, messages);
	}

	public static void sendOkResponse(HttpExchange exchange) throws IOException {
		sendJsonResponse(exchange, HttpResponseStatus.OK, "");
	}

	public static void sendOkResponse(HttpExchange exchange, String... responseMessageList) throws IOException {
		sendResponseWithMessage(exchange, HttpResponseStatus.OK, responseMessageList);
	}


	protected boolean pathsEqual(String path1, String path2) {
		path1 = path1.endsWith("/") ? path1 : path1 + "/";
		path2 = path2.endsWith("/") ? path2 : path2 + "/";
		return path1.equals(path2);
	}
}