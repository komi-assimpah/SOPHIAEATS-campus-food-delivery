package fr.unice.polytech.server;

import com.sun.net.httpserver.HttpServer;
import fr.unice.polytech.application.exceptions.RestaurantNotFoundException;
import fr.unice.polytech.application.usecase.interfaces.IRestaurantCapacityService;
import fr.unice.polytech.application.usecase.interfaces.IRestaurantScheduleManager;
import fr.unice.polytech.application.usecase.interfaces.IRestaurantService;
import fr.unice.polytech.domain.models.Address;
import fr.unice.polytech.domain.models.restaurant.MenuItem;
import fr.unice.polytech.domain.models.restaurant.Restaurant;
import fr.unice.polytech.domain.models.restaurant.Schedule;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class IntegrationOfRestaurantServerTest {

    private static final int PORT = 9002;
    private static final String BASE_URL = "http://localhost:" + PORT;

    private static HttpServer server;
    private static IRestaurantService restaurantServiceMock;
    private static IRestaurantScheduleManager restaurantScheduleManagerMock;
    private static IRestaurantCapacityService restaurantCapacityServiceMock;

    @BeforeAll
    static void setupServer() throws IOException {
        restaurantServiceMock = mock(IRestaurantService.class);
        restaurantScheduleManagerMock = mock(IRestaurantScheduleManager.class);
        restaurantCapacityServiceMock = mock(IRestaurantCapacityService.class);
        server = RestaurantHttpServer.startServer(PORT, restaurantServiceMock, restaurantScheduleManagerMock, restaurantCapacityServiceMock);
    }

    @AfterAll
    static void tearDownServer() {
        server.stop(0);
    }

    @BeforeEach
    void resetMocks() {
        reset(restaurantServiceMock, restaurantScheduleManagerMock, restaurantCapacityServiceMock);
    }

    @Test
    void testGetRestaurants() throws IOException, InterruptedException {
        Address address1 = new Address("930 Route des Colles", "06410", "Biot", "France");
        Address address2 = new Address("123 Main Street", "75001", "Paris", "France");
        List<Restaurant> mockRestaurants = List.of(
                new Restaurant("1", "Restaurant A", address1),
                new Restaurant("2", "Restaurant B", address2)
        );
        when(restaurantServiceMock.getAllRestaurants()).thenReturn(mockRestaurants);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE_URL + "/api/restaurants"))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        String expectedResponse = JaxsonUtils.toJson(mockRestaurants);
        assertEquals(expectedResponse, response.body());

        verify(restaurantServiceMock, times(1)).getAllRestaurants();
    }

    @Test
    void testGetRestaurantById() throws IOException, InterruptedException, RestaurantNotFoundException {
        String restaurantId = "1";
        Address address = new Address("930 Route des Colles", "06410", "Biot", "France");
        Restaurant mockRestaurant = new Restaurant(restaurantId, "Restaurant A", address);
        when(restaurantServiceMock.getRestaurantById(restaurantId)).thenReturn(mockRestaurant);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE_URL + "/api/restaurants/" + restaurantId))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        String expectedResponse = JaxsonUtils.toJson(mockRestaurant);
        assertEquals(expectedResponse, response.body());

        verify(restaurantServiceMock, times(1)).getRestaurantById(restaurantId);
    }

    @Test
    void testGetAvailableRestaurants() throws IOException, InterruptedException {
        Address address1 = new Address("930 Route des Colles", "06410", "Biot", "France");
        Address address2 = new Address("123 Main Street", "75001", "Paris", "France");
        Schedule schedule1 = new Schedule(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(17, 0), 5);
        Schedule schedule2 = new Schedule(DayOfWeek.TUESDAY, LocalTime.of(10, 0), LocalTime.of(18, 0), 6);
        Restaurant restaurant1 = new Restaurant("1", "Restaurant A", address1);
        Restaurant restaurant2 = new Restaurant("2", "Restaurant B", address2);
        restaurant1.addSchedule(schedule1);
        restaurant2.addSchedule(schedule2);
        List<Restaurant> mockRestaurants = List.of(restaurant1, restaurant2);
        LocalDateTime rightNow = LocalDateTime.of(2024, 11, 12, 12, 12);
        when(restaurantServiceMock.getAvailableRestaurants(rightNow)).thenReturn(mockRestaurants);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE_URL + "/api/restaurants/available"))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        String expectedResponse = JaxsonUtils.toJson(mockRestaurants);
        assertEquals(expectedResponse, response.body());

        verify(restaurantServiceMock, times(1)).getAvailableRestaurants(rightNow);
    }

    @Test
    void testCreateRestaurant() throws IOException, InterruptedException {
        Address address1 = new Address("930 Route des Colles", "06410", "Biot", "France");
        Restaurant newRestaurant = new Restaurant("1", "New Restaurant", address1);
        when(restaurantServiceMock.createRestaurant(anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(newRestaurant);

        String newRestaurantJson = """
        {
            "name": "New Restaurant",
            "address": {
                "street": "Street",
                "city": "City",
                "zipCode": "Zip",
                "country": "Country"
            }
        }
        """;

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(newRestaurantJson))
                .uri(URI.create(BASE_URL + "/api/restaurants"))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());
        String expectedResponse = JaxsonUtils.toJson(newRestaurant);
        assertEquals(expectedResponse, response.body());

        verify(restaurantServiceMock, times(1)).createRestaurant(anyString(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void testUpdateRestaurantMenu() throws IOException, InterruptedException {
        String restaurantId = "1";
        List<MenuItem> menuItems = List.of(new MenuItem("Item 1", 10.0, 20), new MenuItem("Item 2", 15.0, 10));
        doNothing().when(restaurantServiceMock).updateRestaurantMenu(eq(restaurantId), anyList());

        String menuItemsJson = """
                {
                  "menu": [
                    {
                      "id": "12345",
                      "name": "Burger Deluxe",
                      "price": 81.99,
                      "preparationTime": 15
                    },
                    {
                      "id": "67890",
                      "name": "Vegan Salad",
                      "price": 6.50,
                      "preparationTime": 10
                    },
                    {
                      "name": "Spaghetti Bolognese",
                      "price": 12.00,
                      "preparationTime": 20
                    },
                    {
                      "name": "Grilled Cheese Sandwich",
                      "price": 4.50,
                      "preparationTime": 8
                    }
                  ]
                }
        """;

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .PUT(HttpRequest.BodyPublishers.ofString(menuItemsJson))
                .uri(URI.create(BASE_URL + "/api/restaurants/" + restaurantId + "/menu"))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        String expectedResponse = "{\"message\":\"Menu updated successfully.\"}";
        assertEquals(expectedResponse, response.body());

        verify(restaurantServiceMock, times(1)).updateRestaurantMenu(eq(restaurantId), anyList());
    }

    @Test
    void testCreateSchedule() throws IOException, InterruptedException, RestaurantNotFoundException {
        String restaurantId = "1";
        Schedule newSchedule = new Schedule(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(17, 0), 5);
        doNothing().when(restaurantScheduleManagerMock).addSchedule(eq(restaurantId), any(Schedule.class));

        String newScheduleJson = """
        {
            "day": "MONDAY",
            "startTime": "09:00",
            "endTime": "17:00",
            "numberOfWorkingStaff": 5
        }
        """;

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(newScheduleJson))
                .uri(URI.create(BASE_URL + "/api/restaurants/" + restaurantId + "/schedule"))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());
        String expectedResponse = "{\"message\":\"Schedule created successfully.\"}";
        assertEquals(expectedResponse, response.body());

        verify(restaurantScheduleManagerMock, times(1)).addSchedule(eq(restaurantId), any(Schedule.class));
    }

    @Test
    void testUpdateRestaurantSchedule() throws IOException, InterruptedException, RestaurantNotFoundException {
        String restaurantId = "1";
        String scheduleId = "1";
        Schedule existingSchedule = new Schedule(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(17, 0), 5);
        Schedule newSchedule = new Schedule(DayOfWeek.TUESDAY, LocalTime.of(10, 0), LocalTime.of(18, 0), 6);
        when(restaurantScheduleManagerMock.getScheduleByRestaurantId(restaurantId, scheduleId)).thenReturn(existingSchedule);
        when(restaurantScheduleManagerMock.updateSchedule(eq(restaurantId), eq(existingSchedule), any(Schedule.class))).thenReturn(Optional.of(newSchedule));

        String newScheduleJson = """
        {
            "day": "TUESDAY",
            "startTime": "10:00",
            "endTime": "18:00",
            "numberOfWorkingStaff": 6
        }
        """;

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .PUT(HttpRequest.BodyPublishers.ofString(newScheduleJson))
                .uri(URI.create(BASE_URL + "/api/restaurants/" + restaurantId + "/schedule/" + scheduleId))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        String expectedResponse = "{\"message\":\"Schedule updated successfully.\"}";
        assertEquals(expectedResponse, response.body());

        verify(restaurantScheduleManagerMock, times(1)).getScheduleByRestaurantId(restaurantId, scheduleId);
        verify(restaurantScheduleManagerMock, times(1)).updateSchedule(eq(restaurantId), eq(existingSchedule), any(Schedule.class));
    }
}