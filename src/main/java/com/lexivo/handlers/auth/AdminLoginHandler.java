package com.lexivo.handlers.auth;

import com.lexivo.db.Db;
import com.lexivo.enums.UserRole;
import com.lexivo.logger.Logger;
import com.lexivo.schema.User;
import com.lexivo.util.*;
import org.jandle.api.annotations.HttpRequestHandler;
import org.jandle.api.http.Handler;
import org.jandle.api.http.Request;
import org.jandle.api.http.RequestMethod;
import org.jandle.api.http.Response;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@HttpRequestHandler(method = RequestMethod.POST, path = "/auth/admin_login")
public class AdminLoginHandler implements Handler {
	private final Logger logger = new Logger();

	@Override
	public void handle(Request request, Response response) throws IOException {
		var requestBody = request.getBodyJson();
		String email = (String) requestBody.get("email");
		String password = (String) requestBody.get("password");
		String adminPassword = (String) requestBody.get("adminPassword");

		try {
			logger.warning(email, new String[]{"Admin login attempt"});

			String[] missingData = ValidationUtil.getMissingStrings(new String[]{email, password, adminPassword}, List.of("Email is missing", "Password is missing", "Admin password is missing"));
			if (missingData.length > 0) {
				StandardResponse.jsonWithMessages(response, HttpResponseStatus.BAD_REQUEST, missingData);
			}

			User user = Db.users().getByEmail(email.trim());

			if (!credentialsCorrect(user, password, adminPassword)) {
				StandardResponse.incorrectCredentials(response);
				return;
			}

			logger.warning(email, new String[]{"Admin login"});
			Email.sendEmailToAdmin("Admin Login", "Date - " + DateAndTime.getFormattedDateAndTimeFromMs(System.currentTimeMillis()) + ".\nIP - " + request.getRemoteAddress().getHostString());

			if (!user.isConfirmed()) {
				StandardResponse.sendConfirmationCode(response, user);
				return;
			}

			Map<String, String> userData = new HashMap<>();
			userData.put(JwtUtil.KEY_ACCESS_TOKEN, JwtUtil.createHMAC256Token(email, UserRole.ADMIN, 15));
			userData.put("email", user.getEmail());
			userData.put("name", user.getName());

			response
					.status(HttpResponseStatus.OK)
					.sendJson(userData);
		}
		catch (Exception e) {
			logger.exception(e, email, new String[]{e.getMessage()});
			response.sendStatus(HttpResponseStatus.SERVER_SIDE_ERROR);
		}
	}

	private boolean credentialsProvided(String email, String password, String adminPassword, Response response) throws IOException {
		if (email == null || email.isEmpty()) {
			StandardResponse.jsonWithMessages(response, HttpResponseStatus.BAD_REQUEST, "Email is not provided");
			return false;
		}

		if (password == null || password.isBlank()) {
			StandardResponse.jsonWithMessages(response, HttpResponseStatus.BAD_REQUEST, "Password is not provided");
			return false;
		}

		if (adminPassword == null || adminPassword.isBlank()) {
			StandardResponse.jsonWithMessages(response, HttpResponseStatus.BAD_REQUEST, "Admin password is not provided");
			return false;
		}

		return true;
	}

	private boolean credentialsCorrect(User user, String password, String adminPassword) {
		if (!AuthUtil.isUserPasswordCorrect(user, password)) return false;

		String adminPasswordHash = System.getenv("ADMIN_PASSWORD_HASH");
		try {
			return BCrypt.checkpw(adminPassword, adminPasswordHash);
		}
		catch (RuntimeException ignore) {
			return false;
		}

	}
}
