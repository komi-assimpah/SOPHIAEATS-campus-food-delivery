package fr.unice.polytech.server.remote;

import com.sun.net.httpserver.HttpServer;
import fr.unice.polytech.application.port.IUserRepository;
import fr.unice.polytech.application.usecase.UserService;
import fr.unice.polytech.application.usecase.interfaces.IUserService;
import fr.unice.polytech.infrastructure.repository.firebase.UserRepository;
import fr.unice.polytech.server.httphandlers.HttpUtils;
import fr.unice.polytech.server.httphandlers.UserHttpHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.logging.Logger;

public class UserHttpServer {

    static Logger logger = Logger.getLogger(UserHttpServer.class.getName());
    static HttpServer server=null;

    public static void main(String[] args) {
        try {
            IUserRepository userRepository = new UserRepository();
            IUserService userService = new UserService(userRepository);
            UserHttpServer.startServer(HttpUtils.ServerPort.USER_SERVICE_PORT, userService);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static HttpServer startServer(int port, IUserService userService) throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/api/users", new UserHttpHandler(userService));
        server.setExecutor(null); // creates a default executor
        server.start();
        logger.info("Server started on port " + port);
        return server;
    }

    public static HttpServer getServer() {
        return server;
    }
}
