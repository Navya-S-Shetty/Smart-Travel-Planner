package com.tripplanner.controller;

import com.tripplanner.entity.User;
import com.tripplanner.entity.Landmark;
import com.tripplanner.service.LandmarkService;
import com.tripplanner.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/landmarks")
public class LandmarkController {

    @Autowired
    private LandmarkService landmarkService;

    @Autowired
    private UserService userService;

    // This is the endpoint the frontend will call to save a place
    @PostMapping("/save")
    public ResponseEntity<?> saveLandmark(@RequestBody Landmark landmark, Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).body("Please log in first");
        }
        User user = userService.findByUsername(authentication.getName());
        Landmark saved = landmarkService.saveLandmark(landmark);
        return ResponseEntity.ok(saved);
    }

    // This is the endpoint to see the user's saved places
  
    
}