package com.lexivo;

import com.lexivo.routes.AuthRoute;
import com.lexivo.routes.LogsRoute;
import com.lexivo.routes.NotFoundRoute;
import com.lexivo.routes.UserRoute;
import com.lexivo.schema.Log;
import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;
import java.util.Arrays;

public class App {
    public static final String BASE_URL = "/api/v1";
    public static void main(String[] args) {
        try {
            int PORT;
            String envPort = System.getenv("PORT");
            PORT = envPort == null ? 8001 : Integer.parseInt(envPort);

            HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

            initRoutes(server);

            server.setExecutor(null);

            server.start();

            Log.info("Server running on port " + PORT);
        }
        catch (Exception e) {
            Log.exception("Exception in App", Arrays.stream(e.getStackTrace()).toList().toString(), e.getMessage());
        }
	}

    private static void initRoutes(HttpServer server) {
        new AuthRoute(BASE_URL).withServer(server);
        new UserRoute(BASE_URL).withServer(server);
        new LogsRoute(BASE_URL).withServer(server);
        new NotFoundRoute().withServer(server);
    }
}
