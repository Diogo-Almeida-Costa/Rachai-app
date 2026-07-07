package com.frequencia;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;


@SpringBootApplication(scanBasePackages = {"com.frequencia", "com.framework"})

public class FrequentaiAplication {

    public static void main(String[] args) {
        SpringApplication.run(FrequentaiAplication.class, args);
    }
    

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
