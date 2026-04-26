package com.tracker.AIService.client;

import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TransactionClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String BASE_URL = "http://localhost:8082/api/transactions";

    public String createTransaction(String json, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            return restTemplate.exchange(
                    BASE_URL + "/create",
                    HttpMethod.POST,
                    new HttpEntity<>(json, headers),
                    String.class
            ).getBody();
        } catch (Exception e) {
            System.out.println("TransactionClient CREATE ERROR: " + e.getMessage());
            throw e;
        }
    }

    public String getTransactionById(Long id, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);

        return restTemplate.exchange(
                BASE_URL + "/" + id,
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
                .queryParamIfPresent("type",     Optional.ofNullable(type))
                .queryParamIfPresent("category", Optional.ofNullable(category))
                .queryParamIfPresent("sortBy",   Optional.ofNullable(sortBy))
                .toUriString();

        return restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        ).getBody();
    }

    public String getUserBalance(Long userId, String token) {
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

    public String updateTransaction(Long id, String json, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            return restTemplate.exchange(
                    BASE_URL + "/update/" + id,
                    HttpMethod.PUT,
                    new HttpEntity<>(json, headers),
                    String.class
            ).getBody();
        } catch (Exception e) {
            System.out.println("TransactionClient UPDATE ERROR: " + e.getMessage());
            throw e;
        }
    }

    public String deleteTransaction(Long id, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    BASE_URL + "/delete/" + id,
                    HttpMethod.DELETE,
                    new HttpEntity<>(headers),
                    String.class
            );
            return response.getBody() != null ? response.getBody() : "DELETED";
        } catch (Exception e) {
            System.out.println("TransactionClient DELETE ERROR: " + e.getMessage());
            throw e;
        }
    }
}