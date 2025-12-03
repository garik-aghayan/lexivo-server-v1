package com.lexivo.controllers;

import com.lexivo.util.HttpResponseStatus;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.lexivo.util.HttpRequestMethod;

import java.io.IOException;
import java.io.OutputStream;

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
				case null, default -> notFoundResponse(exchange);
			}
		} catch (IOException ioe) {
			// TODO: Replace with a proper logger
			System.err.println(ioe.getMessage());
		}
	}

	protected void get(HttpExchange exchange) throws IOException {
		notFoundResponse(exchange);
	}

	protected void post(HttpExchange exchange) throws IOException {
		notFoundResponse(exchange);
	}

	protected void put(HttpExchange exchange) throws IOException {
		notFoundResponse(exchange);
	}
	protected void delete(HttpExchange exchange) throws IOException {
		notFoundResponse(exchange);
	}

	protected void sendJsonResponse(HttpExchange exchange, int responseCode, String jsonResponse) throws IOException {
		exchange.sendResponseHeaders(responseCode, jsonResponse.length());
		OutputStream os = exchange.getResponseBody();
		os.write(jsonResponse.getBytes());
		os.close();
	}

	protected void notFoundResponse(HttpExchange exchange) {
		notFoundResponse(exchange, "Route does not exist");
	}

	protected void notFoundResponse(HttpExchange exchange, String responseMessage) {
		String message = "{ \"message\": \"" + responseMessage + "\" }";
		try {
			sendJsonResponse(exchange, HttpResponseStatus.NOT_FOUND, message);
		}
		catch(IOException ioe) {
			// TODO: Update
			System.err.println(ioe.getMessage());
		}
	}

	protected boolean pathsEqual(String path1, String path2) {
		path1 = path1.endsWith("/") ? path1 : path1 + "/";
		path2 = path2.endsWith("/") ? path2 : path2 + "/";
		return path1.equals(path2);
	}
}