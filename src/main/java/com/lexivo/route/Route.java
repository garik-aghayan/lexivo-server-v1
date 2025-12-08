package com.lexivo.route;

import com.lexivo.controllers.Controller;
import com.lexivo.filters.RateLimiter;
import com.lexivo.schema.Log;
import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpServer;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class Route {
	public Route(HttpServer server, String basePath, Class<? extends Controller> controllerClass, Filter[] filters) {
		try {
			Constructor<? extends Controller> controllerConstructor = controllerClass.getConstructor(String.class);
			Controller controller = controllerConstructor.newInstance(basePath);

			var context1 = server.createContext(basePath, controller);
			var context2 = server.createContext(context1.getPath() + "/", controller);

			for (var filter : filters) {
				context1.getFilters().add(filter);
				context2.getFilters().add(filter);
			}

			context1.getFilters().add(new RateLimiter());
			context2.getFilters().add(new RateLimiter());
		}
		catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
			Log.exception("Exception in route.Route", e.getClass().getName(), e.getMessage());
		}
	}

	public Route(HttpServer server, String basePath, Class<? extends Controller> controllerClass) {
		this(server, basePath, controllerClass, new Filter[]{});
	}
}
