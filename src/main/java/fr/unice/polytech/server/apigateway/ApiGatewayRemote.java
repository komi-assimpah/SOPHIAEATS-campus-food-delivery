package fr.unice.polytech.server.apigateway;

import com.sun.net.httpserver.HttpServer;
import fr.unice.polytech.server.remote.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.logging.Logger;

import static fr.unice.polytech.server.httphandlers.HttpUtils.ServerPort.*;

public class ApiGatewayRemote {
    private static final String url = "http://localhost:";
    private static final int GATEWAY_PORT = API_GATEWAY_PORT; // Port de l'API Gateway

    static final String RESTAURANT_SERVICE_URL = url+RESTAURANT_SERVICE_PORT;
    final static String LOCATION_SERVICE_URL = url+LOCATION_SERVICE_PORT;
    final static String ORDER_SERVICE_URL = url+ORDER_SERVICE_PORT;
    final static String GROUP_ORDER_SERVICE_URL = url+GROUP_ORDER_SERVICE_PORT;
    final static String USER_SERVICE_URL = url+USER_SERVICE_PORT;
    private static final Logger logger = Logger.getLogger(ApiGatewayRemote.class.getName());

    public static void main(String[] args) throws IOException {
        // Lancer les serveurs de services orchestrés
        startServices();

        HttpServer server = HttpServer.create(new InetSocketAddress(GATEWAY_PORT), 0);


        server.createContext("/api/users", new ApiGatewayHandler(USER_SERVICE_URL));
        server.createContext("/api/restaurants", new ApiGatewayHandler(RESTAURANT_SERVICE_URL));
        server.createContext("/api/locations", new ApiGatewayHandler(LOCATION_SERVICE_URL));
        server.createContext("/api/orders", new ApiGatewayHandler(ORDER_SERVICE_URL));
        server.createContext("/api/groupOrders", new ApiGatewayHandler(GROUP_ORDER_SERVICE_URL));
        server.setExecutor(null);
        server.start();
        logger.info("API Gateway started on port " + GATEWAY_PORT);
    }


    static void startServices() {
        try {
            new Thread(() -> {
                try {
                    UserHttpServer.main(null);
                } catch (Exception e) {
                    logger.severe("Failed to start UserHttpServer: " + e.getMessage());
                }
            }).start();
            new Thread(() -> {
                try {
                    RestaurantHttpServer.main(null);
                } catch (Exception e) {
                    logger.severe("Failed to start RestaurantHttpServer: " + e.getMessage());
                }
            }).start();

            new Thread(() -> {
                try {
                    LocationHttpServer.main(null);
                } catch (Exception e) {
                    logger.severe("Failed to start LocationHttpServer: " + e.getMessage());
                }
            }).start();

            new Thread(() -> {
                try {
                    OrderHttpServer.main(null);
                } catch (Exception e) {
                    logger.severe("Failed to start OrderHttpServer: " + e.getMessage());
                }
            }).start();

            new Thread(() ->{
                try {
                    GroupOrderHttpServer.main(null);
                } catch (Exception e) {
                    logger.severe("Failed to start GroupOrderHttpServer: " + e.getMessage());
                }
            }).start();

            logger.info("All services started successfully.");
        } catch (Exception e) {
            logger.severe("Error while starting services: " + e.getMessage());
        }
    }


    public static void stopServices() {
        logger.info("Stopping all services...");
        
        try {
            // Arrêter les services dans l'ordre inverse du démarrage
            GroupOrderHttpServer.getServer().stop(0);
            logger.info("Group Order service stopped");
            
            OrderHttpServer.getServer().stop(0); 
            logger.info("Order service stopped");
            
            LocationHttpServer.getServer().stop(0);
            logger.info("Location service stopped");
            
            RestaurantHttpServer.getServer().stop(0);
            logger.info("Restaurant service stopped");
            
            logger.info("All services stopped successfully");
            
        } catch (Exception e) {
            logger.severe("Error while stopping services: " + e.getMessage());
        }
    }
}
