package fr.unice.polytech.utils;

import java.time.LocalDateTime;
import java.util.UUID;

public class Utils {

    private Utils() {
    }

    public static String generateUniqueId() {
        return UUID.randomUUID().toString();
    }

    public static boolean isInFuture(LocalDateTime deliveryDate) {
        return deliveryDate.isAfter(LocalDateTime.now());
    }
}
