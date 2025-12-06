package com.lexivo.controllers;

import com.lexivo.db.Db;
import com.lexivo.schema.EmailConfirmationCodeData;
import com.lexivo.schema.User;
import com.lexivo.util.*;
import com.sun.net.httpserver.HttpExchange;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AuthController extends Controller {

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

		if (pathsEqual(routeBasePath + "/recover_password", path)) {
			recoverPassword(exchange);
			return;
		}

		sendRouteDoesNotExistResponse(exchange);
	}

	private void login(HttpExchange exchange) throws IOException, SQLException {
		User requestBody = RequestDataCheck.getCheckedRequestBody(exchange, List.of("email", "password"), User.class);

		if (requestBody == null) return;

		String email = requestBody.getEmail();
		email = email == null ? null : email.trim();
		String password = requestBody.getPassword();

		User user = Db.users().getByEmail(email);
		if (user == null) {
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
			sendConfirmationCodeEmail(exchange, user, HttpResponseStatus.FORBIDDEN);
			return;
		}

		Map<String, String> jsonMap = new HashMap<>();
		jsonMap.put(JwtUtil.KEY_ACCESS_TOKEN, JwtUtil.createAccessToken(email));
		jsonMap.put(JwtUtil.KEY_REFRESH_TOKEN, JwtUtil.createRefreshToken(email));

		sendJsonResponse(exchange, HttpResponseStatus.OK, JsonUtil.toJson(jsonMap));
	}

	private void signup(HttpExchange exchange) throws IOException, SQLException {
		User requestBody = RequestDataCheck.getCheckedRequestBody(exchange, List.of("name", "email", "password"), User.class);
		if (requestBody == null) return;

		String name = requestBody.getName();
		String email = requestBody.getEmail();
		String password = requestBody.getPassword();

		User user = Db.users().getByEmail(email);
		if (user != null) {
			sendBadRequestResponse(exchange, "User with the given email already exists");
			return;
		}

		boolean passwordMeetsRequirements = RequestDataCheck.doesPasswordMeetRequirements(password);
		if (!passwordMeetsRequirements) {
			sendBadRequestResponse(exchange, "Password must have", "8-32 characters", "at least one upper case letter", "at least on lower case letter", "at least one number");
			return;
		}

		String passwordHash = BCrypt.hashpw(password, BCrypt.gensalt());
		User newUser = new User(email, name, passwordHash, false);
		boolean success = Db.users().addUser(newUser);

		if (!success) {
			sendServerSideErrorResponse(exchange);
			return;
		}

		sendConfirmationCodeEmail(exchange, newUser, HttpResponseStatus.OK);
	}

	private void confirmEmail(HttpExchange exchange) throws IOException, SQLException {
		@SuppressWarnings("unchecked")
		Map<String, ?> requestBody = RequestDataCheck.getCheckedRequestBody(exchange, List.of("email", "confirmation_code"), Map.class);

		if (requestBody == null) return;
		String email = (String) requestBody.get("email");
		String confirmationCode = (String) requestBody.get("confirmation_code");

		EmailConfirmationCodeData entry = Db.emailConfirmationCodes().getByEmail(email);
		long now = System.currentTimeMillis();

		if (entry == null || !entry.getCode().equals(confirmationCode) || now > entry.getExpiresAt()) {
			sendBadRequestResponse(exchange, "Wrong confirmation code");
			return;
		}

		boolean success = Db.users().confirmUser(email);
		if(!success) {
			sendServerSideErrorResponse(exchange);
			return;
		}

		Db.emailConfirmationCodes().deleteWhereEmail(email);

		sendJsonResponse(exchange, HttpResponseStatus.OK, JsonUtil.toJson(Map.of("message", "User email successfully confirmed")));
	}

	private void recoverPassword(HttpExchange exchange) throws IOException, SQLException {
//		TODO:
		sendRouteDoesNotExistResponse(exchange);
	}

	private void sendConfirmationCodeEmail(HttpExchange exchange, User user, int responseCode) throws SQLException, IOException {
		EmailConfirmationCodeData codeData = new EmailConfirmationCodeData(user.getEmail(), Randomizer.getEmailConfirmationCode());
		boolean success = Db.emailConfirmationCodes().addConfirmationCode(codeData);
		if (!success) {
			sendServerSideErrorResponse(exchange);
			return;
		};

		Email.sendConfirmationCode(user.getEmail(), codeData.getCode());

		sendResponseWithMessage(exchange, responseCode, "Confirm your email within 10 minutes");
	}
}
