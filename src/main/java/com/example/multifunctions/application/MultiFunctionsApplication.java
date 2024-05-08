package com.example.multifunctions.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.bind.annotation.RequestMapping;

@SpringBootApplication
@ComponentScan("com.example.multifunctions")
public class MultiFunctionsApplication {

    public static void main(String[] args) {
        SpringApplication.run(MultiFunctionsApplication.class, args);
    }

}
