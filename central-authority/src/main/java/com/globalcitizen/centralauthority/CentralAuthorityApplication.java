package com.globalcitizen.centralauthority;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Central Authority Application
 * Manages mother key and global keys for the Global Citizen system
 */
@SpringBootApplication
@EnableScheduling
public class CentralAuthorityApplication {

    public static void main(String[] args) {
        SpringApplication.run(CentralAuthorityApplication.class, args);
    }
} 