package com.hivcare;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
@SpringBootApplication
@EnableAsync
@EnableScheduling
public class HivCareApplication {
    public static void main(String[] args) {
        SpringApplication.run(HivCareApplication.class, args);

    }
    
}
