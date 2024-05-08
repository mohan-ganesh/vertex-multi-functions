package com.example.multifunctions.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckController {

    @GetMapping("/health")
    public String healthCheck() {
        return "Application is up and running!";
    }

    @RequestMapping("/")
    String home() {
        return "Hello World!";
    }
}
