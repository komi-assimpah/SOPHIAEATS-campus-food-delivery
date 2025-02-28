package fr.unice.polytech.server.remote;

import com.sun.net.httpserver.HttpServer;
import fr.unice.polytech.application.usecase.*;
import fr.unice.polytech.application.usecase.interfaces.IOrderPlacementCoordinator;
import fr.unice.polytech.infrastructure.external.PaymentService;
import fr.unice.polytech.infrastructure.repository.firebase.LocationRepository;
import fr.unice.polytech.infrastructure.repository.firebase.OrderRepository;
import fr.unice.polytech.infrastructure.repository.firebase.RestaurantRepository;
import fr.unice.polytech.infrastructure.repository.firebase.UserRepository;
import fr.unice.polytech.server.httphandlers.HttpUtils;
import fr.unice.polytech.server.httphandlers.OrderHttpHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.logging.Logger;

public class OrderHttpServer {
    static Logger logger = Logger.getLogger(OrderHttpServer.class.getName());

    static HttpServer server=null;
    public static void main(String[] args) {
        try {
            IOrderPlacementCoordinator orderPlacementCoordinator = new OrderCoordinator(
                    new UserService(new UserRepository()),
                    new RestaurantService(new RestaurantRepository()),
                    new RestaurantCapacityService(),
                    new OrderService(new OrderRepository()),
                    new PaymentService(),
                    new LocationService(new LocationRepository())
            );
            OrderHttpServer.startServer(HttpUtils.ServerPort.ORDER_SERVICE_PORT, orderPlacementCoordinator);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static HttpServer startServer(int port, IOrderPlacementCoordinator orderPlacementCoordinator) throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/api/orders", new OrderHttpHandler(orderPlacementCoordinator));
        server.setExecutor(null); // creates a default executor
        server.start();
        logger.info("Server started on port " + port);
        return server;
    }

    public static HttpServer getServer() {
        return server;
    }
}
