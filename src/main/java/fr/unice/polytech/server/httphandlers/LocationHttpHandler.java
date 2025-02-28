package fr.unice.polytech.server.httphandlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import fr.unice.polytech.application.dto.DeliveryLocationDTO;
import fr.unice.polytech.application.exceptions.EntityAlreadyExist;
import fr.unice.polytech.application.usecase.interfaces.ILocationService;
import fr.unice.polytech.domain.models.delivery.DeliveryLocation;
import fr.unice.polytech.server.JaxsonUtils;
import fr.unice.polytech.server.httphandlers.HttpUtils.HttpHeader;
import fr.unice.polytech.server.httphandlers.HttpUtils.HttpStatusCode;
import fr.unice.polytech.server.utils.ApiRegistry;
import fr.unice.polytech.server.utils.QueryParams;
import fr.unice.polytech.server.utils.RouteInfo;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;

import static fr.unice.polytech.server.httphandlers.HttpUtils.*;

public class LocationHttpHandler implements HttpHandler {
    Logger logger = Logger.getLogger(LocationHttpHandler.class.getName());
    private final ILocationService locationService;

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // CORS
        if (HttpUtils.configureCors(exchange)) return;

        if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Accept, Content-Type, Authorization");
            exchange.sendResponseHeaders(HttpStatusCode.NO_CONTENT, -1);
            return;
        }

        String requestMethod = exchange.getRequestMethod();
        String requestPath = exchange.getRequestURI().getPath();
        String requestQuery = exchange.getRequestURI().getQuery();

        logger.log(Level.INFO, () -> "LocationsHandler called: " + requestMethod + " " + requestPath);
        List<RouteInfo> routes = ApiRegistry.getRoutes(requestMethod);
        for (RouteInfo route : routes) {
            if (route.matches(requestMethod, requestPath)) {
                Matcher matcher = route.getPathMatcher(requestPath);
                String pathVariable = "";
                if (matcher.find() && matcher.groupCount() > 0) {
                    pathVariable = matcher.group(1);
                }
                List<QueryParams> queryParams = (List<QueryParams>) HttpUtils.parseQueryParams(requestQuery);
                route.getHandler().handle(exchange, pathVariable, queryParams);
                return;
            }
        }


        exchange.sendResponseHeaders(HttpUtils.HttpStatusCode.NOT_FOUND, 0);
        exchange.getResponseBody().close();
    }

    public LocationHttpHandler(ILocationService locationService) {
        this.locationService = locationService;

        // Get all delivery location registerd
        ApiRegistry.registerRoute("GET", "/api/locations", (exchange, pathVariable, queryParams) -> {
            logger.info("GET all locations registerd");
            answerWithAllLocations(exchange);
        });


        ApiRegistry.registerRoute("GET", "/api/locations/name", (exchange, pathVariable, queryParams) -> {
            logger.info("GET locations with name");
            String locationName = queryParams.getFirst().getValue();
            logger.info("locationName: " + locationName);
            System.out.println("locationName: " + locationName);
            answerWithLocationByName(exchange, locationName);
        });

        //Get a delivery location by its id
        ApiRegistry.registerRoute("GET", "/api/locations/{locationId}", (exchange, locationId, queryParams) -> {
            logger.info("GET location with id" + locationId);
            answerWithLocationById(exchange, locationId);
        });


        // POST to add a new location
        ApiRegistry.registerRoute("POST", "/api/locations", (exchange, pathVariable, queryParams) -> {
            logger.info("POST request to add a new location");
            askToCreateLocation(exchange);
        });
    }

    private void answerWithAllLocations(HttpExchange exchange) throws IOException {
        List<DeliveryLocation> locations = locationService.getDeliveryLocations();

        String response = JaxsonUtils.toJson(locations);
        HttpUtils.sendResponse(exchange, HttpUtils.HttpStatusCode.OK, response);
    }

    private void answerWithLocationById(HttpExchange exchange, String locationId) throws IOException, IllegalArgumentException {
        Optional<DeliveryLocation> location = locationService.getLocationById(locationId);

        if (location.isPresent()) {
            String response = JaxsonUtils.toJson(location.get());
            HttpUtils.sendResponse(exchange, HttpUtils.HttpStatusCode.OK, response);
        } else {
            logger.info("Location not found");
            String errorResponse = "{\"error\":\"Location not found\"}";
            HttpUtils.sendResponse(exchange, HttpStatusCode.NOT_FOUND, errorResponse);
        }
    }

    private void answerWithLocationByName(HttpExchange exchange, String locationName) throws IOException {
        //Optional<DeliveryLocation> location = locationService.getLocationByName(locationName);
        List<DeliveryLocation> locations = locationService.getLocationsByName(locationName);

        logger.info("locationName: " + locationName);
        for(DeliveryLocation location : locations) {
            logger.info("Location found: " + location);
        }

        if(locations.isEmpty()) {
            logger.info("Location not found");
            String errorResponse = "{\"error\":\"Location not found with name: " + locationName + "\"}";
            HttpUtils.sendResponse(exchange, HttpStatusCode.NOT_FOUND, errorResponse);
        } else {
            String response = JaxsonUtils.toJson(locations);
            HttpUtils.sendResponse(exchange, HttpUtils.HttpStatusCode.OK, response);
        }

    }

    private void askToCreateLocation(HttpExchange exchange) throws IOException {
        InputStream requestBody = exchange.getRequestBody();
        String json = new String(requestBody.readAllBytes(), StandardCharsets.UTF_8);

        logger.log(java.util.logging.Level.FINE, "Request body: " + json);
        try {
            // Parse the JSON input to a DeliveryLocationDTO
            DeliveryLocationDTO locationDTO = JaxsonUtils.fromJson(json, DeliveryLocationDTO.class);
            
            DeliveryLocation newLocation = locationService.addLocation(locationDTO);
            String response = JaxsonUtils.toJson(newLocation);
            exchange.getResponseHeaders().set(HttpHeader.CONTENT_TYPE, HttpHeader.APPLICATION_JSON);
            exchange.sendResponseHeaders(HttpUtils.HttpStatusCode.CREATED, response.length());
            exchange.getResponseBody().write(response.getBytes());
        // TODO: refactor this verbose error handling
        } catch (EntityAlreadyExist e) {
            String errorResponse = "{\"error\": \"" + e.getMessage() + "\"}";
            exchange.getResponseHeaders().set(HttpHeader.CONTENT_TYPE, HttpHeader.APPLICATION_JSON);
            exchange.sendResponseHeaders(HttpUtils.HttpStatusCode.CONFLICT, errorResponse.length()); // Conflict
            exchange.getResponseBody().write(errorResponse.getBytes());
        } catch (JsonProcessingException e ) {
            StringBuilder errorResponse = new StringBuilder("{\"error\":\"Invalid JSON input.\"");
            String message = e.getCause() != null ? e.getCause().getMessage() : "";
            errorResponse.append(", \"message\":\"").append(message).append("\"}");
            exchange.getResponseHeaders().set(HttpHeader.CONTENT_TYPE, HttpHeader.APPLICATION_JSON);
            exchange.sendResponseHeaders(HttpUtils.HttpStatusCode.BAD_REQUEST, errorResponse.length()); // Bad Request
            exchange.getResponseBody().write(errorResponse.toString().getBytes());
        } catch (Exception e) {
            String errorResponse = "{\"error\":\"An error occurred while creating the location.\"}";
            exchange.getResponseHeaders().set(HttpHeader.CONTENT_TYPE, HttpHeader.APPLICATION_JSON);
            exchange.sendResponseHeaders(HttpUtils.HttpStatusCode.INTERNAL_SERVER_ERROR, errorResponse.length()); // Internal Server Error
            exchange.getResponseBody().write(errorResponse.getBytes());
        } finally {
            exchange.close();
        }
    }
}
