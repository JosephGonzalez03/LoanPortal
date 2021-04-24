package com.nerd.LoanPortal.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

@Configuration
public class RestTemplateConfiguration {

    public RestTemplate restTemplate(HttpProperties properties) {
        RestTemplate restTemplate = new RestTemplate();

        restTemplate.setUriTemplateHandler(new DefaultUriBuilderFactory((
            properties.getProtocol() + "://" +
            properties.getHost() + ":" +
            properties.getPort() +
            properties.getBaseUri()
        )));
        return restTemplate;
    }

    @Bean
    @ConfigurationProperties(prefix = "loan-system-api.mock")
    public HttpProperties loanSystemApiProperties() { return new HttpProperties(); }

    @Bean
    @ConfigurationProperties(prefix = "payment-process-api.local")
    public HttpProperties paymentProcessApiProperties() { return new HttpProperties(); }
}
