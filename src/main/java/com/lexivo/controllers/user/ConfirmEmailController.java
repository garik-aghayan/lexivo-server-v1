package com.lexivo.controllers.user;

import com.lexivo.controllers.Controller;
import com.lexivo.db.Db;
import com.lexivo.schema.EmailConfirmationCodeData;
import com.lexivo.util.HttpResponseStatus;
import com.lexivo.util.JsonUtil;
import com.lexivo.util.RequestData;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class ConfirmEmailController extends Controller {
	public ConfirmEmailController(String path) {
		super(path);
	}

	@Override
	protected void post(HttpExchange exchange) throws IOException, SQLException {
		@SuppressWarnings("unchecked")
		Map<String, ?> requestBody = RequestData.getCheckedRequestBody(exchange, List.of("email", "confirmation_code"), Map.class);

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
}
