package com.lexivo;

import com.lexivo.controllers.AuthController;
import com.lexivo.controllers.NotFoundController;
import com.lexivo.enums.UserRole;
import com.lexivo.route.Route;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class App {
    public static final String BASE_URL = "/api/v1";
    public static void main(String[] args) {
        try {
            int PORT;
            String envPort = System.getenv("PORT");
            PORT = envPort == null ? 8080 : Integer.parseInt(envPort);

            HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

            initRoutes(server);

            server.setExecutor(null);
            server.start();
            // TODO: Replace with a proper logger
            System.out.println("Server running on port " + PORT);
        }
        catch (IOException ioe) {
//			TODO: Replace with a proper logger
            System.err.println(ioe.getMessage());
        }
    }

    private static void initRoutes(HttpServer server) {
        new Route(server, BASE_URL + "/auth", AuthController.class, UserRole.PUBLIC);
        new Route(server, "/", NotFoundController.class, UserRole.PUBLIC);
    }
}
