package com.lexivo.routes;

import com.lexivo.controllers.Controller;
import com.lexivo.enums.UserRole;
import com.lexivo.filters.AuthFilter;
import com.sun.net.httpserver.HttpServer;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class Route {
	public Route(HttpServer server, String basePath, Class<? extends Controller> controllerClass, UserRole minimumRole) {
		try {
			Constructor<? extends Controller> controllerConstructor = controllerClass.getConstructor(String.class);
			Controller controller = controllerConstructor.newInstance(basePath);

			var context1 = server.createContext(basePath, controller);
			var context2 = server.createContext(context1.getPath() + "/", controller);

			context1.getFilters().add(new AuthFilter(minimumRole));
			context2.getFilters().add(new AuthFilter(minimumRole));
		}
		catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
			// TODO: Replace with a proper logger
			System.err.println(e.getMessage());
		}
	}
}
