package com.tripplanner.service;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class WeatherService {

    @Value("${weather.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public JSONObject getWeather(double lat, double lon) {

        String url = "https://api.openweathermap.org/data/2.5/weather"
                + "?lat=" + lat
                + "&lon=" + lon
                + "&units=metric"
                + "&appid=" + apiKey;

        String response = restTemplate.getForObject(url, String.class);
        return new JSONObject(response);
    }
}
