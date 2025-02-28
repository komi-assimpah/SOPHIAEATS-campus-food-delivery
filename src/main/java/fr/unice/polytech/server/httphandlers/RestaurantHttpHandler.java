package fr.unice.polytech.server.httphandlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import fr.unice.polytech.application.exceptions.EntityNotFoundException;
import fr.unice.polytech.application.usecase.interfaces.IRestaurantCapacityService;
import fr.unice.polytech.application.usecase.interfaces.IRestaurantScheduleManager;
import fr.unice.polytech.application.usecase.interfaces.IRestaurantService;
import fr.unice.polytech.domain.exceptions.InvalidScheduleException;
import fr.unice.polytech.domain.models.restaurant.MenuItem;
import fr.unice.polytech.domain.models.restaurant.Restaurant;
import fr.unice.polytech.domain.models.restaurant.Schedule;
import fr.unice.polytech.server.JaxsonUtils;
import fr.unice.polytech.server.httphandlers.HttpUtils.HttpStatusCode;
import fr.unice.polytech.server.utils.ApiRegistry;
import fr.unice.polytech.server.utils.QueryParams;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class RestaurantHttpHandler implements HttpHandler {
    Logger logger = Logger.getLogger(RestaurantHttpHandler.class.getName());
    private final IRestaurantService restaurantService;
    private final IRestaurantScheduleManager restaurantScheduleManager;
    private final IRestaurantCapacityService restaurantCapacityService;

    public RestaurantHttpHandler(IRestaurantService restaurantService, IRestaurantScheduleManager restaurantScheduleManager, IRestaurantCapacityService restaurantCapacityService) {
        this.restaurantService = restaurantService;
        this.restaurantScheduleManager = restaurantScheduleManager;
        this.restaurantCapacityService = restaurantCapacityService;
    }

    private void handleGetAvailableDeliveryTimesForAll(HttpExchange exchange) throws IOException {
        //LocalDateTime orderDateTime = LocalDateTime.now(); // or use a specific date-time if needed
        LocalDateTime orderDateTime = LocalDateTime.of(2024, 12, 31, 13, 00);

        List<Restaurant> restaurants = restaurantService.getAvailableRestaurants(orderDateTime);
        for (Restaurant restaurant : restaurants) {
            logger.info("Restaurant: " + restaurant.getName());
        }
        List<LocalDateTime> availableTimes = restaurantCapacityService.getAvailableDeliveryTimeForAllRestaurants(restaurants, orderDateTime);

        for (LocalDateTime time : availableTimes) {
            logger.info("Available time: " + time);
        }

        try {
            String response = JaxsonUtils.toJson(availableTimes);
            HttpUtils.sendResponse(exchange, HttpUtils.HttpStatusCode.OK, response);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            HttpUtils.sendResponse(exchange, HttpUtils.HttpStatusCode.INTERNAL_SERVER_ERROR, "{\"error\":\"Unable to process request.\"}");
        }
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Gestion CORS

        if (HttpUtils.configureCors(exchange)) return;

        String requestMethod = exchange.getRequestMethod();
        String requestPath = exchange.getRequestURI().getPath();
        String query = exchange.getRequestURI().getQuery(); // Récupérer les paramètres de requête (ex. "name=Ksc")

        logger.info("Request received: " + requestMethod + " " + requestPath);

        switch (requestMethod) {
            case "GET" -> handleGetRequest(exchange, requestPath, query);
            case "POST" -> handlePostRequest(exchange, requestPath);
            case "PUT" -> handlePutRequest(exchange, requestPath);
            default -> HttpUtils.sendNotFoundResponse(exchange);
        }
    }

    private void handleGetRequest(HttpExchange exchange, String path, String query) throws IOException {
        switch (path) {
            case "/api/restaurants" -> handleGetRestaurants(exchange, query);
            case "/api/restaurants/available" -> handleGetAvailableRestaurants(exchange);
            case "/api/restaurants/times" -> handleGetAvailableDeliveryTimesForAll(exchange);
            default -> {
                if (path.matches("/api/restaurants/[a-zA-Z0-9\\-]+")) {
                    String restaurantId = path.split("/")[3];
                    handleGetRestaurantById(exchange, restaurantId);
                } else {
                    HttpUtils.sendNotFoundResponse(exchange);
                }
            }
        }
    }

    private void handlePostRequest(HttpExchange exchange, String path) throws IOException {
        if ("/api/restaurants".equals(path)) {
            handleCreateRestaurant(exchange);
        } else if (path.matches("/api/restaurants/[a-zA-Z0-9\\-]+/schedule")) {
            String restaurantId = path.split("/")[3];
            handleCreateSchedule(exchange, restaurantId);
        } else {
            HttpUtils.sendNotFoundResponse(exchange);
        }
    }

    private void handlePutRequest(HttpExchange exchange, String path) throws IOException {
        if (path.matches("/api/restaurants/[a-zA-Z0-9\\-]+/menu")) {
            String restaurantId = path.split("/")[3];
            handleUpdateRestaurantMenu(exchange, restaurantId);
        } else if (path.matches("/api/restaurants/[a-zA-Z0-9\\-]+/schedule/[a-zA-Z0-9\\-]+")) {
            String restaurantId = path.split("/")[3];
            String scheduleId = path.split("/")[5];
            logger.info("Entering handleUpdateRestaurantSchedule");
            handleUpdateRestaurantSchedule(exchange, restaurantId, scheduleId);
        } else {
            HttpUtils.sendNotFoundResponse(exchange);
        }
    }



    //---------------------------------------------Restaurant Management-------------------------------------------//
    private void handleGetRestaurants(HttpExchange exchange, String query) throws IOException {
        List<QueryParams> queryParams = HttpUtils.parseQueryParams(query);

        QueryParams param = queryParams.stream().filter(p -> p.getKey().equals("name")).findFirst().orElse(null);

        if (param!=null) {
            String name = param.getValue();

            List<Restaurant> restaurants = restaurantService.findsByName(name);

            if (restaurants.isEmpty()) {
                String response = "{\"error\":\"No restaurants found with name '" + name + "'.\"}";
                HttpUtils.sendResponse(exchange, HttpStatusCode.NOT_FOUND, response);
            } else {
                String response = JaxsonUtils.toJson(restaurants);
                HttpUtils.sendResponse(exchange, HttpStatusCode.OK, response);
            }
        } else {
            List<Restaurant> restaurants = restaurantService.getAllRestaurants();
            String response = JaxsonUtils.toJson(restaurants);
            HttpUtils.sendResponse(exchange, HttpStatusCode.OK, response);
        }
    }

    private void handleGetAvailableRestaurants(HttpExchange exchange) throws IOException {
        //LocalDateTime rightNow = LocalDateTime.now();
        LocalDateTime rightNow = LocalDateTime.of(2024, 11, 12, 12, 12);
        logger.info("Checking available restaurants at: " + rightNow);
        try {
            List<Restaurant> restaurants = restaurantService.getAvailableRestaurants(rightNow);

            if (restaurants.isEmpty()) {
                logger.info("No restaurants available at " + rightNow);
            } else {
                logger.info("Available restaurants: " + restaurants.size());
                for (Restaurant restaurant : restaurants) {
                    logger.info(" - " + restaurant.getName());
                }
            }

            String response = JaxsonUtils.toJson(restaurants);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(HttpStatusCode.OK, response.length());
            exchange.getResponseBody().write(response.getBytes());

        } catch (Exception e) {
            logger.severe("Error fetching available restaurants: " + e.getMessage());
            String response = "{\"error\":\"Unable to fetch available restaurants.\"}";
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(HttpStatusCode.INTERNAL_SERVER_ERROR, response.length());
            exchange.getResponseBody().write(response.getBytes());
        } finally {
            exchange.close();
        }
    }

    private void handleGetRestaurantById(HttpExchange exchange, String restaurantId) throws IOException {
        try {
            Restaurant restaurant = restaurantService.getRestaurantById(restaurantId);
            String response = JaxsonUtils.toJson(restaurant);
            HttpUtils.sendResponse(exchange, HttpStatusCode.OK, response);
        } catch (EntityNotFoundException e) {
            String response = "{\"error\":\"Restaurant with ID " + restaurantId + " not found.\"}";
            HttpUtils.sendResponse(exchange, HttpStatusCode.NOT_FOUND, response);
        }
    }

    private void handleCreateRestaurant(HttpExchange exchange) throws IOException {
        InputStream requestBody = exchange.getRequestBody();
        String json = new String(requestBody.readAllBytes(), StandardCharsets.UTF_8);

        ObjectMapper mapper = new ObjectMapper();
        try {
            Map<String, Object> jsonData = mapper.readValue(json, Map.class);
            String restaurantName = (String) jsonData.get("name");
            Map<String, String> addressData = (Map<String, String>) jsonData.get("address");
            String street = addressData.get("street");
            String city = addressData.get("city");
            String zipCode = addressData.get("zipCode");
            String country = addressData.get("country");

            Restaurant restaurant = restaurantService.createRestaurant(restaurantName, street, city, zipCode, country);
            String response = JaxsonUtils.toJson(restaurant);
            HttpUtils.sendResponse(exchange, HttpStatusCode.CREATED, response);
        } catch (IllegalArgumentException e) {
            String response = "{\"error\":\"" + e.getMessage() + "\"}";
            HttpUtils.sendResponse(exchange, HttpStatusCode.CONFLICT, response);
        } catch (Exception e) {
            String response = "{\"error\":\"Unable to create restaurant.\"}";
            HttpUtils.sendResponse(exchange, HttpStatusCode.INTERNAL_SERVER_ERROR, response);
        }
    }

    private void handleUpdateRestaurantMenu(HttpExchange exchange, String restaurantId) throws IOException {
        logger.info("entering handleUpdateRestaurantMenu");
        InputStream requestBody = exchange.getRequestBody();
        String json = new String(requestBody.readAllBytes(), StandardCharsets.UTF_8);
        logger.info("Received JSON for updated restaurant: " + json);

        ObjectMapper mapper = new ObjectMapper();
        try {
            Map<String, Object> jsonData = mapper.readValue(json, Map.class);
            //List<MenuItem> menuList = mapper.readValue(json, mapper.getTypeFactory().constructCollectionType(List.class, MenuItem.class));
            //logger.info("Parsed menu items: " + menuList.size());
            logger.info("Parsed menu items: " + jsonData.get("menu"));
            List<MenuItem> menuList = (List<MenuItem>) jsonData.get("menu");

            restaurantService.updateRestaurantMenu(restaurantId, menuList);

            String response = "{\"message\":\"Menu updated successfully.\"}";
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(HttpStatusCode.OK, response.length());
            exchange.getResponseBody().write(response.getBytes());

        } catch (EntityNotFoundException e) {
            String response = "{\"error\":\"Restaurant with ID " + restaurantId + " not found.\"}";
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(HttpStatusCode.NOT_FOUND, response.length());
            exchange.getResponseBody().write(response.getBytes());

        } catch (Exception e) {
            String response = "{\"error\":\"Unable to update restaurant menu.\"}";
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(HttpStatusCode.INTERNAL_SERVER_ERROR, response.length());
            exchange.getResponseBody().write(response.getBytes());
        } finally {
            exchange.close();
        }
    }


    //--------------------------------------------- Restaurant Schedule Management ----------------------------------//
    private void handleCreateSchedule(HttpExchange exchange, String restaurantId) throws IOException {
        InputStream requestBody = exchange.getRequestBody();
        String json = new String(requestBody.readAllBytes(), StandardCharsets.UTF_8);
        logger.info("Received JSON for new schedule: " + json);

        ObjectMapper mapper = new ObjectMapper();
        try {
            Map<String, Object> scheduleData = mapper.readValue(json, Map.class);

            // Convertir les données JSON en objet Schedule
            Schedule newSchedule = mapToSchedule(scheduleData);

            // Appeler `IRestaurantScheduleManager` pour ajouter le nouveau schedule
            restaurantScheduleManager.addSchedule(restaurantId, newSchedule);

            String response = "{\"message\":\"Schedule created successfully.\"}";
            HttpUtils.sendResponse(exchange, HttpStatusCode.CREATED, response);

        } catch (EntityNotFoundException e) {
            String response = "{\"error\":\"Restaurant not found.\"}";
            HttpUtils.sendResponse(exchange, HttpStatusCode.NOT_FOUND, response);

        } catch (IllegalArgumentException | InvalidScheduleException e) {
            String response = "{\"error\":\"" + e.getMessage() + "\"}";
            HttpUtils.sendResponse(exchange, HttpStatusCode.BAD_REQUEST, response);

        } catch (Exception e) {
            String response = "{\"error\":\"Unable to create schedule: " + e.getMessage() + "\"}";
            HttpUtils.sendResponse(exchange, HttpStatusCode.INTERNAL_SERVER_ERROR, response);
        }
    }

    private void handleUpdateRestaurantSchedule(HttpExchange exchange, String restaurantId, String scheduleId) throws IOException {
        InputStream requestBody = exchange.getRequestBody();
        String json = new String(requestBody.readAllBytes(), StandardCharsets.UTF_8);
        logger.info("Received JSON for schedule update: " + json);

        ObjectMapper mapper = new ObjectMapper();
        try {
            Map<String, Object> jsonData = mapper.readValue(json, Map.class);

            Schedule existingSchedule = restaurantScheduleManager.getScheduleByRestaurantId(restaurantId, scheduleId);
            Schedule newSchedule = mapToSchedule(jsonData);

            restaurantScheduleManager.updateSchedule(restaurantId, existingSchedule, newSchedule)
                    .orElseThrow(() -> new IllegalArgumentException("Failed to update schedule"));

            String response = "{\"message\":\"Schedule updated successfully.\"}";
            HttpUtils.sendResponse(exchange, HttpStatusCode.OK, response);

        } catch (EntityNotFoundException e) {
            logger.severe("Error: " + e.getMessage());
            String response = "{\"error\":\"" + e.getMessage() + "\"}";
            HttpUtils.sendResponse(exchange, HttpStatusCode.NOT_FOUND, response);

        } catch (IllegalArgumentException e) {
            logger.warning("Error: " + e.getMessage());
            String response = "{\"error\":\"" + e.getMessage() + "\"}";
            HttpUtils.sendResponse(exchange, HttpStatusCode.BAD_REQUEST, response);

        } catch (Exception e) {
            logger.severe("Unexpected error: " + e.getMessage());
            String response = "{\"error\":\"Unable to update schedule: " + e.getMessage() + "\"}";
            HttpUtils.sendResponse(exchange, HttpStatusCode.INTERNAL_SERVER_ERROR, response);
        }
    }

    private Schedule mapToSchedule(Map<String, Object> scheduleData) {
        try {
            DayOfWeek day = DayOfWeek.valueOf(((String) scheduleData.get("day")).toUpperCase());
            LocalTime startTime = LocalTime.parse((String) scheduleData.get("startTime"));
            LocalTime endTime = LocalTime.parse((String) scheduleData.get("endTime"));
            int numberOfWorkingStaff = (int) scheduleData.getOrDefault("numberOfWorkingStaff", 0);

            Schedule schedule = new Schedule(day, startTime, endTime, numberOfWorkingStaff);
            logger.info("Schedule created: " + schedule.toString());
            return schedule;

        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid schedule data: " + e.getMessage());
        }
    }




}
