/*package fr.unice.polytech.server.apigateway;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import static fr.unice.polytech.server.httphandlers.HttpUtils.ServerPort.*;
import static org.junit.jupiter.api.Assertions.*;

class ApiGatewayTest {
    private ApiGateway apiGateway;
    private HttpServer gatewayServer;

    @BeforeEach
    void setUp() {
        apiGateway = new ApiGateway();
    }

    @AfterEach
    void tearDown() {
        if (gatewayServer != null) {
            gatewayServer.stop(0);
        }
        ApiGateway.stopServices();
    }

    @Test
    void testStartServices() throws IOException, InterruptedException {
        ApiGateway.startServices();

        TimeUnit.SECONDS.sleep(2);

        assertTrue(isServiceRunning(ApiGateway.RESTAURANT_SERVICE_URL));
        assertTrue(isServiceRunning(ApiGateway.LOCATION_SERVICE_URL));
        assertTrue(isServiceRunning(ApiGateway.ORDER_SERVICE_URL));
        assertTrue(isServiceRunning(ApiGateway.GROUP_ORDER_SERVICE_URL));
    }








    @Test
    void testServiceUrlConfiguration() {
        String baseUrl = "http://localhost:";
        assertEquals(ApiGateway.RESTAURANT_SERVICE_URL, baseUrl + RESTAURANT_SERVICE_PORT);
        assertEquals(ApiGateway.LOCATION_SERVICE_URL, baseUrl + LOCATION_SERVICE_PORT);
        assertEquals(ApiGateway.ORDER_SERVICE_URL, baseUrl + ORDER_SERVICE_PORT);
        assertEquals(ApiGateway.GROUP_ORDER_SERVICE_URL, baseUrl + GROUP_ORDER_SERVICE_PORT);
    }



    @Test
    void testRouteConfiguration() {
        ApiGateway gateway = new ApiGateway();
        assertNotNull(gateway);
    }

    @Test
    void testGatewayPort() {
        assertEquals(API_GATEWAY_PORT, 8080);
    }

    @Test
    void testServicePorts() {
        assertEquals(RESTAURANT_SERVICE_PORT, 8000);
        assertEquals(LOCATION_SERVICE_PORT, 8001);
        assertEquals(ORDER_SERVICE_PORT, 8003);
        assertEquals(GROUP_ORDER_SERVICE_PORT, 8002);
    }

    private boolean isServiceRunning(String serviceUrl) {
        try {
            URL url = new URL(serviceUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(1000);
            connection.connect();
            return true;
        } catch (ConnectException e) {
            return false;
        } catch (IOException e) {
            return false;
        }
    }

}

 */