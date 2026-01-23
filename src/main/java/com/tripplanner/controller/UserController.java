package com.tripplanner.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    @GetMapping("/api/user/me")
    public String getCurrentUser() {

        Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();

        if (auth != null &&
            auth.isAuthenticated() &&
            !auth.getPrincipal().equals("anonymousUser")) {

            return auth.getName(); // username
        }

        return "Guest Planner";
    }
}
