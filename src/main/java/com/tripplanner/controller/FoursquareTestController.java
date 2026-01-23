package com.tripplanner.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

@RestController
public class FoursquareTestController {

    @GetMapping("/test-fsq")
    public String testFoursquare() {

        String url = "https://api.foursquare.com/v3/places/search?query=beach&near=Udupi&limit=1";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + "GCMOLFGVFW5IC2FNICNZY0ZVWSRFN23XK2R4453111JIPXVL");
        headers.set("Accept", "application/json");

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                String.class
        );

        return response.getBody();
    }
}
