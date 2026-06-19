package org.local.springroutergraphql.controller;

import org.local.springroutergraphql.model.Account;
import org.local.springroutergraphql.model.Card;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Controller
public class CardGatewayController {

    private final WebClient cardsWebClient;
    private final HttpGraphQlClient accountsClient;

    public CardGatewayController(
            @Qualifier("cardsWebClient") WebClient cardsWebClient,
            @Qualifier("accountsClient") HttpGraphQlClient accountsClient) {
        this.cardsWebClient = cardsWebClient;
        this.accountsClient = accountsClient;
    }

    @QueryMapping
    public Mono<List<Card>> cards() {
        return cardsWebClient.get()
                .uri("/api/cards")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Card>>() {});
    }

    @QueryMapping
    public Mono<Card> card(@Argument String id) {
        return cardsWebClient.get()
                .uri("/api/cards/{id}", id)
                .retrieve()
                .bodyToMono(Card.class);
    }

    @QueryMapping
    public Mono<List<Card>> cardsByAccountId(@Argument String accountId) {
        return cardsWebClient.get()
                .uri("/api/cards/account/{accountId}", accountId)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Card>>() {});
    }

    @SchemaMapping(typeName = "Account", field = "cards")
    public Mono<List<Card>> cards(Account account) {
        return cardsWebClient.get()
                .uri("/api/cards/account/{accountId}", account.id())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Card>>() {});
    }

    @SchemaMapping(typeName = "Card", field = "account")
    public Mono<Account> account(Card card) {
        var representations = List.of(Map.of("__typename", "Account", "id", card.accountId()));
        return accountsClient
                .document("""
                        query($representations: [_Any!]!) {
                            _entities(representations: $representations) {
                                ... on Account {
                                    id name email balance
                                }
                            }
                        }
                        """)
                .variable("representations", representations)
                .execute()
                .map(response -> {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> entities = (List<Map<String, Object>>) response.field("_entities").getValue();
                    if (entities == null || entities.isEmpty()) return null;
                    Map<String, Object> a = entities.get(0);
                    return new Account(
                            (String) a.get("id"),
                            (String) a.get("name"),
                            (String) a.get("email"),
                            ((Number) a.get("balance")).doubleValue());
                });
    }
}
