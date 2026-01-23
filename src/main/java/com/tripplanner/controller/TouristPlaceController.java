package com.tripplanner.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.tripplanner.entity.TouristPlace;
import com.tripplanner.repository.TouristPlaceRepository;

@RestController
@RequestMapping("/api/places")
@CrossOrigin
public class TouristPlaceController {

    @Autowired
    private TouristPlaceRepository repository;

    @GetMapping
    public List<TouristPlace> getPlacesByCity(@RequestParam String city) {
        return repository.findByCity(city);
    }
}
