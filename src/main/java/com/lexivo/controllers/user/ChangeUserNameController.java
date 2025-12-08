package com.lexivo.controllers.user;

import com.lexivo.controllers.Controller;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.sql.SQLException;

public class ChangeUserNameController extends Controller {
	public ChangeUserNameController(String path) {
		super(path);
	}

	@Override
	protected void put(HttpExchange exchange) throws IOException, SQLException {
		super.put(exchange);
	}
}
