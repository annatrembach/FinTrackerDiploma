package com.tracker.AIService.client;

import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
public class GoalClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String BASE_URL = "http://localhost:8083/api/goals";

    public String createGoal(String goalJson, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        return restTemplate.exchange(
                BASE_URL + "/create",
                HttpMethod.POST,
                new HttpEntity<>(goalJson, headers),
                String.class
        ).getBody();
    }

    public String getGoalById(Long goalId, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);

        return restTemplate.exchange(
                BASE_URL + "/" + goalId,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        ).getBody();
    }

    public String getAllGoals(String status, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);

        String url = UriComponentsBuilder.fromUriString(BASE_URL + "/")
                .queryParamIfPresent("goalStatus", java.util.Optional.ofNullable(status))
                .toUriString();

        return restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        ).getBody();
    }

    public String getUserGoals(Long userId, String status, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);

        String url = UriComponentsBuilder
                .fromUriString(BASE_URL + "/user/" + userId)
                .queryParamIfPresent("goalStatus", java.util.Optional.ofNullable(status))
                .toUriString();

        return restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        ).getBody();
    }

    public String updateGoal(Long goalId, String goalJson, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        return restTemplate.exchange(
                BASE_URL + "/update/" + goalId,
                HttpMethod.PUT,
                new HttpEntity<>(goalJson, headers),
                String.class
        ).getBody();
    }

    public String deleteGoal(Long goalId, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);

        return restTemplate.exchange(
                BASE_URL + "/delete/" + goalId,
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                String.class
        ).getBody();
    }

    public String completeGoal(Long goalId, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);

        return restTemplate.exchange(
                BASE_URL + "/" + goalId + "/complete",
                HttpMethod.PUT,
                new HttpEntity<>(headers),
                String.class
        ).getBody();
    }
}
