package fr.unice.polytech.server.apigateway;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import fr.unice.polytech.server.httphandlers.HttpUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

import static fr.unice.polytech.server.httphandlers.HttpUtils.sendErrorResponse;

public class ApiGatewayHandler implements HttpHandler {
    private final String targetServiceUrl;
    Logger logger = Logger.getLogger(ApiGatewayHandler.class.getName());

    public ApiGatewayHandler(String targetServiceUrl) {
        this.targetServiceUrl = targetServiceUrl;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Gestion CORS

        if (HttpUtils.configureCors(exchange)) return;

        String requestMethod = exchange.getRequestMethod();
        String requestPath = exchange.getRequestURI().getPath();
        String query = exchange.getRequestURI().getQuery();

        String fullUrl = targetServiceUrl + requestPath + (query != null ? "?" + query : "");

        HttpURLConnection connection = null;
        logger.info(() -> "Attempting to connect to: " + fullUrl);
        try {
            connection = (HttpURLConnection) new URL(fullUrl).openConnection();
            connection.setRequestMethod(requestMethod);

            copyHeaders(exchange, connection);

            logger.info(() -> "Connecting to: " + fullUrl);

            if ("POST".equalsIgnoreCase(requestMethod) || "PUT".equalsIgnoreCase(requestMethod) || "PATCH".equalsIgnoreCase(requestMethod)) {
                connection.setDoOutput(true);
                try (OutputStream out = connection.getOutputStream()) {
                    exchange.getRequestBody().transferTo(out);
                }
            }

            int responseCode = connection.getResponseCode();
            logger.info(() -> "Response code from target service: " + responseCode);


            InputStream inputStream = (responseCode >= 200 && responseCode < 300)
                    ? connection.getInputStream() // Réponse réussie
                    : connection.getErrorStream(); // Réponse d'erreur

            if (inputStream != null) {

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                inputStream.transferTo(baos);
                byte[] responseBytes = baos.toByteArray();

                String contentType = connection.getHeaderField("Content-Type");
                if (contentType != null && contentType.contains("application/json")) {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        mapper.enable(SerializationFeature.INDENT_OUTPUT);
                        Object jsonObject = mapper.readValue(responseBytes, Object.class);
                        String prettyJson = mapper.writeValueAsString(jsonObject);
                        responseBytes = prettyJson.getBytes(StandardCharsets.UTF_8);
                    } catch (Exception e) {
                        logger.warning("Failed to pretty print JSON: " + e.getMessage());
                    }
                }

                exchange.getResponseHeaders().set("Content-Type", (contentType != null) ? contentType : "application/json");
                exchange.sendResponseHeaders(responseCode, responseBytes.length);
                try (OutputStream out = exchange.getResponseBody()) {
                    out.write(responseBytes);
                }
            } else {
                sendErrorResponse(exchange, 502, "No response from target service");
            }
        } catch (IOException e) {
            logger.severe("Error connecting to service: " + e.getMessage());
            sendErrorResponse(exchange, 503, "Service Unavailable: Unable to connect to " + fullUrl);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            exchange.close();
        }
    }


    private void copyHeaders(HttpExchange exchange, HttpURLConnection connection) {
        exchange.getRequestHeaders().forEach((key, values) -> values.forEach(value -> connection.setRequestProperty(key, value)));
    }
}
