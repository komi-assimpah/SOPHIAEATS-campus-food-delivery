package fr.unice.polytech.server.httphandlers;


import com.sun.net.httpserver.HttpExchange;
import fr.unice.polytech.server.utils.QueryParams;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class HttpUtils {

    public static class ServerPort {
        public static final int RESTAURANT_SERVICE_PORT = 8000;
        public static final int LOCATION_SERVICE_PORT = 8001;
        public static final int GROUP_ORDER_SERVICE_PORT = 8002;
        public static final int ORDER_SERVICE_PORT = 8003;
        public static final int API_GATEWAY_PORT = 8080;
        public static final int USER_SERVICE_PORT = 8004;
    }

    public static class HttpStatusCode {
        public static final int OK = 200;
        public static final int CREATED = 201;
        public static final int NO_CONTENT = 204;
        public static final int BAD_REQUEST = 400;
        public static final int NOT_FOUND = 404;
        public static final int CONFLICT = 409;
        public static final int INTERNAL_SERVER_ERROR = 500;
        public static final int UNAUTHORIZED = 401;
    }

    public static class HttpHeader {
        public static final String CONTENT_TYPE = "Content-Type";
        public static final String APPLICATION_JSON = "application/json";
        public static final String TEXT_PLAIN = "text/plain";
    }

    public static List<QueryParams> parseQueryParams(String requestQuery) {
        List<QueryParams> queryParams = new ArrayList<>();
        if (requestQuery == null) {
            return queryParams;
        }
        for (String param : requestQuery.split("&")) {
            String[] pair = param.split("=");
            queryParams.add(new QueryParams(pair[0], pair[1]));
        }
        return queryParams;
    }

    public static void sendResponse(HttpExchange exchange, int created, String response) {
        exchange.getResponseHeaders().set(HttpHeader.CONTENT_TYPE, HttpHeader.APPLICATION_JSON);
        try {
            exchange.sendResponseHeaders(created, response.getBytes().length);
            exchange.getResponseBody().write(response.getBytes());
            exchange.getResponseBody().close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendNotFoundResponse(HttpExchange exchange) {
        String response = "{\"error\":\"Not found\"}";
        sendResponse(exchange, HttpStatusCode.NOT_FOUND, response);
    }

    public static void sendErrorResponse(HttpExchange exchange, int statusCode, String errorMessage) throws IOException, IOException {
        String jsonResponse = String.format("{\"error\":\"%s\"}", errorMessage);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, jsonResponse.getBytes().length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(jsonResponse.getBytes());
        }
    }


    public static boolean configureCors(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*"); // Remplacez par votre origine cliente
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Accept, X-Requested-With, Content-Type, Content-Length, Accept-Encoding, X-CSRF-Token, Authorization");

        if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Accept, Content-Type, Authorization");
            exchange.sendResponseHeaders(HttpUtils.HttpStatusCode.NO_CONTENT, -1);
            return true;
        }
        return false;
    }


}
