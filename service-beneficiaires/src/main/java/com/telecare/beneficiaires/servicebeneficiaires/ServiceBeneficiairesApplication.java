package com.telecare.beneficiaires.servicebeneficiaires;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class ServiceBeneficiairesApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceBeneficiairesApplication.class, args);
    }
}