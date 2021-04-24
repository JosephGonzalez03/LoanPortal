package com.nerd.LoanPortal.configuration;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
public class HttpProperties {
    private String protocol;
    private String host;
    private Integer port;
    private String baseUri;
}
