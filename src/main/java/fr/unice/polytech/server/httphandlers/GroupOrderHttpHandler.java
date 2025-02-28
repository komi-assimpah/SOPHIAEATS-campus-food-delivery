package fr.unice.polytech.server.httphandlers;

import com.fasterxml.jackson.databind.JsonNode;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import fr.unice.polytech.application.exceptions.EntityNotFoundException;
import fr.unice.polytech.application.usecase.interfaces.IGroupOrderService;
import fr.unice.polytech.domain.models.groupOrder.GroupOrder;
import fr.unice.polytech.server.JaxsonUtils;
import fr.unice.polytech.server.utils.ApiRegistry;
import fr.unice.polytech.server.utils.QueryParams;
import fr.unice.polytech.server.utils.RouteInfo;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;

public class GroupOrderHttpHandler implements HttpHandler {

    Logger logger = Logger.getLogger(GroupOrderHttpHandler.class.getName());
    private static final String REQUEST_COMPLETED = "Request processing complete";
    private static final String REQUEST_BODY = "Request body: ";
    private static final String REQUEST_PATH = "Request path: ";
    private static final String MISSING_REQUIRED_PARAMS= "Missing required parameter(s)";
    private static final String EXTRACTED_ORDER_ID = "Extracted Order ID: ";
    private final IGroupOrderService groupOrderService;

    public GroupOrderHttpHandler(IGroupOrderService groupOrderService) {
        this.groupOrderService = groupOrderService;

        //Get all group orders
        ApiRegistry.registerRoute("GET", "/api/groupOrders", (exchange, pathVariable, param) -> {
            try {
                logger.info("Handling GET request for all group orders");
                askToGetAllGroupOrders(exchange, pathVariable);
            } catch (IOException e) {
                logger.severe("Error while handling GET request for all group orders: " + e.getMessage());
                exchange.sendResponseHeaders(HttpUtils.HttpStatusCode.INTERNAL_SERVER_ERROR, -1);
            }
        });

        //Get group order by id
        ApiRegistry.registerRoute("GET", "/api/groupOrders/group/{groupID}", ((exchange, groupID, param) -> {
            logger.info("Handling GET request for a group order with ID");
            try {
                askToGetGroupOrderById(exchange, groupID);
            } catch (IOException e) {
                logger.severe("Error while handling GET request for a group order: " + e.getMessage());
                exchange.sendResponseHeaders(HttpUtils.HttpStatusCode.INTERNAL_SERVER_ERROR, -1);
            }
        }));

        //Get sub-order group
        ApiRegistry.registerRoute("GET", "/api/groupOrders/orderGroup", (exchange, pathVariable, param) -> {
            try {
                logger.info("Handling GET request for a sub-order group");
                askToFindSubOrderGroup(exchange, pathVariable, param);
            } catch (IOException e) {
                logger.severe("Error while handling GET request for a sub-order group: " + e.getMessage());
                exchange.sendResponseHeaders(HttpUtils.HttpStatusCode.INTERNAL_SERVER_ERROR, -1);
            }
        });

        //Create group order
        ApiRegistry.registerRoute("POST", "/api/groupOrders/create", (exchange, pathVariable, param) -> {
            try {
                logger.info("Handling POST request for a group order creation");
                askToCreateGroupOrder(exchange);
            } catch (IOException e) {
                logger.severe("Error while handling POST request for a group order creation: " + e.getMessage());
                exchange.sendResponseHeaders(HttpUtils.HttpStatusCode.INTERNAL_SERVER_ERROR, -1);
            }
        });

        //Join group order
        ApiRegistry.registerRoute("PUT" ,"/api/groupOrders/join/{groupID}" , (exchange, groupID, param) -> {
            try {

                logger.info("Handling PUT request for a group order joining");
                askToJoinGroupOrder(exchange, groupID);
            } catch (IOException e) {
                logger.severe("Error while handling PUT request for a group order joining: " + e.getMessage());
                exchange.sendResponseHeaders(HttpUtils.HttpStatusCode.INTERNAL_SERVER_ERROR, -1);
            }
        });

        //Complete group order
        ApiRegistry.registerRoute("PUT", "/api/groupOrders/complete", (exchange, pathVariable, param) -> {
            try {
                logger.info("Handling PUT request for a group order completion");
                askToCompleteOrderGroup(exchange, pathVariable, param);
            } catch (IOException e) {
                logger.severe("Error while handling PUT request for a group order completion: " + e.getMessage());
                exchange.sendResponseHeaders(HttpUtils.HttpStatusCode.INTERNAL_SERVER_ERROR, -1);
            }
        });

        //Validate group order
        ApiRegistry.registerRoute("PUT", "/api/groupOrders/validate", (exchange, pathVariable, param) -> {
            try {
                logger.info("Handling PUT request for a group order validation");
                askToValidateGroupOrder(exchange);
            } catch (IOException e) {
                logger.severe("Error while handling PUT request for a group order validation: " + e.getMessage());
                exchange.sendResponseHeaders(HttpUtils.HttpStatusCode.INTERNAL_SERVER_ERROR, -1);
            }
        });

        //Confirm group order
        ApiRegistry.registerRoute("PUT", "/api/groupOrders/confirm", (exchange, pathVariable, param) -> {
            try {
                logger.info("Handling PUT request for a group order confirmation");
                askToConfirmGroupOrder(exchange, pathVariable, param);
            } catch (IOException e) {
                logger.severe("Error while handling PUT request for a group order confirmation: " + e.getMessage());
                exchange.sendResponseHeaders(HttpUtils.HttpStatusCode.INTERNAL_SERVER_ERROR, -1);
            }
        });

        //Drop sub-order
        ApiRegistry.registerRoute("PUT", "/api/groupOrders/dropSub", (exchange, pathVariable, param) -> {
            try {
                logger.info("Handling DELETE request for a sub-order drop");
                askToDropSubOrder(exchange);
            } catch (IOException e) {
                logger.severe("Error while handling DELETE request for a sub-order drop: " + e.getMessage());
                exchange.sendResponseHeaders(HttpUtils.HttpStatusCode.INTERNAL_SERVER_ERROR, -1);
            }
        });

    }

    private void askToGetAllGroupOrders(HttpExchange exchange, String pathVariable) throws IOException {
        logger.info("Asking to get all group orders");

        String path = exchange.getRequestURI().getPath();
        logger.info(() -> "Request path: " + path);

        List<GroupOrder> groupOrders = groupOrderService.getAllGroupOrders();

        String response = JaxsonUtils.toJson(groupOrders);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(HttpUtils.HttpStatusCode.OK, response.length());
        exchange.getResponseBody().write(response.getBytes());

        logger.info(REQUEST_COMPLETED);
        exchange.close();
    }

    private void askToGetGroupOrderById(HttpExchange exchange, String groupID) throws IOException {
        logger.info("Asking to get a group order by ID");

        logger.info(() -> "Extracted GroupOrder ID: " + groupID);

        try {
            GroupOrder groupOrder = groupOrderService.findGroupOrderById(groupID);

            String response = JaxsonUtils.toJson(groupOrder);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(HttpUtils.HttpStatusCode.OK, response.length());
            exchange.getResponseBody().write(response.getBytes());

        } catch (EntityNotFoundException e) {

            logger.warning("GroupOrder not found: " + e.getMessage());
            String response = JaxsonUtils.toJson(Map.of("error", e.getMessage()));
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(HttpUtils.HttpStatusCode.NOT_FOUND, response.length());
            exchange.getResponseBody().write(response.getBytes());

        } finally {
            logger.info(() -> REQUEST_COMPLETED);
            exchange.close();
        }
    }

    private void askToFindSubOrderGroup(HttpExchange exchange, String pathVariable, List<QueryParams> param) throws IOException {
        logger.info("Asking to find a sub-order group");

        String path = exchange.getRequestURI().getPath();
        logger.info(() -> "Request path: " + path);

        String orderID = getQueryParamValue(param, "orderID");

        if (orderID == null) {
            logger.warning("Missing required parameter(s)");
            String response = JaxsonUtils.toJson(Map.of("error", "Missing required parameter(s)"));
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(HttpUtils.HttpStatusCode.BAD_REQUEST, -1);
            exchange.getResponseBody().write(response.getBytes());
            return;
        }
        logger.info(() -> EXTRACTED_ORDER_ID + orderID);

        try {
            GroupOrder groupOrder = groupOrderService.findSubOrderGroup(orderID);

            String response = JaxsonUtils.toJson(groupOrder);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(HttpUtils.HttpStatusCode.OK, response.length());
            exchange.getResponseBody().write(response.getBytes());

        } catch (EntityNotFoundException e) {

            logger.warning("Sub-order's group not found: " + e.getMessage());
            String response = JaxsonUtils.toJson(Map.of("error", e.getMessage()));
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(HttpUtils.HttpStatusCode.NOT_FOUND, -1);
            exchange.getResponseBody().write(response.getBytes());

        } finally {
            logger.info(REQUEST_COMPLETED);
            exchange.close();
        }
    }

    private void askToCreateGroupOrder(HttpExchange exchange) throws IOException {
        logger.info("Asking to create a group order");

        InputStream requestBody = exchange.getRequestBody();
        String body = new String(requestBody.readAllBytes(), StandardCharsets.UTF_8);
        logger.info(() -> REQUEST_BODY + body);

        JsonNode jsonNode = JaxsonUtils.getJsonNode(body);
        if (jsonNode == null) return;

        String orderID = jsonNode.path("orderID").asText(null);
        String deliveryLocationID = jsonNode.path("locationID").asText(null);
        String deliveryTime = jsonNode.path("deliveryTime").asText(null);

        if (orderID == null || deliveryLocationID == null) {
            logger.warning("Missing required parameter(s)");
            String response = JaxsonUtils.toJson(Map.of("error", "Missing required parameter(s)"));
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(HttpUtils.HttpStatusCode.BAD_REQUEST, -1);
            exchange.getResponseBody().write(response.getBytes());
            return;
        }
        logger.info(() -> "Extracted Order ID: " + orderID);
        logger.info(() -> "Extracted Location ID: " + deliveryLocationID);


        LocalDateTime deliveryTimeParsed = null;
        if (deliveryTime != null) {
            try {
                deliveryTimeParsed = LocalDateTime.parse(deliveryTime);
            } catch (IllegalArgumentException e) {
                logger.warning("Invalid delivery time format");
                String response = JaxsonUtils.toJson(Map.of("error", "Invalid delivery time format"));
                exchange.sendResponseHeaders(HttpUtils.HttpStatusCode.BAD_REQUEST, -1);
                exchange.getResponseBody().write(response.getBytes());
                return;
            }
            LocalDateTime finalDeliveryTimeParsed = deliveryTimeParsed;
            logger.info(() -> "Extracted Delivery Time: " + finalDeliveryTimeParsed);
        }

        try {
            String groupID = groupOrderService.createGroupOrder(orderID, deliveryLocationID, deliveryTimeParsed);

            String response = JaxsonUtils.toJson(Map.of("groupID", groupID
                    , "message", "Group order successfully created"));
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(HttpUtils.HttpStatusCode.CREATED, response.length());
            exchange.getResponseBody().write(response.getBytes());

        } catch (IllegalArgumentException e) {

            logger.warning("Invalid parameter(s)");
            String response = JaxsonUtils.toJson(Map.of("error", e.getMessage()));
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(HttpUtils.HttpStatusCode.BAD_REQUEST, -1);
            exchange.getResponseBody().write(response.getBytes());

        } finally {
            logger.info("Request processing complete");
            exchange.close();
        }
    }

    private void askToJoinGroupOrder(HttpExchange exchange, String groupID) throws IOException {
        logger.info("Asking to join a group order");

        InputStream requestBody = exchange.getRequestBody();
        String body = new String(requestBody.readAllBytes(), StandardCharsets.UTF_8);
        logger.info(() -> REQUEST_BODY + body);

        JsonNode jsonNode = JaxsonUtils.getJsonNode(body);
        if (jsonNode == null) return;

        String orderID = jsonNode.path("orderID").asText(null);

        logger.info(() -> "Extracted GroupOrder ID: " + groupID);
        if (orderID == null) {
            logger.warning("Missing required parameter(s)");
            String response = JaxsonUtils.toJson(Map.of("error", "Missing required parameter(s)"));
            exchange.sendResponseHeaders(HttpUtils.HttpStatusCode.BAD_REQUEST, -1);
            exchange.getResponseBody().write(response.getBytes());
            return;
        }
        logger.info(() -> "Extracted Group ID: " + groupID);

        try {
            groupOrderService.joinGroupOrder(orderID, groupID);

            String response = JaxsonUtils.toJson(Map.of("message", "Sub-order successfully joined group"));
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(HttpUtils.HttpStatusCode.OK, response.length());
            exchange.getResponseBody().write(response.getBytes());

        } catch (Exception e) {

            logger.warning("Failed to join group order: " + e.getMessage());
            String response = JaxsonUtils.toJson(Map.of("error", e.getMessage()));
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(HttpUtils.HttpStatusCode.BAD_REQUEST, -1);
            exchange.getResponseBody().write(response.getBytes());

        } finally {
            logger.info("Request processing complete");
            exchange.close();
        }
    }

    private void askToCompleteOrderGroup(HttpExchange exchange, String pathVariable, List<QueryParams> param) throws IOException {
        logger.info("Asking to complete a group order");
        String path = exchange.getRequestURI().getPath();
        logger.info(() -> "Request path: " + path);

        String orderID = getQueryParamValue(param, "orderID");

        if (orderID == null) {
            logger.warning("Missing required parameter(s)");
            String response = JaxsonUtils.toJson(Map.of("error", "Missing required parameter(s)"));
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(HttpUtils.HttpStatusCode.BAD_REQUEST, -1);
            exchange.getResponseBody().write(response.getBytes());
            return;
        }
        logger.info(() -> "Extracted Order ID: " + orderID);


        groupOrderService.completeOrderGroup(orderID);
        if (groupOrderService.findSubOrderGroup(orderID) == null) {
            logger.warning("Order not found in any group");
        }

        String response = JaxsonUtils.toJson(Map.of("message", "Group order successfully completed"));
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(HttpUtils.HttpStatusCode.OK, response.length());
        exchange.getResponseBody().write(response.getBytes());

        logger.info("Request processing complete");
        exchange.close();
    }

    private void askToValidateGroupOrder(HttpExchange exchange) throws IOException {
        logger.info("Asking to validate a group order");
        InputStream requestBody = exchange.getRequestBody();
        String body = new String(requestBody.readAllBytes(), StandardCharsets.UTF_8);
        logger.log(Level.INFO, REQUEST_BODY + body);

        JsonNode jsonNode = JaxsonUtils.getJsonNode(body);
        if (jsonNode == null) return;

        String orderID = jsonNode.path("orderID").asText(null);
        String deliveryTime = jsonNode.path("deliveryTime").asText(null);
        String closestPossibleDeliveryTime = jsonNode.path("possibleTime").asText(null);

        if (orderID == null || (deliveryTime != null && closestPossibleDeliveryTime == null)
                || (deliveryTime == null && closestPossibleDeliveryTime != null)) {
            logger.warning("Missing required parameter(s)");
            String response = JaxsonUtils.toJson(Map.of("error", "Missing required parameter(s)"));
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(HttpUtils.HttpStatusCode.BAD_REQUEST, -1);
            exchange.getResponseBody().write(response.getBytes());
            return;
        }
        logger.info(() -> "Extracted Order ID: " + orderID);

        LocalDateTime deliveryTimeParsed = null;
        LocalDateTime closestPossibleDeliveryTimeParsed = null;
        if (deliveryTime != null) {
            try {
                deliveryTimeParsed = LocalDateTime.parse(deliveryTime);
                closestPossibleDeliveryTimeParsed = LocalDateTime.parse(closestPossibleDeliveryTime);
            } catch (IllegalArgumentException e) {
                logger.warning("Invalid (closest) delivery time format");
                String response = JaxsonUtils.toJson(Map.of("error", "Invalid (closest) delivery time format"));
                exchange.sendResponseHeaders(HttpUtils.HttpStatusCode.BAD_REQUEST, -1);
                exchange.getResponseBody().write(response.getBytes());
                return;
            }

            LocalDateTime finalDeliveryTimeParsed = deliveryTimeParsed;
            logger.info(() -> "Extracted Delivery Time: " + finalDeliveryTimeParsed);
            logger.info(String.format("Extracted Closest Possible Delivery Time: %s", closestPossibleDeliveryTimeParsed));
        }

        try {
            groupOrderService.validateGroupOrder(orderID, deliveryTimeParsed, closestPossibleDeliveryTimeParsed);

            String response = JaxsonUtils.toJson(Map.of("message", "Group order successfully validated"));
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(HttpUtils.HttpStatusCode.OK, response.length());
            exchange.getResponseBody().write(response.getBytes());

        } catch (Exception e) {

            logger.warning("Failed to validate group order: " + e.getMessage());
            String response = JaxsonUtils.toJson(Map.of("error", e.getMessage()));
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(HttpUtils.HttpStatusCode.NOT_FOUND, -1);
            exchange.getResponseBody().write(response.getBytes());

        } finally {
            logger.info("Request processing complete");
            exchange.close();
        }
    }

    private void askToConfirmGroupOrder(HttpExchange exchange, String pathVariable, List<QueryParams> param) throws IOException {
        logger.info("Asking to confirm a group order");
        String path = exchange.getRequestURI().getPath();
        logger.info(() -> "Request path: " + path);

        String orderID = getQueryParamValue(param, "orderID");

        if (orderID == null) {
            logger.warning("Missing required parameter(s)");
            String response = JaxsonUtils.toJson(Map.of("error", "Missing required parameter(s)"));
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(HttpUtils.HttpStatusCode.BAD_REQUEST, -1);
            exchange.getResponseBody().write(response.getBytes());
            return;
        }
        logger.info(() -> "Extracted Order ID: " + orderID);

        try {
            groupOrderService.confirmGroupOrder(orderID);

            String response = JaxsonUtils.toJson(Map.of("message", "Group order successfully confirmed"));
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(HttpUtils.HttpStatusCode.OK, response.length());
            exchange.getResponseBody().write(response.getBytes());

        } catch (IllegalStateException e) {

            logger.warning("Failed to confirm group order: " + e.getMessage());
            String response = JaxsonUtils.toJson(Map.of("error", e.getMessage()));
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(HttpUtils.HttpStatusCode.NOT_FOUND, -1);
            exchange.getResponseBody().write(response.getBytes());

        } finally {
            logger.info("Request processing complete");
            exchange.close();
        }
    }

    private void askToDropSubOrder(HttpExchange exchange) throws IOException {
        logger.info("Asking to drop a sub-order");

        InputStream requestBody = exchange.getRequestBody();
        String body = new String(requestBody.readAllBytes(), StandardCharsets.UTF_8);
        logger.info(() -> REQUEST_BODY + body);

        JsonNode jsonNode = JaxsonUtils.getJsonNode(body);
        if (jsonNode == null) return;

        String orderID = jsonNode.path("orderID").asText(null);

        if (orderID == null) {
            logger.warning("Missing required parameter(s)");
            String response = JaxsonUtils.toJson(Map.of("error", "Missing required parameter(s)"));
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(HttpUtils.HttpStatusCode.BAD_REQUEST, -1);
            exchange.getResponseBody().write(response.getBytes());
            return;
        }
        logger.info(() -> "Extracted Order ID: " + orderID);

        try {
            groupOrderService.dropSubOrder(orderID);

            String response = JaxsonUtils.toJson(Map.of("message", "Sub-order successfully dropped from group"));
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(HttpUtils.HttpStatusCode.OK, response.length());
            exchange.getResponseBody().write(response.getBytes());

        } catch (EntityNotFoundException e) {

            logger.warning("Failed to drop sub-order: " + e.getMessage());
            String response = JaxsonUtils.toJson(Map.of("error", e.getMessage()));
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(HttpUtils.HttpStatusCode.NOT_FOUND, -1);
            exchange.getResponseBody().write(response.getBytes());

        } finally {
            logger.info("Request processing complete");
            exchange.close();
        }
    }

    private static String getQueryParamValue(List<QueryParams> params, String key) {
        return params.stream()
                .filter(param -> param.getKey().equals(key))
                .map(QueryParams::getValue)
                .findFirst()
                .orElse(null);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        logger.info("Handling HTTP request");
        // CORS

        if (HttpUtils.configureCors(exchange)) return;

        if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Accept, Content-Type, Authorization");
            exchange.sendResponseHeaders(HttpUtils.HttpStatusCode.NO_CONTENT, -1);
            return;
        }

        String requestMethod = exchange.getRequestMethod();
        String requestPath = exchange.getRequestURI().getPath();
        String requestQuery = exchange.getRequestURI().getQuery();

        logger.info(() -> "Incoming request: " + requestMethod + " " + requestPath);

        for (RouteInfo route : ApiRegistry.getRoutes(requestMethod)) {
            logger.info("Checking route: " + route.getMethod() + " " + route.getPath());

            if (route.matches(requestMethod, requestPath)) {
                logger.info("Route matched: " + route.getMethod() + " " + route.getPath());
                Matcher matcher = route.getPathMatcher(requestPath);
                String pathVariable = "";

                if (matcher.find() && matcher.groupCount() > 0) {
                    pathVariable = matcher.group(1);
                    logger.log(Level.INFO, String.format("Extracted parameter: %s", pathVariable));
                }

                List<QueryParams> queryParams = HttpUtils.parseQueryParams(requestQuery);
                route.getHandler().handle(exchange, pathVariable, queryParams);
                return;
            }
        }
        logger.warning(() -> "No matching route for: " + requestMethod + " " + requestPath);
        exchange.sendResponseHeaders(HttpUtils.HttpStatusCode.NOT_FOUND, -1);
    }

}