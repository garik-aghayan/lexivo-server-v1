package com.lexivo.controllers;

import com.lexivo.db.Db;
import com.lexivo.schema.User;
import com.lexivo.util.HttpResponseStatus;
import com.lexivo.util.JsonUtil;
import com.lexivo.util.JwtUtil;
import com.lexivo.util.Randomizer;
import com.sun.net.httpserver.HttpExchange;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.sql.SQLException;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class AuthController extends Controller {
	private static final Map<String, List<Long>> emailConfirmationCodeMap = new HashMap<>();

	public AuthController(String routeBasePath) {
		super(routeBasePath);
	}

	@Override
	protected void post(HttpExchange exchange) throws IOException, SQLException {
		String path = exchange.getRequestURI().getPath();
		if (pathsEqual(routeBasePath + "/login", path)) {
			login(exchange);
			return;
		}

		if (pathsEqual(routeBasePath + "/signup", path)){
			signup(exchange);
			return;
		}

		if (pathsEqual(routeBasePath + "/confirm_email", path)){
			confirmEmail(exchange);
			return;
		}

		sendNotFoundResponse(exchange);
	}

	private void login(HttpExchange exchange) throws IOException, SQLException {
		User requestBody = getCheckedRequestBody(exchange, List.of("email", "password"), User.class);

		if (requestBody == null) return;

		String email = requestBody.getEmail();
		String password = requestBody.getPassword();

		User user = Db.user().getByEmail(email);
		String fakeHash = BCrypt.hashpw("fake-password", BCrypt.gensalt());
		if (user == null) {
			// Fake hashing to match the waiting time
			BCrypt.checkpw("wrong-password", fakeHash);
			sendBadRequestResponse(exchange, "Incorrect credentials");
			return;
		}

		boolean passwordCorrect = false;
		try {
			passwordCorrect = BCrypt.checkpw(password, user.getPasswordHash());
		} catch (RuntimeException ignore) {}

		if (!passwordCorrect) {
			sendBadRequestResponse(exchange, "Incorrect credentials");
			return;
		}

		if (!user.isConfirmed()) {
			emailConfirmationCodeMap.put(email, Randomizer.getEmailConfirmationNumberAndDateList());
			// TODO: Remove the second value from map
			sendJsonResponse(exchange, HttpResponseStatus.OK, JsonUtil.toJson(Map.of("message", "Confirm your email within 10 minutes", "code", emailConfirmationCodeMap.get(email))));
			return;
		}

		Map<String, String> jsonMap = new HashMap<>();
		jsonMap.put(JwtUtil.KEY_ACCESS_TOKEN, JwtUtil.createAccessToken(email));
		jsonMap.put(JwtUtil.KEY_REFRESH_TOKEN, JwtUtil.createRefreshToken(email));

		sendJsonResponse(exchange, HttpResponseStatus.OK, JsonUtil.toJson(jsonMap));
	}

	private void signup(HttpExchange exchange) throws IOException, SQLException {
		User requestBody = getCheckedRequestBody(exchange, List.of("name", "email", "password"), User.class);
		if (requestBody == null) return;

		String name = requestBody.getName();
		String email = requestBody.getEmail();
		String password = requestBody.getPassword();

		User user = Db.user().getByEmail(email);
		if (user != null) {
			sendBadRequestResponse(exchange, "User with the given email already exists");
			return;
		}

		//		TODO: Add password complexity check

		String passwordHash = BCrypt.hashpw(password, BCrypt.gensalt());

		boolean success = Db.user().addUser(new User(email, name, passwordHash, false));

		if (!success) {
			sendServerSideErrorResponse(exchange);
			return;
		}


		emailConfirmationCodeMap.put(email, Randomizer.getEmailConfirmationNumberAndDateList());

		// TODO: Send an email and check

		// TODO: Remove the second value from map
		sendJsonResponse(exchange, HttpResponseStatus.OK, JsonUtil.toJson(Map.of("message", "Confirm your email within 10 minutes", "code", emailConfirmationCodeMap.get(email))));
	}

	private void confirmEmail(HttpExchange exchange) throws IOException, SQLException {
		@SuppressWarnings("unchecked")
		Map<String, ?> requestBody = getCheckedRequestBody(exchange, List.of("email", "confirmation_code"), Map.class);

		if (requestBody == null) return;

		String email = (String) requestBody.get("email");
		double confirmationCode = (double) requestBody.get("confirmation_code");

		List<Long> emailConfirmationCodeAndDate = emailConfirmationCodeMap.get(email);
		long now = Instant.now().toEpochMilli();
		int maxDifference = 10 * 60 * 1000;

		if (emailConfirmationCodeAndDate == null || emailConfirmationCodeAndDate.getFirst() != confirmationCode || now - emailConfirmationCodeAndDate.get(1) > maxDifference) {
			sendBadRequestResponse(exchange, "Wrong confirmation code");
			return;
		}

		boolean success = Db.user().confirmUser(email);
		if(!success) {
			sendServerSideErrorResponse(exchange);
			return;
		}

		emailConfirmationCodeMap.remove(email);

		sendJsonResponse(exchange, HttpResponseStatus.OK, JsonUtil.toJson(Map.of("message", "User email successfully confirmed")));
	}
}
