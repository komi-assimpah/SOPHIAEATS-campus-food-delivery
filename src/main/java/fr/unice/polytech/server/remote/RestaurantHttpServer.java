package fr.unice.polytech.server.remote;

import com.sun.net.httpserver.HttpServer;
import fr.unice.polytech.application.port.IRestaurantRepository;
import fr.unice.polytech.application.usecase.RestaurantCapacityService;
import fr.unice.polytech.application.usecase.RestaurantScheduleManager;
import fr.unice.polytech.application.usecase.RestaurantService;
import fr.unice.polytech.application.usecase.interfaces.IRestaurantCapacityService;
import fr.unice.polytech.application.usecase.interfaces.IRestaurantScheduleManager;
import fr.unice.polytech.application.usecase.interfaces.IRestaurantService;
import fr.unice.polytech.infrastructure.repository.firebase.RestaurantRepository;
import fr.unice.polytech.server.httphandlers.HttpUtils;
import fr.unice.polytech.server.httphandlers.RestaurantHttpHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.logging.Logger;

public class RestaurantHttpServer {
    static HttpServer server=null;
    static Logger logger = Logger.getLogger(RestaurantHttpServer.class.getName());

    public static void main(String[] args) {
        try {
            IRestaurantRepository restaurantRepository = new RestaurantRepository();
            IRestaurantService restaurantService = new RestaurantService(restaurantRepository);
            IRestaurantScheduleManager restaurantScheduleManager = new RestaurantScheduleManager(restaurantRepository);
            IRestaurantCapacityService restaurantCapacityService = new RestaurantCapacityService();

            RestaurantHttpServer.startServer(HttpUtils.ServerPort.RESTAURANT_SERVICE_PORT, restaurantService, restaurantScheduleManager, restaurantCapacityService);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static HttpServer startServer(int port, IRestaurantService restaurantService, IRestaurantScheduleManager restaurantScheduleManager, IRestaurantCapacityService restaurantCapacityService) throws IOException {

        server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/api/restaurant", new RestaurantHttpHandler(restaurantService, restaurantScheduleManager, restaurantCapacityService));
        server.setExecutor(null); // creates a default executor
        server.start();
        logger.info("Server started on port " + port);
        return server;
    }

    public static void stopServer(HttpServer server) {
        if(server != null){
            server.stop(0);
            server = null;
            logger.info("location stopped");
        }
    }

    public static HttpServer getServer() {
        return server;
    }

}
