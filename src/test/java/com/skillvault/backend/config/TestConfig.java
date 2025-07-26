package com.skillvault.backend.config;

import com.skillvault.backend.Services.AzureService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestConfig {

    @Bean
    public AzureService azureService() {
        return Mockito.mock(AzureService.class);
    }
}
