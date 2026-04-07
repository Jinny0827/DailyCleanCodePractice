package org.example.cleancode.Y_2026.first_half.april.day107;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static com.github.tomakehurst.wiremock.client.WireMock.*;

/**
 * Day 107 — WireMock: 외부 HTTP API 목킹
 *
 * 1. 실제 외부 서버 의존 -> @WireMockTest로 로컬 Mock 서버 사용
 * 2. 500 응답 시나리오 불가 -> stubFor + withStatus(500) 로 제어
 * 3. 타임아웃 테스트 불가 -> withFixedDelay(ms) 로 지연 시뮬레이션
 * 4. 요청 본문 검증 없음 -> withRequestBody(containing(...)) 추가
 */

@WireMockTest
public class Day107ShippingClientTest {

    @Test
    void shippingSuccess(WireMockRuntimeInfo wmRuntimeInfo) {
        stubFor(post(urlEqualTo("/shipping/request"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                 {"trackingNumber":"TRK-12345"}
                                """)));

        Day107ShippingClient client =
                new Day107ShippingClient(wmRuntimeInfo.getHttpBaseUrl());
        
        ShippingResult result = client.requestShipping("ORD-001", "서울시 강남구");

        assertTrue(result.success());

        verify(postRequestedFor(urlEqualTo("/shipping/request"))
                .withRequestBody(containing("ORD-001")));
    }

    @Test
    void shippingFailure(WireMockRuntimeInfo wmRuntimeInfo) {
        stubFor(post(urlEqualTo("/shipping/request"))
                .willReturn((aResponse()
                        .withStatus(500))));

        Day107ShippingClient client = new Day107ShippingClient(wmRuntimeInfo.getHttpBaseUrl());
        
        ShippingResult result = client.requestShipping("BAD-ORDER","주소 없음");

        assertFalse(result.success());
        assertThat(result.value()).isEqualTo("HTTP 500");
    }

    @Test
    void shippingTimeout(WireMockRuntimeInfo wmRuntimeInfo) {
        stubFor(post(urlEqualTo("/shipping/request"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withFixedDelay(3000)));

        Day107ShippingClient client =
                new Day107ShippingClient(wmRuntimeInfo.getHttpBaseUrl());
        
        ShippingResult result = client.requestShipping("ORD-001", "서울시 강남구");

        assertFalse(result.success());
        assertThat(result.value()).contains("timed out");
    }
}
