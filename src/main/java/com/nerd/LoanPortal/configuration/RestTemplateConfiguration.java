package com.nerd.LoanPortal.configuration;

import com.nerd.LoanPortal.configuration.RestTemplateProperties;
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
    @ConfigurationProperties(prefix = "loan-api")
    public RestTemplateProperties loanApiProperties() { return new RestTemplateProperties(); }
}
