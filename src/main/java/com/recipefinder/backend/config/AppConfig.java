package com.recipefinder.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    //gydkum-sAqmu3-rapcas
    //RecipeFinderBackend
    //david.magnucki@gmail.com
    //Application ID: 7280fca1
    //Application Keys: 6b7812b1a2b7fa5072b1c458424b6245
}
