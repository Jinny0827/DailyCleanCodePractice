package org.example.cleancode.Y_2026.first_half.april.day107;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 *
 *
 */
public class Day107ShippingClient {

    private final String baseUrl;

    public Day107ShippingClient(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public ShippingResult requestShipping(String orderId, String address) {
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(1))
                    .build();

            String body = """
                    {"orderId":"%s","address":"%s"}
                    """.formatted(orderId, address);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/shipping/request"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .timeout(Duration.ofSeconds(1))
                    .build();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                // JSON 파싱 생략, trackingNumber만 추출
                String tracking = response.body()
                        .replaceAll(".*\"trackingNumber\":\"([^\"]+)\".*", "$1");
                return ShippingResult.success(tracking);
            }

            return ShippingResult.fail("HTTP " + response.statusCode());

        } catch (Exception e) {
            return ShippingResult.fail(e.getMessage());
        }
    }
}

record ShippingResult(boolean success, String value) {
    static ShippingResult success(String trackingNumber) {
        return new ShippingResult(true, trackingNumber);
    }
    static ShippingResult fail(String reason) {
        return new ShippingResult(false, reason);
    }
}
