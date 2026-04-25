package com.tracker.AIService.client;


import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class UserClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String BASE_URL = "http://localhost:8081/api/users";

    public String getUserProfile(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);

        return restTemplate.exchange(
                BASE_URL + "/profile",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        ).getBody();
    }

}
