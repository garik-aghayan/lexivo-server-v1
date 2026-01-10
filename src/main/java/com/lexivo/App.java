package com.lexivo;

import com.lexivo.enums.UserRole;
import com.lexivo.filters.AuthVerifierFilter;
import com.lexivo.handlers.auth.AdminLoginHandler;
import com.lexivo.handlers.auth.RefreshTokenHandler;
import com.lexivo.handlers.auth.SignupHandler;
import com.lexivo.handlers.auth.UserLoginHandler;
import com.lexivo.handlers.dict.GetAllDictHandler;
import com.lexivo.handlers.dict.GetDictByIdHandler;
import com.lexivo.handlers.lang.GetAllLangHandler;
import com.lexivo.handlers.logs.GetAllLogsHandler;
import com.lexivo.handlers.user.ChangePasswordHandler;
import com.lexivo.handlers.user.ChangeUserNameHandler;
import com.lexivo.handlers.user.ConfirmEmailHandler;
import com.lexivo.handlers.user.RecoverPasswordHandler;
import com.lexivo.logger.Logger;
import com.sun.net.httpserver.HttpServer;
import org.jandle.api.JandleApplication;
import org.jandle.api.cors.Cors;
import org.jandle.api.cors.CorsConfig;
import org.jandle.api.ratelimiter.RateLimiter;

import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;

public class App {
    public static final String BASE_URL = "/api/v1";
    public static int PORT = 8001;
    public static void main(String[] args) {
        Logger logger = new Logger();
        try {
            setPort();
            HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
            JandleApplication app = new JandleApplication(server, BASE_URL);

            registerGlobalFilters(app);
            registerHandlers(app);

            app.start(() -> logger.info("Server running on port " + PORT + " ..."));
        }
        catch (Exception e) {
            logger.exception(e, new String[]{"Exception in App"});
        }
	}

    private static void setPort() {
        String envPort = System.getenv("PORT");
        if (envPort != null)
            PORT = Integer.parseInt(envPort);
    }

    private static void registerGlobalFilters(JandleApplication app) {
        CorsConfig corsConfig = new CorsConfig();
        corsConfig.setAllowCredentials(true);
        Cors cors = new Cors(corsConfig);

        RateLimiter rateLimiter = new RateLimiter(5, (double) 5 /60);

        app.setGlobalFilters(
                rateLimiter,
                cors
        );
    }

    private static void registerHandlers(JandleApplication app) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        app.registerHandlers(
            new UserLoginHandler(),
            new AdminLoginHandler(),
            new SignupHandler(),
            new RefreshTokenHandler(),
            new RecoverPasswordHandler(),
            new ConfirmEmailHandler(),
            new ChangePasswordHandler(),
            new ChangeUserNameHandler(),
            new GetAllDictHandler(),
            new GetDictByIdHandler()
        );

        // Get all logs
        app.registerHandler(
                new GetAllLogsHandler(),
                new AuthVerifierFilter(UserRole.ADMIN)
        );

        // Get all lang list
        app.registerHandler(
                new GetAllLangHandler(),
                new AuthVerifierFilter(UserRole.ADMIN)
        );
    }
}
