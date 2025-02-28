package fr.unice.polytech.server;

import com.sun.net.httpserver.HttpServer;
import fr.unice.polytech.application.port.ILocationRepository;
import fr.unice.polytech.application.usecase.LocationService;
import fr.unice.polytech.application.usecase.interfaces.ILocationService;
import fr.unice.polytech.infrastructure.repository.inmemory.LocationRepository;

import fr.unice.polytech.server.httphandlers.HttpUtils;
import fr.unice.polytech.server.httphandlers.LocationHttpHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.logging.Logger;

public class LocationHttpServer {
    static Logger logger = Logger.getLogger(LocationHttpServer.class.getName());

    static HttpServer server=null;
    public static void main(String[] args) {
        try {
            ILocationRepository locationRepository = new LocationRepository();
            ILocationService locationService = new LocationService(locationRepository);
            LocationHttpServer.startServer(HttpUtils.ServerPort.LOCATION_SERVICE_PORT, locationService);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static HttpServer startServer(int port, ILocationService locationService) throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/api/locations", new LocationHttpHandler(locationService));
        server.setExecutor(null); // creates a default executor
        server.start();
        logger.info("location server started on port " + port);
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
