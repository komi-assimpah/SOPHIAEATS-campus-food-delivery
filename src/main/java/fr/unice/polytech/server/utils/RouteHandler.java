package fr.unice.polytech.server.utils;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.List;

/**
 * Interface servant à définir la fonction de callback qui va être fournie au moment de l'enregistrement de la Route.
 * Note : le paramètre est obligatoire même s'il est vide, on devrait pouvoir améliorer ça.
 */
@FunctionalInterface
public interface RouteHandler {
    void handle(HttpExchange exchange, String pathVariable, List<QueryParams> queryParams) throws IOException;
}
