package com.tracker.AIService.client;

import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
public class TransactionClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String BASE_URL = "http://localhost:8082/api/transactions";

    public String getBalance(Long userId, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);

        return restTemplate.exchange(
                BASE_URL + "/balance/" + userId,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        ).getBody();
    }

    public String getMonthlySummary(Long userId, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);

        return restTemplate.exchange(
                BASE_URL + "/monthly-summary/" + userId,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        ).getBody();
    }

    public String getUserTransactions(Long userId, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);

        return restTemplate.exchange(
                BASE_URL + "/user/" + userId,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        ).getBody();
    }

    public String getFilteredTransactions(String type, String category, String sortBy, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);

        String url = UriComponentsBuilder.fromUriString(BASE_URL + "/filter")
                .queryParamIfPresent("type", java.util.Optional.ofNullable(type))
                .queryParamIfPresent("category", java.util.Optional.ofNullable(category))
                .queryParamIfPresent("sortBy", java.util.Optional.ofNullable(sortBy))
                .toUriString();

        return restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        ).getBody();
    }

    public String createTransaction(String transactionJson, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        return restTemplate.exchange(
                BASE_URL + "/create",
                HttpMethod.POST,
                new HttpEntity<>(transactionJson, headers),
                String.class
        ).getBody();
    }
}