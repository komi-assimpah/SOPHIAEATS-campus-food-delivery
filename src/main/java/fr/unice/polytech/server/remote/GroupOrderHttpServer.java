package fr.unice.polytech.server.remote;

import com.sun.net.httpserver.HttpServer;
import fr.unice.polytech.application.usecase.GroupOrderService;
import fr.unice.polytech.application.usecase.interfaces.IGroupOrderService;
import fr.unice.polytech.infrastructure.repository.firebase.GroupOrderRepository;
import fr.unice.polytech.server.httphandlers.GroupOrderHttpHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.logging.Logger;

import static fr.unice.polytech.server.httphandlers.HttpUtils.ServerPort.GROUP_ORDER_SERVICE_PORT;

public class GroupOrderHttpServer {


    static HttpServer server=null;
        static Logger logger = Logger.getLogger(GroupOrderHttpServer.class.getName());
    
        public static void main(String[] args) {
            try {
                GroupOrderService groupOrderService = new GroupOrderService(new GroupOrderRepository());
                startServer(GROUP_ORDER_SERVICE_PORT, groupOrderService);
            } catch (IOException e) {
                logger.severe("Failed to start server. Error: " + e.getMessage());
            }
        }
    
        public static HttpServer startServer(int port, IGroupOrderService groupOrderService) throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/api/groupOrders", new GroupOrderHttpHandler(groupOrderService));
        server.setExecutor(null);
        server.start();

        logger.info("Server started on port " + port);
        return server;
    }

    public static HttpServer getServer() {
        return server;
    }

}