package com.project.url_shortener;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UrlShortenerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate testRestTemplate;

    private RestTemplate noRedirectRestTemplate;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port;
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory() {
            @Override
            protected void prepareConnection(HttpURLConnection connection, String httpMethod) throws IOException {
                super.prepareConnection(connection, httpMethod);
                connection.setInstanceFollowRedirects(false);
            }
        };
        noRedirectRestTemplate = new RestTemplate(requestFactory);
    }

    @Test
    void shortenAndRedirectRoundTrip() {
        Map<String, String> request = Map.of("url", "https://example.com/page");
        ResponseEntity<Map> shortenResponse = testRestTemplate.postForEntity(
                baseUrl + "/shorten", request, Map.class);

        assertEquals(HttpStatus.CREATED, shortenResponse.getStatusCode());
        assertNotNull(shortenResponse.getBody());
        String shortCode = (String) shortenResponse.getBody().get("shortCode");
        assertNotNull(shortCode);

        ResponseEntity<Void> redirectResponse = noRedirectRestTemplate.exchange(
                baseUrl + "/" + shortCode,
                HttpMethod.GET,
                new HttpEntity<>(new HttpHeaders()),
                Void.class);

        assertEquals(HttpStatus.MOVED_PERMANENTLY, redirectResponse.getStatusCode());
        assertEquals("https://example.com/page", redirectResponse.getHeaders().getLocation().toString());
    }

    @Test
    void unknownCodeReturns404() {
        ResponseEntity<Map> response = testRestTemplate.getForEntity(baseUrl + "/unknown-code", Map.class);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void duplicateUrlReusesSameCode() {
        Map<String, String> request = Map.of("url", "https://duplicate.example.com");

        ResponseEntity<Map> first = testRestTemplate.postForEntity(baseUrl + "/shorten", request, Map.class);
        ResponseEntity<Map> second = testRestTemplate.postForEntity(baseUrl + "/shorten", request, Map.class);

        assertEquals(HttpStatus.CREATED, first.getStatusCode());
        assertEquals(HttpStatus.OK, second.getStatusCode());
        assertEquals(first.getBody().get("shortCode"), second.getBody().get("shortCode"));
        assertTrue((Boolean) second.getBody().get("reused"));
    }

    @Test
    void customAliasConflictReturns409() {
        Map<String, String> first = Map.of(
                "url", "https://one.example.com",
                "customAlias", "shared-alias"
        );
        Map<String, String> second = Map.of(
                "url", "https://two.example.com",
                "customAlias", "shared-alias"
        );

        ResponseEntity<Map> firstResponse = testRestTemplate.postForEntity(baseUrl + "/shorten", first, Map.class);
        ResponseEntity<Map> secondResponse = testRestTemplate.postForEntity(baseUrl + "/shorten", second, Map.class);

        assertEquals(HttpStatus.CREATED, firstResponse.getStatusCode());
        assertEquals(HttpStatus.CONFLICT, secondResponse.getStatusCode());
    }

    @Test
    void invalidUrlReturns400() {
        Map<String, String> request = Map.of("url", "javascript:alert(1)");
        ResponseEntity<Map> response = testRestTemplate.postForEntity(baseUrl + "/shorten", request, Map.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
