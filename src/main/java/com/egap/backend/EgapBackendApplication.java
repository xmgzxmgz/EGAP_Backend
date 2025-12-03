package com.egap.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class EgapBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(EgapBackendApplication.class, args);
    }
}
