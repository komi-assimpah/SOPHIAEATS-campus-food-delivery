package fr.unice.polytech.domain.models.restaurant;

import fr.unice.polytech.domain.models.order.OrderItem;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Logger;

public class MenuItem {
    private String id;
    private String name;
    private double price;
    private int preparationTimeInMinutes; // Preparation time in minutes
    private int preparationTimeInSeconds;

    Logger logger = Logger.getLogger(MenuItem.class.getName());


    public MenuItem(String menuItemId, String menuItemName, double menuItemPrice, int preparationTime) {
        this.id = menuItemId;
        this.name = menuItemName;
        this.price = validatePrice(menuItemPrice);
        this.preparationTimeInMinutes = validatePreparationTime(preparationTime);
    }

    public MenuItem(String menuItemName, double menuItemPrice, int preparationTime) {
        this.id = UUID.randomUUID().toString();
        this.name = menuItemName;
        this.price = validatePrice(menuItemPrice);
        this.preparationTimeInMinutes = validatePreparationTime(preparationTime);
    }

    public MenuItem(){
        this.id = null ;
        this.name = null ;
        this.price = 0;
        this.preparationTimeInMinutes =0 ;
    }


    // Getters and Setters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = validatePrice(price);
    }

    public int getPreparationTimeInMinutes() {
        return preparationTimeInMinutes;
    }

    public int getPreparationTimeInSeconds() {
        this.preparationTimeInSeconds = preparationTimeInMinutes*60;
        return
                preparationTimeInMinutes*60;
    }

    public void setPreparationTime(int preparationTime) {
        this.preparationTimeInMinutes = validatePreparationTime(preparationTime);
    }



    public boolean isAvailable(LocalTime orderTime, LocalTime deliveryTime) {
        LocalTime expectedTimeAtPreparationEnd = orderTime.plusMinutes(this.preparationTimeInMinutes);
        return !expectedTimeAtPreparationEnd.isAfter(deliveryTime);
    }

    private double validatePrice(double price) {
        if (price < 0) {
            logger.warning("Warning: Price cannot be negative. Setting price to 0.");
            return 0.0;
        }
        return price;
    }

    private int validatePreparationTime(int preparationTime) {
        if (preparationTime < 0) {
            logger.warning("Warning: Preparation time cannot be negative. Setting preparation time to 0.");
            return 1;
        }
        return preparationTime;
    }

    public Map<String, Object> serializeMenuItemForFirebase() {
        Map<String, Object> menuItemData = new HashMap<>();
        menuItemData.put("id", id);
        menuItemData.put("name", name);
        menuItemData.put("price", price);
        menuItemData.put("preparationTime", preparationTimeInMinutes);  // This stores preparation time in minutes

        return menuItemData;
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MenuItem menuItem = (MenuItem) o;
        return name.equals(menuItem.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
