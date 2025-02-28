package fr.unice.polytech.server.httphandlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import fr.unice.polytech.application.usecase.interfaces.IOrderPlacementCoordinator;
import fr.unice.polytech.domain.models.order.Order;
import fr.unice.polytech.domain.models.restaurant.MenuItem;
import fr.unice.polytech.server.JaxsonUtils;
import fr.unice.polytech.server.utils.ApiRegistry;
import fr.unice.polytech.server.utils.QueryParams;
import fr.unice.polytech.server.utils.RouteInfo;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;

public class OrderHttpHandler implements HttpHandler {
    private final IOrderPlacementCoordinator orderPlacementCoordinator;

    Logger logger = Logger.getLogger(OrderHttpHandler.class.getName());

    public OrderHttpHandler(IOrderPlacementCoordinator orderPlacementCoordinator) {
        this.orderPlacementCoordinator = orderPlacementCoordinator;

        // Get all orders by user id
        ApiRegistry.registerRoute("GET", "/api/orders/user/{userId}", (exchange, userId, queryParams) -> {
            System.out.println("GET all orders by user id " + userId);
            logger.info("GET all orders by user id " + userId);
            answerWithAllOrders(exchange, userId);
        });

        // Create a new order
        ApiRegistry.registerRoute("POST", "/api/orders", (exchange, pathVariable, queryParams) -> {
            logger.info("POST method called");
            askToCreateOrder(exchange);
        });

        // Get an order by id
        ApiRegistry.registerRoute("GET", "/api/orders/{orderId}", (exchange, orderId, queryParams) -> {
            logger.info("GET order with id " + orderId);
            answerWithOrder(exchange, orderId);
        });


        // Add OrderItem to an order
        ApiRegistry.registerRoute("PUT", "/api/orders/{orderId}/items", (exchange, orderId, queryParams) -> {
            logger.info("PUT order item to order with id " + orderId);
            askToAddOrderItem(exchange, orderId);
        });

        // Place an order
        ApiRegistry.registerRoute("GET", "/api/orders/{orderId}/placement", (exchange, orderId, queryParams) -> {
            logger.info("GET place order with id " + orderId);
            askToPlaceOrder(exchange, orderId);
        });

        // Pay an order
        ApiRegistry.registerRoute("GET", "/api/orders/{orderId}/payment", (exchange, orderId, queryParams) -> {
            logger.info("GET pay order with id " + orderId);
            askToPayOrder(exchange, orderId);
        });

        ApiRegistry.registerRoute("GET", "/api/orders/{restaurantId}/times", (exchange, restaurantId, queryParams) -> {
            logger.info("GET all times by user id " + restaurantId);
            answerWithAllDeliveryTimes(exchange, restaurantId);
        });

        ApiRegistry.registerRoute("GET", "/api/orders/{restaurantId}/items", (exchange, restaurantId, queryParams) -> {
            logger.info("GET menu items with id " + restaurantId);
            String deliveryDate = queryParams.getFirst().getValue();
            answerWithAvailableItems(exchange, restaurantId, deliveryDate);
        });

        ApiRegistry.registerRoute("GET", "/api/orders/{restaurantId}/menu", (exchange, restaurantId, queryParams) -> {
            logger.info("GET menu items with id " + restaurantId);
            answerWithAllItems(exchange, restaurantId);
        });

        // Register the route for choosing a restaurant
        ApiRegistry.registerRoute("PUT", "/api/orders/{userId}/chooseRestaurant", (exchange, userId, queryParams) -> {
            logger.info("PUT choose restaurant for user with id " + userId);
            askToChooseRestaurant(exchange, userId);
        });

    }

    private void answerWithAllItems(HttpExchange exchange, String restaurantId) {
        List<MenuItem> availableItems = orderPlacementCoordinator.getAllMenuItems(restaurantId);

        try {
            String response = JaxsonUtils.toJson(availableItems);
            HttpUtils.sendResponse(exchange, HttpUtils.HttpStatusCode.OK, response);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    private void askToChooseRestaurant(HttpExchange exchange, String userId) throws IOException {
        InputStream requestBody = exchange.getRequestBody();
        String body = new String(requestBody.readAllBytes(), StandardCharsets.UTF_8);
        logger.info(() -> "Request body: " + body);

        JsonNode jsonNode = JaxsonUtils.getJsonNode(body);
        if (jsonNode == null) return;

        String restaurantId = jsonNode.path("restaurantId").asText(null);
        if (restaurantId == null) {
            HttpUtils.sendResponse(exchange, HttpUtils.HttpStatusCode.BAD_REQUEST, "Missing restaurantId");
            return;
        }

        try {
            Order order = orderPlacementCoordinator.chooseRestaurant(userId, restaurantId);
            String response = JaxsonUtils.toJson(order);
            HttpUtils.sendResponse(exchange, HttpUtils.HttpStatusCode.OK, response);
        } catch (IllegalStateException e) {
            HttpUtils.sendResponse(exchange, HttpUtils.HttpStatusCode.BAD_REQUEST, e.getMessage());
        }
    }

    private void answerWithAvailableItems(HttpExchange exchange, String restaurantId, String deliveryDate){
        LocalDateTime deliveryDateTime = LocalDateTime.parse(deliveryDate);
        List<MenuItem> availableItems = orderPlacementCoordinator.getAvailableMenuItems(restaurantId, deliveryDateTime);

        try {
            String response = JaxsonUtils.toJson(availableItems);
            HttpUtils.sendResponse(exchange, HttpUtils.HttpStatusCode.OK, response);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

    }


    private void answerWithAllDeliveryTimes(HttpExchange exchange, String restaurantId) {
        //LocalDateTime orderDateTime = LocalDateTime.now();
        LocalDateTime orderDateTime = LocalDateTime.of(2024, 12, 31, 13, 00);
        List<LocalDateTime> availableTimes = orderPlacementCoordinator.getAvailableDeliveryTime(restaurantId, orderDateTime);

        logger.info(() -> "Available times: " + availableTimes);

        try {
            String response = JaxsonUtils.toJson(availableTimes);
            HttpUtils.sendResponse(exchange, HttpUtils.HttpStatusCode.OK, response);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // CORS
        if (HttpUtils.configureCors(exchange)) return;

        String requestMethod = exchange.getRequestMethod();
        String requestPath = exchange.getRequestURI().getPath();
        String requestQuery = exchange.getRequestURI().getQuery();

        logger.log(Level.INFO, () -> "OrderHandler called: " + requestMethod + " " + requestPath);
        List<RouteInfo> routes = ApiRegistry.getRoutes(requestMethod);
        for (RouteInfo route : routes) {
            if (route.matches(requestMethod, requestPath)) {
                Matcher matcher = route.getPathMatcher(requestPath);
                String pathVariable = "";
                if (matcher.find() && matcher.groupCount() > 0) {
                    pathVariable = matcher.group(1);
                }
                List<QueryParams> queryParams = HttpUtils.parseQueryParams(requestQuery);
                route.getHandler().handle(exchange, pathVariable, queryParams);
                return;
            }
        }
    }

    private void askToCreateOrder(HttpExchange exchange) throws IOException {
        InputStream requestBody = exchange.getRequestBody();
        String body = new String(requestBody.readAllBytes(), StandardCharsets.UTF_8);
        logger.info(() -> "Request body: " + body);

        JsonNode jsonNode = JaxsonUtils.getJsonNode(body);
        if (jsonNode == null) return;

        String restaurantId = jsonNode.path("restaurantId").asText(null);
        String userId = jsonNode.path("userId").asText(null);
        String deliveryLocationId = jsonNode.path("deliveryLocationId").asText(null);
        String deliveryTimeStr = jsonNode.path("deliveryTime").asText(null);

        LocalDateTime deliveryTime = null;
        try {
            if (deliveryTimeStr != null) {
                deliveryTime = LocalDateTime.parse(deliveryTimeStr);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Invalid date format", e);
            return;
        }

        Order order = orderPlacementCoordinator.createOrder(restaurantId, userId, deliveryLocationId, deliveryTime);

        String response = JaxsonUtils.toJson(order);
        HttpUtils.sendResponse(exchange, HttpUtils.HttpStatusCode.CREATED, response);
    }

    private void answerWithAllOrders(HttpExchange exchange, String userId) throws IOException {
        logger.info(() -> "User id: " + userId);
        Optional<Order> orders = orderPlacementCoordinator.getUserCart(userId);
        logger.info(() -> "Orders: " + orders);
        List<Order> ordersList = orders.map(List::of).orElse(List.of());

        if (orders.isEmpty()) {
            exchange.sendResponseHeaders(HttpUtils.HttpStatusCode.NOT_FOUND, 0);
            exchange.getResponseBody().close();
        } else {
            String response = JaxsonUtils.toJson(ordersList);
            HttpUtils.sendResponse(exchange, HttpUtils.HttpStatusCode.OK, response);
        }

    }

    private void answerWithOrder(HttpExchange exchange, String orderId) throws IOException {
        Order order = orderPlacementCoordinator.getOrderById(orderId);

        if (order == null) {
            HttpUtils.sendNotFoundResponse(exchange);
        } else {
            String response = JaxsonUtils.toJson(order);
            HttpUtils.sendResponse(exchange, HttpUtils.HttpStatusCode.OK, response);
        }
    }

    private void askToAddOrderItem(HttpExchange exchange, String orderId) throws IOException {
        InputStream requestBody = exchange.getRequestBody();
        String body = new String(requestBody.readAllBytes(), StandardCharsets.UTF_8);

        logger.info(() -> "Request body: " + body);

        JsonNode jsonNode = JaxsonUtils.getJsonNode(body);
        if (jsonNode == null) return;

        String menuItemId = jsonNode.path("menuItemId").asText(null);
        int quantity = Integer.parseInt(jsonNode.path("quantity").asText(null));

        Order order = orderPlacementCoordinator.addItemToOrder(orderId, menuItemId, quantity);

        String response = JaxsonUtils.toJson(order);
        HttpUtils.sendResponse(exchange, HttpUtils.HttpStatusCode.OK, response);


    }

    private void askToPlaceOrder(HttpExchange exchange, String orderId) throws JsonProcessingException {
        Order order = orderPlacementCoordinator.getOrderById(orderId);
        if (order == null) {
            HttpUtils.sendNotFoundResponse(exchange);
        } else {
            boolean placed = orderPlacementCoordinator.placeOrder(order);
            if (placed) {
                String response = JaxsonUtils.toJson("Your order has been placed successfully");
                HttpUtils.sendResponse(exchange, HttpUtils.HttpStatusCode.OK, response);

            } else {
                HttpUtils.sendResponse(exchange, HttpUtils.HttpStatusCode.INTERNAL_SERVER_ERROR, "Order placement failed");
            }
        }
    }

    private void askToPayOrder(HttpExchange exchange, String orderId) {
        Order order = orderPlacementCoordinator.getOrderById(orderId);
        if (order == null) {
            HttpUtils.sendNotFoundResponse(exchange);
        } else {
            boolean placed = orderPlacementCoordinator.processPayment(order);
            if (placed) {
                try {
                    String response = JaxsonUtils.toJson(order);
                    HttpUtils.sendResponse(exchange, HttpUtils.HttpStatusCode.OK, response);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                HttpUtils.sendResponse(exchange, HttpUtils.HttpStatusCode.INTERNAL_SERVER_ERROR, "Payment failed");
            }
        }
    }
}
