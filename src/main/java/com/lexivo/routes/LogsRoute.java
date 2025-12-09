package com.lexivo.routes;

import com.lexivo.controllers.logs.LogsController;
import com.lexivo.enums.UserRole;
import com.lexivo.filters.AuthVerifier;
import com.lexivo.filters.QueryParamsToMap;
import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpServer;

public class LogsRoute  implements RouteWithServer {
	private final String path;
	public LogsRoute(String basePath) {
		this.path = basePath + "/logs";
	}

	@Override
	public void withServer(HttpServer server) {
		new Route(server, path, LogsController.class, new Filter[]{new AuthVerifier(UserRole.ADMIN), new QueryParamsToMap()});
	}
}
