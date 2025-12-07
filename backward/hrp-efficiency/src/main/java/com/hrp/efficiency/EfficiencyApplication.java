package com.hrp.efficiency;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class EfficiencyApplication {
    public static void main(String[] args) {
        SpringApplication.run(EfficiencyApplication.class, args);
    }
}

