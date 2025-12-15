package com.hrp.budg;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class BudgApplication {
    public static void main(String[] args) {
        SpringApplication.run(BudgApplication.class, args);
    }
}








