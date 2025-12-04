package com.lexivo.controllers;

import com.lexivo.enums.UserRole;
import com.lexivo.schema.User;
import com.lexivo.util.HttpResponseStatus;
import com.lexivo.util.JsonUtil;
import com.lexivo.util.JwtUtil;
import com.lexivo.util.TimeUtil;
import com.sun.net.httpserver.HttpExchange;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AuthController extends Controller {
	public AuthController(String routeBasePath) {
		super(routeBasePath);
	}

	@Override
	protected void post(HttpExchange exchange) throws IOException {
		String path = exchange.getRequestURI().getPath();
		if (pathsEqual(routeBasePath + "/login", path)) {
			login(exchange);
			return;
		}

		sendNotFoundResponse(exchange);
	}

	private void login(HttpExchange exchange) throws IOException {
		User requestBody = getRequestBody(exchange, User.class);
		if (requestBody == null) {
			sendBadRequestResponse(exchange, "Invalid request body");
			return;
		}
		String username = requestBody.getUsername();
		String password = requestBody.getPassword();

		String errorMsg = null;

		if (username == null || username.isBlank()) {
			errorMsg = "username is missing from request body;";
		}
		if (password == null || password.isBlank()) {
			errorMsg = errorMsg == null ? "" : errorMsg;
			errorMsg += " password is missing from request body;";
		}
		if (errorMsg != null) {
			sendBadRequestResponse(exchange, errorMsg);
			return;
		}
		// TODO: Uncomment the code below when DB is implemented
//		User user = Db.getUserByUsername(username);
//		String fakeHash = BCrypt.hashpw("fake-password", BCrypt.gensalt());
//		if (user == null) {
//			// Fake hashing to match the waiting time
//			BCrypt.checkpw("wrong-password", fakeHash);
//			sendBadRequestResponse(exchange, "Incorrect credentials");
//			return;
//		}

//		boolean passwordCorrect = BCrypt.checkpw(password, user.getPasswordHash());
//		if (!passwordCorrect) {
//			sendBadRequestResponse(exchange, "Incorrect credentials");
//			return;
//		}

		String accessToken = JwtUtil.createHMAC256Token(username, UserRole.USER, 5);
		String refreshToken = JwtUtil.createHMAC256Token(username, UserRole.USER, TimeUtil.getMinutesInDays(7));

		Map<String, String> jsonMap = new HashMap<>();
		jsonMap.put(JwtUtil.KEY_ACCESS_TOKEN, accessToken);
		jsonMap.put(JwtUtil.KEY_REFRESH_TOKEN, refreshToken);

		sendJsonResponse(exchange, HttpResponseStatus.OK, JsonUtil.toJson(jsonMap));
	}
}
