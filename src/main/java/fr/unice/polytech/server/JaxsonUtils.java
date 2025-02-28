package fr.unice.polytech.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import fr.unice.polytech.application.dto.PercentageDiscountDTO;
import fr.unice.polytech.domain.models.restaurant.discountstrategy.PercentageDiscount;

import java.io.IOException;

public class JaxsonUtils {

    private JaxsonUtils() {
    }

    public static String toJson(Object object) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        mapper.registerModule(new SimpleModule().addSerializer(PercentageDiscount.class, new PercentageDiscountDTO()));
        return mapper.writeValueAsString(object);
    }

    public static <T> T fromJson(String json, Class<T> clazz) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(json, clazz);
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }

    public static JsonNode getJsonNode(String body) {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode;

        try {
            jsonNode = objectMapper.readTree(body);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return jsonNode;
    }
}
