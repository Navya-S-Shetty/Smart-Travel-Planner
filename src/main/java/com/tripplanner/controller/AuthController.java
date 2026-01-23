package com.tripplanner.controller;

import com.tripplanner.entity.User;
import com.tripplanner.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    // This matches the 'action="/register"' in your HTML form
    @PostMapping("/register")
    public String handleSignup(@ModelAttribute User user) {
        userService.registerNewUser(user);
        
        // After saving, send them to the login page
        return "redirect:/login.html?success"; 
    }
}