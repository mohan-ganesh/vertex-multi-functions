package com.example.multifunctions.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.stereotype.Controller;

@Controller
public class RedirectSwaggerController {

    @GetMapping("/")
    public String redirectToSwagger(RedirectAttributes attributes) {
        // Redirecting to swagger.html
        return "redirect:/swagger.html";
    }
}
