package com.djw.autopartsbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class AutoPartsBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(AutoPartsBackendApplication.class, args);
    }

}
