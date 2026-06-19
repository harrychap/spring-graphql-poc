package org.local.accountsgraphql.config;

import org.springframework.boot.graphql.autoconfigure.GraphQlSourceBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.data.federation.FederationSchemaFactory;

@Configuration
public class FederationConfig {

    @Bean
    public FederationSchemaFactory federationSchemaFactory() {
        return new FederationSchemaFactory();
    }

    @Bean
    public GraphQlSourceBuilderCustomizer federationCustomizer(FederationSchemaFactory schemaFactory) {
        return builder -> builder.schemaFactory(schemaFactory::createGraphQLSchema);
    }
}
