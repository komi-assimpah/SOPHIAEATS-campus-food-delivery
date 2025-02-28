package fr.unice.polytech.server;

import com.sun.net.httpserver.HttpServer;
import fr.unice.polytech.application.usecase.interfaces.ILocationService;
import fr.unice.polytech.domain.models.Address;
import fr.unice.polytech.domain.models.delivery.DeliveryLocation;
import org.junit.jupiter.api.*;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


public class IntegrationOfLocationServerTest {
    private static final int PORT = 9001;
    private static final String BASE_URL = "http://localhost:" + PORT + "/api/locations";

    private static HttpServer server;
    private static ILocationService locationServiceMock;

    @BeforeAll
    static void setupServer() throws IOException {
        locationServiceMock = mock(ILocationService.class);
        server = LocationHttpServer.startServer(PORT, locationServiceMock);
    }

    @AfterAll
    static void tearDownServer() {
        LocationHttpServer.stopServer(server);
    }

    @BeforeEach
    void resetMocks() {
        reset(locationServiceMock);
    }

    @Test
    void testGetAllLocations() throws IOException, InterruptedException {
        // Préparation des données simulées
        List<DeliveryLocation> mockLocations = List.of(
                new DeliveryLocation("1", "Location A", new Address("930 Route des Colles", "06410", "Biot", "France")),
                new DeliveryLocation("2", "Location B", new Address("123 Main Street", "75001", "Paris", "France"))
        );
        when(locationServiceMock.getDeliveryLocations()).thenReturn(mockLocations);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE_URL))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());


        assertEquals(200, response.statusCode());
        String expectedResponse = JaxsonUtils.toJson(mockLocations);
        assertEquals(expectedResponse, response.body());

        // Vérification des interactions
        verify(locationServiceMock, times(1)).getDeliveryLocations();
    }

    @Test
    void testGetLocationById() throws IOException, InterruptedException {
        // Préparation des données simulées
        DeliveryLocation mockLocation = new DeliveryLocation(
                "1", "Location A", new Address("930 Route des Colles", "06410", "Biot", "France"));
        when(locationServiceMock.getLocationById("1")).thenReturn(Optional.of(mockLocation));

        // Envoi de la requête
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE_URL + "/1"))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Validation des résultats
        assertEquals(200, response.statusCode());
        String expectedResponse = JaxsonUtils.toJson(mockLocation);
        assertEquals(expectedResponse, response.body());

        // Vérification des interactions
        verify(locationServiceMock, times(1)).getLocationById("1");
    }

    @Test
    void testCreateLocation() throws IOException, InterruptedException {
        DeliveryLocation newLocation = new DeliveryLocation("New Location", new Address("930 Route des Colles", "06410", "Biot", "France"));
        when(locationServiceMock.addLocation(any())).thenReturn(newLocation);

        String newLocationJson = """
        {
            "name": "New Location",
            "address": {
                "street": "930 Route des Colles",
                "zipCode": "06410",
                "city": "Biot",
                "country": "France"
            }
        }
        """;

        // Envoi de la requête
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(newLocationJson))
                .uri(URI.create(BASE_URL))
                .header("Content-Type", "application/json")
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());
        assertEquals(JaxsonUtils.toJson(newLocation), response.body());

        verify(locationServiceMock, times(1)).addLocation(any());
    }

    @Test
    void testGetLocationByNameNotFound() throws IOException, InterruptedException {
        when(locationServiceMock.getLocationByName("Unknown")).thenReturn(Optional.empty());

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE_URL + "/name?locationName=Unknown"))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
        String expectedResponse = "{\"error\":\"Location not found with name: Unknown\"}";
        assertEquals(expectedResponse, response.body());

        verify(locationServiceMock, times(1)).getLocationsByName("Unknown");
    }


    @Test
    void testGetLocationByNameFound() throws IOException, InterruptedException {
        String locationName = "locationA";
        DeliveryLocation expectedLocation = new DeliveryLocation(
                "1", locationName, new Address("930 Route des Colles", "06410", "Biot", "France")
        );
        when(locationServiceMock.getLocationsByName(locationName)).thenReturn(List.of(expectedLocation));

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(BASE_URL + "/name?locationName=" + locationName))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        String expectedResponse = JaxsonUtils.toJson(List.of(expectedLocation));
        assertEquals(expectedResponse, response.body());

        // Vérification des interactions
        verify(locationServiceMock, times(1)).getLocationsByName(locationName);
    }
}