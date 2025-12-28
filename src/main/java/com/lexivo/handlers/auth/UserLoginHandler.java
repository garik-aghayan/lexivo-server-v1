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

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@HttpRequestHandler(method = RequestMethod.POST, path = "/auth/login")
public class UserLoginHandler implements Handler {
	private final Logger logger = new Logger();

	@Override
	public void handle(Request request, Response response) throws IOException {
		var requestBody = request.getBodyJson();
		String email = (String) requestBody.get("email");
		String password = (String) requestBody.get("password");

		try {
			String[] missingData = ValidationUtil.getMissingStrings(new String[]{email, password}, List.of("Email is missing", "Password is missing"));
			if (missingData.length > 0) {
				StandardResponse.jsonWithMessages(response, HttpResponseStatus.BAD_REQUEST, missingData);
				return;
			}

			User user = Db.users().getByEmail(email.trim());

			if (!AuthUtil.isUserPasswordCorrect(user, password)) {
				StandardResponse.incorrectCredentials(response);
				return;
			}

			if (!user.isConfirmed()) {
				StandardResponse.sendConfirmationCode(response, user);
				return;
			}

			Map<String, String> userData = new HashMap<>();
			userData.put(JwtUtil.KEY_ACCESS_TOKEN, JwtUtil.createHMAC256Token(email, UserRole.USER, 5));
			userData.put(JwtUtil.KEY_REFRESH_TOKEN, JwtUtil.createHMAC256Token(email, UserRole.USER, DateAndTime.getMinutesInDays(30)));
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
}
