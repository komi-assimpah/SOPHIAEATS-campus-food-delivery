package fr.unice.polytech.server.httphandlers;


import com.fasterxml.jackson.databind.JsonNode;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import fr.unice.polytech.application.exceptions.EntityNotFoundException;
import fr.unice.polytech.application.usecase.interfaces.IUserService;
import fr.unice.polytech.domain.models.user.User;
import fr.unice.polytech.server.JaxsonUtils;
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

public class UserHttpHandler implements HttpHandler {

    Logger logger = Logger.getLogger(UserHttpHandler.class.getName());
    private final IUserService userService;

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // CORS
        if (HttpUtils.configureCors(exchange)) return;

        String requestMethod = exchange.getRequestMethod();
        String requestPath = exchange.getRequestURI().getPath();
        String requestQuery = exchange.getRequestURI().getQuery();


        logger.log(Level.INFO, () -> "UserHandler called: " + requestMethod + " " + requestPath);
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


    public UserHttpHandler(IUserService userService) {
        this.userService = userService;

        ApiRegistry.registerRoute("GET", "/api/users/{userId}", (exchange, userId, queryParams) -> {
            logger.info("GET user with id " + userId);
            askToGetUser(exchange, userId);
        });

        ApiRegistry.registerRoute("POST", "/api/users", (exchange, pathVariable, queryParams) -> {
            logger.info("POST user");
            askToCreateUser(exchange);
        });

        ApiRegistry.registerRoute("POST", "/api/users/login", (exchange, pathVariable, queryParams) -> {
            logger.info("POST user login");
            askToLoginUser(exchange);
        });
    }

    private void askToGetUser(HttpExchange exchange, String userId) throws IOException {
        logger.log(Level.INFO, "Received request for user ID: " + userId);
        try {
            Optional<User> user = userService.getUserById(userId);
            if (user.isPresent()) {
                String response = JaxsonUtils.toJson(user.get());
                HttpUtils.sendResponse(exchange, HttpUtils.HttpStatusCode.OK, response);
            } else {
                HttpUtils.sendResponse(exchange, HttpUtils.HttpStatusCode.NOT_FOUND, "{\"error\":\"User not found\"}");
            }
        } catch (Exception e) {
            HttpUtils.sendResponse(exchange, HttpUtils.HttpStatusCode.BAD_REQUEST, "{\"error\":\"Invalid input\"}");
        }

    }


    private void askToCreateUser(HttpExchange exchange) throws IOException {
        InputStream requestBody = exchange.getRequestBody();
        String body = new String(requestBody.readAllBytes(), StandardCharsets.UTF_8);
        logger.log(Level.INFO, "Request body: " + body);

        JsonNode jsonNode = JaxsonUtils.getJsonNode(body);
        if (jsonNode == null) return;

        String name = jsonNode.path("name").asText(null);
        String email = jsonNode.path("email").asText(null);
        String password = jsonNode.path("password").asText(null);

        try {
            User user = userService.createUser(name, email, password);
            String response = JaxsonUtils.toJson(user);
            HttpUtils.sendResponse(exchange, HttpUtils.HttpStatusCode.CREATED, response);
        } catch (Exception e) {
            HttpUtils.sendResponse(exchange, HttpUtils.HttpStatusCode.BAD_REQUEST, "{\"error\":\"Invalid input\"}");
        }
    }

    private void askToLoginUser(HttpExchange exchange) throws IOException {
        InputStream requestBody = exchange.getRequestBody();
        String body = new String(requestBody.readAllBytes(), StandardCharsets.UTF_8);
        logger.log(Level.INFO, "Request body: " + body);

        JsonNode jsonNode = JaxsonUtils.getJsonNode(body);
        if (jsonNode == null) return;

        String email = jsonNode.path("email").asText(null);
        String password = jsonNode.path("password").asText(null);

        try {
            User user = userService.authenticate(email, password);
            String response = JaxsonUtils.toJson(user);
            HttpUtils.sendResponse(exchange, HttpUtils.HttpStatusCode.OK, response);
        } catch (EntityNotFoundException e) {
            HttpUtils.sendResponse(exchange, HttpUtils.HttpStatusCode.UNAUTHORIZED, "{\"error\":\"Invalid credentials\"}");
        }
    }
}
