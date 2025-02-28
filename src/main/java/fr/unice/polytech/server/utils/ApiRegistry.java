package fr.unice.polytech.server.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Cette classe permet d'enregistrer et de récupérer un ensemble de routes.
 */
public class ApiRegistry {
    private static Map<String, List<RouteInfo>> routes = new HashMap<>();

    public static void registerRoute(String method, String path, RouteHandler handler) {
        routes.computeIfAbsent(method.toUpperCase(), k -> new ArrayList<>())
                .add(new RouteInfo(method.toUpperCase(), path, handler));
    }

    public static List<RouteInfo> getRoutes(String method) {
        return routes.getOrDefault(method.toUpperCase(), new ArrayList<>());
    }

//    public static List<RouteInfo> getAllRoutes() {
//        List<RouteInfo> allRoutes = new ArrayList<>();
//        for (List<RouteInfo> routeList : routes.values()) {
//            allRoutes.addAll(routeList);
//        }
//        return allRoutes;
//    }
}
