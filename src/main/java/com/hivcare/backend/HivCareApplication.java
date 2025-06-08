package com.hivcare.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@EnableJpaAuditing
@EnableAsync
@EnableScheduling
@RestController
public class HivCareApplication {

    public static void main(String[] args) {
        SpringApplication.run(HivCareApplication.class, args);
    }

    @GetMapping("/")
    public String home() {
        return " HIV Care API is running successfully! <br><br>" +
               " Available public endpoints:<br>" +
               "• GET /public/health - API health check<br>" +
               "• GET /public/doctors - List all doctors<br>" +
               "• GET /public/stats - Public statistics<br>" +
               "• POST /auth/login - User login<br>" +
               "• POST /auth/register - User registration<br><br>" +
               " Protected endpoints (require authentication):<br>" +
               "• GET /dashboard/patient - Patient dashboard<br>" +
               "• GET /dashboard/doctor - Doctor dashboard<br>" +
               "• GET /appointments - User appointments<br>" +
               "• GET /test-results - Test results<br>" +
               "• GET /medications/regimens - ARV regimens";
    }

    @GetMapping("/health")
    public String health() {
        return " OK - HIV Care Backend is healthy and running!";
    }
}