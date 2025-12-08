package com.lexivo.routes;

import com.lexivo.controllers.Controller;
import com.lexivo.filters.RateLimiter;
import com.lexivo.schema.Log;
import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpServer;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

class Route {
	public Route(HttpServer server, String path, Class<? extends Controller> controllerClass, Filter[] filters, List<Object> constructorParams) {
		Object[][] paramsAndParamClasses = getConstructorParamsAndParamClasses(path, constructorParams);

		try {
			Constructor<? extends Controller> controllerConstructor = controllerClass.getConstructor((Class<?>[]) paramsAndParamClasses[1]);
			Controller controller = controllerConstructor.newInstance(paramsAndParamClasses[0]);

			var context1 = server.createContext(path, controller);
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

	public Route(HttpServer server, String path, Class<? extends Controller> controllerClass, Filter[] filters) {
		this(server, path, controllerClass, filters, List.of());
	}

	public Route(HttpServer server, String path, Class<? extends Controller> controllerClass, List<Object> constructorParams) {
		this(server, path, controllerClass, new Filter[]{}, constructorParams);
	}

	public Route(HttpServer server, String path, Class<? extends Controller> controllerClass) {
		this(server, path, controllerClass, new Filter[]{}, List.of());
	}

	private Object[][] getConstructorParamsAndParamClasses(String path, List<Object> constructorParams) {
		Object[] params = new Object[constructorParams.size() + 1];
		Class<?>[] paramClasses = new Class[constructorParams.size() + 1];
		params[0] = path;
		paramClasses[0] = String.class;
		for (int i = 0; i < constructorParams.size(); i++) {
			var param = constructorParams.get(i);
			params[i + 1] = param;
			paramClasses[i + 1] = param.getClass();
		}

		return new Object[][]{params, paramClasses};
	}
}
