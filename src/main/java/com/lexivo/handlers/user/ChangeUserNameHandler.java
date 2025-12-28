package com.lexivo.handlers.user;

import com.lexivo.db.Db;
import com.lexivo.logger.Logger;
import com.lexivo.util.HttpResponseStatus;
import com.lexivo.util.StandardResponse;
import com.lexivo.util.ValidationUtil;
import org.jandle.api.annotations.HttpRequestHandler;
import org.jandle.api.http.Handler;
import org.jandle.api.http.Request;
import org.jandle.api.http.RequestMethod;
import org.jandle.api.http.Response;

import java.io.IOException;
import java.util.List;

@HttpRequestHandler(method = RequestMethod.PUT, path = "/user/change_name")
public class ChangeUserNameHandler implements Handler {
	private final Logger logger = new Logger();

	@Override
	public void handle(Request request, Response response) throws IOException {
		var requestBody = request.getBodyJson();

		String email = (String) requestBody.get("email");
		String name = (String) requestBody.get("name");

		try {
			String[] missingData = ValidationUtil.getMissingStrings(new String[]{email, name}, List.of("Email is missing", "Name is missing"));
			if (missingData.length > 0) {
				StandardResponse.jsonWithMessages(response, HttpResponseStatus.BAD_REQUEST, missingData);
				return;
			}
			if (!ValidationUtil.isNameValid(name)) {
				StandardResponse.jsonWithMessages(response, HttpResponseStatus.BAD_REQUEST, ValidationUtil.NAME_REQUIREMENTS);
				return;
			}

			Db.users().updateUserName(email.trim(), name.trim());

			response.sendStatus(HttpResponseStatus.OK);
		}
		catch (Exception e) {
			logger.exception(e, email, new String[]{e.getMessage()});
			response.sendStatus(HttpResponseStatus.SERVER_SIDE_ERROR);
		}
	}
}
