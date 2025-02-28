package fr.unice.polytech.server;

import com.sun.net.httpserver.HttpServer;
import fr.unice.polytech.application.port.IOrderRepository;
import fr.unice.polytech.application.usecase.OrderCoordinator;
import fr.unice.polytech.application.usecase.RestaurantService;
import fr.unice.polytech.application.usecase.interfaces.IOrderPlacementCoordinator;
import fr.unice.polytech.application.usecase.interfaces.IRestaurantService;
import fr.unice.polytech.infrastructure.repository.inmemory.OrderRepository;
import fr.unice.polytech.server.httphandlers.HttpUtils;
import fr.unice.polytech.server.httphandlers.OrderHttpHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OrderHttpServer {
    static Logger logger = Logger.getLogger(OrderHttpServer.class.getName());
    static HttpServer server=null;
    public static void main(String[] args) {
        try {
            IOrderPlacementCoordinator orderPlacementCoordinator = new OrderCoordinator();
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
        logger.info(() -> "Server started on port " + port);
        return server;
    }

    public static HttpServer getServer() {
        return server;
    }
}
