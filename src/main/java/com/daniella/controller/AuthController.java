package com.daniella.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {

    @GetMapping("/auth/register") // Keep the URL full here
    public String register() {
        // This looks for src/main/resources/templates/auth/register.html
        return "auth/register"; 
    }
    
    @GetMapping("/auth/login")
    public String login() {
        return "auth/login";
    }
}
