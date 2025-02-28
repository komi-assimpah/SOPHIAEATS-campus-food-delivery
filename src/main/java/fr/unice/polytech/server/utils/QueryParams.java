package fr.unice.polytech.server.utils;

public class QueryParams {
    private String key;
    private String value;

    public QueryParams(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }
}
