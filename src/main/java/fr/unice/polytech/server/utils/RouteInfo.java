package fr.unice.polytech.server.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Contient les infos sur une route, ainsi que la logique permettant notamment de récupérer des paramètres dans l'URL, tels que /api/members/{memberId}
 */
public class RouteInfo {
    private final String method;
    private final String path;
    private final RouteHandler handler;

    // Pour gérer les paramètres dans les URLs (/api/members/{memberId} par exemple)
    private Pattern pathPattern;

    public RouteInfo(String method, String path, RouteHandler handler) {
        this.method = method;
        this.path = path;
        this.handler = handler;

        // Convertit le chemin avec paramètre en expression régulière "/api/members/{memberId}" devient "/api/members/([^/?]+)"
        String regexPath = path.replaceAll("\\{[^/]+}", "([^/?]+)");
        this.pathPattern = Pattern.compile(regexPath);
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public boolean matches(String method, String requestPath) {
        return this.method.equalsIgnoreCase(method) && pathPattern.matcher(requestPath).matches();
    }

    public Matcher getPathMatcher(String requestPath) {
        return pathPattern.matcher(requestPath);
    }

    public RouteHandler getHandler() {
        return handler;
    }

}
