package org.local.springroutergraphql.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class GatewayConfig {

    @Bean
    public HttpGraphQlClient accountsClient(@Value("${subgraphs.accounts-url}") String url) {
        return HttpGraphQlClient.builder(WebClient.create(url)).build();
    }

    @Bean
    public HttpGraphQlClient transactionsClient(@Value("${subgraphs.transactions-url}") String url) {
        return HttpGraphQlClient.builder(WebClient.create(url)).build();
    }
}
