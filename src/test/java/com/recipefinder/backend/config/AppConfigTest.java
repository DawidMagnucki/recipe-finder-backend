package com.recipefinder.backend.config;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class AppConfigTest {

    @Test
    void shouldCreateRestTemplateBean() {
        AppConfig appConfig = new AppConfig();

        RestTemplate restTemplate = appConfig.restTemplate();

        assertNotNull(restTemplate);
    }
}
