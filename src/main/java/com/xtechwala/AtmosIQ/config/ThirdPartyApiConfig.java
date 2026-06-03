package com.xtechwala.AtmosIQ.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class ThirdPartyApiConfig {

    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }

    @Bean
    public RestClient restClient(){
        return RestClient.create();
    }

    @Bean
    public WebClient webClient(){
        return WebClient.create();
    }

}
