package com.tracker.AIService.client;

import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class ScheduledTransactionClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String BASE_URL =
            "http://localhost:8082/api/scheduled-transactions";

    public String createScheduled(String json, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        return restTemplate.exchange(
                BASE_URL + "/create",
                HttpMethod.POST,
                new HttpEntity<>(json, headers),
                String.class
        ).getBody();
    }

    public String getByUser(Long userId, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);

        return restTemplate.exchange(
                BASE_URL + "/user/" + userId,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        ).getBody();
    }

    public String cancel(Long id, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);

        return restTemplate.exchange(
                BASE_URL + "/" + id + "/cancel",
                HttpMethod.PUT,
                new HttpEntity<>(headers),
                String.class
        ).getBody();
    }

    public String getById(Long id, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);

        return restTemplate.exchange(
                BASE_URL + "/" + id,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        ).getBody();
    }

    public String update(Long id, String json, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        return restTemplate.exchange(
                BASE_URL + "/update/" + id,
                HttpMethod.PUT,
                new HttpEntity<>(json, headers),
                String.class
        ).getBody();
    }
}