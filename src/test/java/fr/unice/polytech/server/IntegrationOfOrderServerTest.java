package fr.unice.polytech.server;

import com.sun.net.httpserver.HttpServer;
import fr.unice.polytech.application.usecase.OrderCoordinator;
import fr.unice.polytech.application.usecase.interfaces.IOrderPlacementCoordinator;
import fr.unice.polytech.domain.models.order.Order;

import fr.unice.polytech.domain.models.restaurant.MenuItem;
import fr.unice.polytech.domain.models.user.User;
import fr.unice.polytech.infrastructure.repository.inmemory.UserRepository;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class IntegrationOfOrderServerTest {
    private static final int PORT = 9004;
    private static final String BASE_URL = "http://localhost:" + PORT + "/api/orders";

    private static HttpServer server;
    private static IOrderPlacementCoordinator orderPlacementCoordinatorMock;
    private static IOrderPlacementCoordinator orderPlacementCoordinator;

    @BeforeAll
    static void setupServer() throws IOException {
        orderPlacementCoordinatorMock = mock(IOrderPlacementCoordinator.class);
        orderPlacementCoordinator = new OrderCoordinator();
        server = OrderHttpServer.startServer(PORT, orderPlacementCoordinatorMock);
    }

    @AfterAll
    static void tearDownServer() {
        server.stop(0);
    }

    @BeforeEach
    void resetMocks() {
        reset(orderPlacementCoordinatorMock);
    }

    @Test
    void testGetAllOrdersByUserId() throws IOException, InterruptedException {
        String userId = "2";
        Order mockOrder = orderPlacementCoordinator.createOrder("1", "2", "1", LocalDateTime.of(2024, 12, 31, 13, 0));

        when(orderPlacementCoordinatorMock.getUserCart(userId)).thenReturn(Optional.of(mockOrder));

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE_URL + "/user/" + userId))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        String expectedResponse = JaxsonUtils.toJson(List.of(mockOrder));
        assertEquals(expectedResponse, response.body());

        verify(orderPlacementCoordinatorMock, times(1)).getUserCart(userId);
    }

    @Test
    void testCreateOrder() throws IOException, InterruptedException {
        Order newOrder = orderPlacementCoordinator.createOrder("1", "2", "1", LocalDateTime.of(2024, 12, 31, 13, 0));
        when(orderPlacementCoordinatorMock.createOrder(anyString(), anyString(), anyString(), any(LocalDateTime.class))).thenReturn(newOrder);

        String newOrderJson = """
        {
            "restaurantId": "1",
            "userId": "2",
            "deliveryLocationId": "1",
            "deliveryTime": "2024-12-31T13:00:00"
        }
        """;

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(newOrderJson))
                .uri(URI.create(BASE_URL))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());
        String expectedResponse = JaxsonUtils.toJson(newOrder);
        assertEquals(expectedResponse, response.body());

        verify(orderPlacementCoordinatorMock, times(1)).createOrder(anyString(), anyString(), anyString(), any(LocalDateTime.class));
    }

    @Test
    void testAddOrderItem() throws IOException, InterruptedException {
        Order updatedOrder = orderPlacementCoordinator.createOrder("1", "2", "1", LocalDateTime.of(2024, 12, 31, 13, 0));
        String orderId = updatedOrder.getId();
        when(orderPlacementCoordinatorMock.addItemToOrder(eq(orderId), anyString(), anyInt())).thenReturn(updatedOrder);

        String addItemJson = """
        {
            "menuItemId": "item1",
            "quantity": 2
        }
        """;

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .PUT(HttpRequest.BodyPublishers.ofString(addItemJson))
                .uri(URI.create(BASE_URL + "/" + orderId + "/items"))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        String expectedResponse = JaxsonUtils.toJson(updatedOrder);
        assertEquals(expectedResponse, response.body());

        verify(orderPlacementCoordinatorMock, times(1)).addItemToOrder(eq(orderId), anyString(), anyInt());
    }

    @Test
    void testPlaceOrder() throws IOException, InterruptedException {
        Order mockOrder = orderPlacementCoordinator.createOrder("1", "2", "1", LocalDateTime.of(2024, 12, 31, 13, 0));
        String orderId = mockOrder.getId();
        when(orderPlacementCoordinatorMock.getOrderById(orderId)).thenReturn(mockOrder);
        when(orderPlacementCoordinatorMock.placeOrder(mockOrder)).thenReturn(true);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE_URL + "/" + orderId + "/placement"))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        String expectedResponse = JaxsonUtils.toJson("Your order has been placed successfully");
        assertEquals(expectedResponse, response.body());

        verify(orderPlacementCoordinatorMock, times(1)).getOrderById(orderId);
        verify(orderPlacementCoordinatorMock, times(1)).placeOrder(mockOrder);
    }

    @Test
    void testPayOrder() throws IOException, InterruptedException {
        Order mockOrder = orderPlacementCoordinator.createOrder("1", "2", "1", LocalDateTime.of(2024, 12, 31, 13, 0));
        String orderId = mockOrder.getId();
        when(orderPlacementCoordinatorMock.getOrderById(orderId)).thenReturn(mockOrder);
        when(orderPlacementCoordinatorMock.processPayment(mockOrder)).thenReturn(true);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE_URL + "/" + orderId + "/payment"))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        String expectedResponse = JaxsonUtils.toJson(mockOrder);
        assertEquals(expectedResponse, response.body());

        verify(orderPlacementCoordinatorMock, times(1)).getOrderById(orderId);
        verify(orderPlacementCoordinatorMock, times(1)).processPayment(mockOrder);
    }

    @Test
    void testAnswerWithAllItems() throws IOException, InterruptedException {
        String restaurantId = "restaurant1";
        List<MenuItem> mockItems = List.of(new MenuItem("Item 1", 10.0, 20), new MenuItem("Item 2", 15.0, 10));
        when(orderPlacementCoordinatorMock.getAllMenuItems(restaurantId)).thenReturn(mockItems);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE_URL + "/" + restaurantId + "/menu"))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        String expectedResponse = JaxsonUtils.toJson(mockItems);
        assertEquals(expectedResponse, response.body());

        verify(orderPlacementCoordinatorMock, times(1)).getAllMenuItems(restaurantId);
    }

    @Test
    void testAskToChooseRestaurant() throws IOException, InterruptedException {
        String userId = "user1";
        String restaurantId = "restaurant1";
        Order mockOrder = orderPlacementCoordinator.createOrder("1", "2", "1", LocalDateTime.of(2024, 12, 31, 13, 0));
        when(orderPlacementCoordinatorMock.chooseRestaurant(userId, restaurantId)).thenReturn(mockOrder);

        String requestBody = """
        {
            "restaurantId": "restaurant1"
        }
        """;

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .PUT(HttpRequest.BodyPublishers.ofString(requestBody))
                .uri(URI.create(BASE_URL + "/" + userId + "/chooseRestaurant"))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        String expectedResponse = JaxsonUtils.toJson(mockOrder);
        assertEquals(expectedResponse, response.body());

        verify(orderPlacementCoordinatorMock, times(1)).chooseRestaurant(userId, restaurantId);
    }

    @Test
    void testAnswerWithAvailableItems() throws IOException, InterruptedException {
        String restaurantId = "restaurant1";
        String deliveryDate = "2024-12-31T13:00:00";
        LocalDateTime deliveryDateTime = LocalDateTime.parse(deliveryDate);
        List<MenuItem> mockItems = List.of(new MenuItem("Item 1", 10.0, 20), new MenuItem("Item 2", 15.0, 10));
        when(orderPlacementCoordinatorMock.getAvailableMenuItems(restaurantId, deliveryDateTime)).thenReturn(mockItems);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE_URL + "/" + restaurantId + "/items?deliveryDate=" + deliveryDate))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        String expectedResponse = JaxsonUtils.toJson(mockItems);
        assertEquals(expectedResponse, response.body());

        verify(orderPlacementCoordinatorMock, times(1)).getAvailableMenuItems(restaurantId, deliveryDateTime);
    }

    @Test
    void testAnswerWithAllDeliveryTimes() throws IOException, InterruptedException {
        String restaurantId = "1";
        LocalDateTime orderDateTime = LocalDateTime.of(2024, 12, 31, 13, 00);
        List<LocalDateTime> mockTimes = List.of(orderDateTime.plusHours(1), orderDateTime.plusHours(2));
        when(orderPlacementCoordinatorMock.getAvailableDeliveryTime(restaurantId, orderDateTime)).thenReturn(mockTimes);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE_URL + "/" + restaurantId + "/times"))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        String expectedResponse = JaxsonUtils.toJson(mockTimes);
        assertEquals(expectedResponse, response.body());

        verify(orderPlacementCoordinatorMock, times(1)).getAvailableDeliveryTime(restaurantId, orderDateTime);
    }


    @Test
    void testAnswerWithOrder() throws IOException, InterruptedException {
        Order mockOrder = orderPlacementCoordinator.createOrder("1", "2", "1", LocalDateTime.of(2024, 12, 31, 13, 0));
        String orderId = mockOrder.getId();
        when(orderPlacementCoordinatorMock.getOrderById(orderId)).thenReturn(mockOrder);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE_URL + "/" + orderId))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        String expectedResponse = JaxsonUtils.toJson(mockOrder);
        assertEquals(expectedResponse, response.body());

        verify(orderPlacementCoordinatorMock, times(1)).getOrderById(orderId);
    }

    @Test
    void testAnswerWithOrderNotFound() throws IOException, InterruptedException {
        String orderId = "order1";
        when(orderPlacementCoordinatorMock.getOrderById(orderId)).thenReturn(null);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE_URL + "/" + orderId))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());

        verify(orderPlacementCoordinatorMock, times(1)).getOrderById(orderId);
    }
}
