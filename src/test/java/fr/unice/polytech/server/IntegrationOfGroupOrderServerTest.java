package fr.unice.polytech.server;

import com.sun.net.httpserver.HttpServer;
import fr.unice.polytech.application.usecase.interfaces.IGroupOrderService;
import fr.unice.polytech.domain.models.groupOrder.GroupOrder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class IntegrationOfGroupOrderServerTest {
    private static final int PORT = 9003;
    private static final String BASE_URL = "http://localhost:" + PORT + "/api/groupOrders";

    private static HttpServer server;
    private static IGroupOrderService groupOrderServiceMock;

    @BeforeAll
    static void setupServer() throws IOException {
        groupOrderServiceMock = mock(IGroupOrderService.class);
        server = GroupOrderHttpServer.startServer(PORT, groupOrderServiceMock);
    }

    @AfterAll
    static void tearDownServer() {
        server.stop(0);
    }

    @BeforeEach
    void resetMocks() {
        reset(groupOrderServiceMock);
    }

    @Test
    void testGetAllGroupOrders() throws IOException, InterruptedException {
        List<GroupOrder> mockGroupOrders = List.of(
                new GroupOrder("go1", LocalDateTime.of(2024, 12, 11, 12, 0)),
                new GroupOrder("go2", LocalDateTime.of(2024, 12, 11, 12, 0))
        );
        when(groupOrderServiceMock.getAllGroupOrders()).thenReturn(mockGroupOrders);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE_URL))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("go1"));
        assertTrue(response.body().contains("go2"));
        String expectedResponse = JaxsonUtils.toJson(mockGroupOrders);
        assertEquals(expectedResponse, response.body());
        verify(groupOrderServiceMock, times(1)).getAllGroupOrders();
    }

    @Test
    void  testGetGroupOrderById() throws IOException, InterruptedException {
        GroupOrder mockGroupOrder = new GroupOrder("go1", LocalDateTime.of(2024, 12, 11, 12, 0));
        when(groupOrderServiceMock.findGroupOrderById("go1")).thenReturn(mockGroupOrder);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE_URL + "/group/go1"))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("go1"));
        String expectedResponse = JaxsonUtils.toJson(mockGroupOrder);
        assertEquals(expectedResponse, response.body());
        verify(groupOrderServiceMock, times(1)).findGroupOrderById("go1");
    }

    @Test
    void testCreateGroupOrder() throws IOException, InterruptedException {
        String locationId = "location123";
        LocalDateTime deliveryTime = LocalDateTime.of(2024, 12, 25, 12, 0);
        GroupOrder newOrder = new GroupOrder(locationId, deliveryTime);

        when(groupOrderServiceMock.createGroupOrder(anyString(), anyString(), any())).thenReturn(newOrder.getGroupID());

        String requestBody = """
        {
            "orderID": "order1",
            "locationID": "location123",
            "deliveryTime": "2024-12-25T12:00:00"
        }
        """;

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/create"))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());
        assertTrue(response.body().contains(newOrder.getGroupID()));
        verify(groupOrderServiceMock, times(1)).createGroupOrder(eq("order1"), eq("location123"), eq(deliveryTime));
    }

    @Test
    void testJoinGroupOrder() throws IOException, InterruptedException {
        String groupId = "go1";
        String orderId = "order1";

        doNothing().when(groupOrderServiceMock).joinGroupOrder(orderId, groupId);

        String requestBody = """
            {
                "orderID": "order1"
            }
            """;

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/join/" + groupId))
                .PUT(HttpRequest.BodyPublishers.ofString(requestBody))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Sub-order successfully joined group"));
        verify(groupOrderServiceMock, times(1)).joinGroupOrder(orderId, groupId);
    }

    @Test
    void testGetSubOrderGroup() throws IOException, InterruptedException {
        String orderId = "order1";
        GroupOrder mockGroupOrder = new GroupOrder("go1", LocalDateTime.of(2024, 12, 11, 12, 0));
        when(groupOrderServiceMock.findSubOrderGroup(orderId)).thenReturn(mockGroupOrder);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE_URL + "/orderGroup?orderID=" + orderId))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("go1"));
        String expectedResponse = JaxsonUtils.toJson(mockGroupOrder);
        assertEquals(expectedResponse, response.body());
        verify(groupOrderServiceMock, times(1)).findSubOrderGroup(orderId);
    }

    @Test
    void testGetSubOrderGroup_MissingOrderId() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE_URL + "/orderGroup"))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode());
        verify(groupOrderServiceMock, never()).findSubOrderGroup(any());
    }

    @Test
    void testCompleteOrderGroup() throws IOException, InterruptedException {
        String orderId = "order1";
        doNothing().when(groupOrderServiceMock).completeOrderGroup(orderId);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/complete?orderID=" + orderId))
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Group order successfully completed"));
        verify(groupOrderServiceMock, times(1)).completeOrderGroup(orderId);
    }

    @Test
    void testCompleteOrderGroup_MissingOrderId() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/complete"))
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode());
        verify(groupOrderServiceMock, never()).completeOrderGroup(any());
    }


    @Test
    void testValidateGroupOrder() throws IOException, InterruptedException {
        String orderId = "order1";
        LocalDateTime expectedDeliveryTime = LocalDateTime.of(2024, 12, 25, 12, 0);
        LocalDateTime closestPossibleDeliveryTime = LocalDateTime.of(2024, 12, 25, 12, 0);

        doNothing().when(groupOrderServiceMock).validateGroupOrder(orderId, expectedDeliveryTime, closestPossibleDeliveryTime);

        String requestBody = """
        {
            "orderID": "%s",
            "deliveryTime": "%s",
            "possibleTime": "%s"
        }
        """.formatted(orderId, expectedDeliveryTime, closestPossibleDeliveryTime);


        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/validate"))
                .PUT(HttpRequest.BodyPublishers.ofString(requestBody))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Group order successfully validated"));
        verify(groupOrderServiceMock, times(1)).validateGroupOrder(orderId, expectedDeliveryTime, closestPossibleDeliveryTime);
    }


    @Test
    void testDropSubOrder() throws IOException, InterruptedException {
        String orderId = "order1";
        doNothing().when(groupOrderServiceMock).dropSubOrder(orderId);

        String requestBody = """
            {
                "orderID": "%s"
            }
            """.formatted(orderId);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/dropSub"))
                .PUT(HttpRequest.BodyPublishers.ofString(requestBody))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Sub-order successfully dropped from group"));
        verify(groupOrderServiceMock, times(1)).dropSubOrder(orderId);
    }

    @Test
    void testDropSubOrder_MissingOrderId() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/dropSub"))
                .PUT(HttpRequest.BodyPublishers.ofString("{}"))
                .header("Content-Type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode());
        verify(groupOrderServiceMock, never()).dropSubOrder(any());
    }

    @Test
    void testConfirmGroupOrder() throws IOException, InterruptedException {
        String orderId = "order1";
        doNothing().when(groupOrderServiceMock).confirmGroupOrder(orderId);

        String requestBody = """
            {
                "orderID": "%s"
            }
            """.formatted(orderId);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/confirm?orderID=" + orderId))
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Group order successfully confirmed"));
        verify(groupOrderServiceMock, times(1)).confirmGroupOrder(orderId);
    }

    @Test
    void testConfirmGroupOrder_MissingOrderId() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/confirm"))
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode());
        verify(groupOrderServiceMock, never()).confirmGroupOrder(any());
    }


}
