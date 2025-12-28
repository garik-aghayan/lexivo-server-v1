package com.lexivo.handlers.user;

import com.lexivo.db.Db;
import com.lexivo.logger.Logger;
import com.lexivo.util.*;
import org.jandle.api.annotations.HttpRequestHandler;
import org.jandle.api.http.Handler;
import org.jandle.api.http.Request;
import org.jandle.api.http.RequestMethod;
import org.jandle.api.http.Response;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@HttpRequestHandler(method = RequestMethod.POST, path = "/user/recover_password")
public class RecoverPasswordHandler implements Handler {
	private final Logger logger = new Logger();

	@Override
	public void handle(Request request, Response response) throws IOException {
		var requestBody = request.getBodyJson();
		String email = (String) requestBody.get("email");

		try {
			String[] missingData = ValidationUtil.getMissingStrings(new String[]{email}, List.of("Email is missing"));
			if (missingData.length > 0) {
				StandardResponse.jsonWithMessages(response, HttpResponseStatus.BAD_REQUEST, missingData);
				return;
			}

			String newPassword = Randomizer.generateUserPassword();
			String newPasswordHash = BCrypt.hashpw(newPassword, BCrypt.gensalt());
			Db.users().updateUserPassword(email, newPasswordHash);

			Email.sendRecoveredPassword(email, newPassword);

			StandardResponse.jsonWithMessages(response, HttpResponseStatus.OK, "Check your email");
		}
		catch (Exception e) {
			logger.exception(e, email, new String[]{e.getMessage()});
			response.sendStatus(HttpResponseStatus.SERVER_SIDE_ERROR);
		}
	}
}
