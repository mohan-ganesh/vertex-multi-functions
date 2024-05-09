package com.example.multifunctions.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMethod;

@RestController
public class HealthCheckController {

    @RequestMapping(path = "/healthcheck", method = RequestMethod.GET)
    public String healthCheck() {
        return "Application is up and running!";
    }

    @RequestMapping(path = "/", method = RequestMethod.GET)
    String home() {
        return "Hello World!";
    }
}
