package com.lexivo.handlers.user;

import com.lexivo.db.Db;
import com.lexivo.logger.Logger;
import com.lexivo.schema.EmailConfirmationCodeData;
import com.lexivo.util.HttpResponseStatus;
import com.lexivo.util.StandardResponse;
import com.lexivo.util.ValidationUtil;
import org.jandle.api.annotations.HttpRequestHandler;
import org.jandle.api.http.Handler;
import org.jandle.api.http.Request;
import org.jandle.api.http.RequestMethod;
import org.jandle.api.http.Response;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@HttpRequestHandler(method = RequestMethod.POST, path = "/user/confirm_email")
public class ConfirmEmailHandler implements Handler {
	private final Logger logger = new Logger();

	@Override
	public void handle(Request request, Response response) throws IOException {
		var requestBody = request.getBodyJson();

		String email = (String) requestBody.get("email");
		String confirmationCode = (String) requestBody.get("confirmation_code");

		try {
			String[] missingData = ValidationUtil.getMissingStrings(new String[]{email, confirmationCode}, List.of("Email is missing", "Incorrect confirmation code"));
			if (missingData.length > 0) {
				StandardResponse.jsonWithMessages(response, HttpResponseStatus.BAD_REQUEST, missingData);
				return;
			}

			EmailConfirmationCodeData entry = Db.emailConfirmationCodes().getByEmail(email);
			long now = System.currentTimeMillis();

			if (entry == null || !entry.getCode().equals(confirmationCode) || now > entry.getExpiresAt()) {
				StandardResponse.jsonWithMessages(response, HttpResponseStatus.BAD_REQUEST, "Incorrect confirmation code");
				return;
			}

			boolean success = Db.users().confirmUser(email);
			if(!success) {
				logger.exception(email, new String[]{"User could not be confirmed"});
				response.sendStatus(HttpResponseStatus.SERVER_SIDE_ERROR);
				return;
			}

			Db.emailConfirmationCodes().deleteWhereEmail(email);

			StandardResponse.jsonWithMessages(response, HttpResponseStatus.OK, "Email successfully confirmed");
		}
		catch (Exception e) {
			logger.exception(e, email, new String[]{e.getMessage()});
			response.sendStatus(HttpResponseStatus.SERVER_SIDE_ERROR);
		}
	}
}
