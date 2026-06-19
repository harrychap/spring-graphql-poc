package org.local.springroutergraphql.controller;

import org.local.springroutergraphql.model.Account;
import org.local.springroutergraphql.model.Transaction;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Controller
public class AccountGatewayController {

    private static final String ACCOUNT_FIELDS = "{ id name email balance }";

    private final HttpGraphQlClient accountsClient;
    private final HttpGraphQlClient transactionsClient;

    public AccountGatewayController(
            @Qualifier("accountsClient") HttpGraphQlClient accountsClient,
            @Qualifier("transactionsClient") HttpGraphQlClient transactionsClient) {
        this.accountsClient = accountsClient;
        this.transactionsClient = transactionsClient;
    }

    @QueryMapping
    public Mono<List<Account>> accounts() {
        return accountsClient
                .document("{ accounts " + ACCOUNT_FIELDS + " }")
                .retrieve("accounts")
                .toEntityList(Account.class);
    }

    @QueryMapping
    public Mono<Account> account(@Argument String id) {
        return accountsClient
                .document("query($id: ID!) { account(id: $id) " + ACCOUNT_FIELDS + " }")
                .variable("id", id)
                .retrieve("account")
                .toEntity(Account.class);
    }

    @SchemaMapping(typeName = "Account", field = "transactions")
    public Mono<List<Transaction>> transactions(Account account) {
        var representations = List.of(Map.of("__typename", "Account", "id", account.id()));
        return transactionsClient
                .document("""
                        query($representations: [_Any!]!) {
                            _entities(representations: $representations) {
                                ... on Account {
                                    transactions { id accountId amount description date }
                                }
                            }
                        }
                        """)
                .variable("representations", representations)
                .execute()
                .map(response -> {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> entities = (List<Map<String, Object>>) response.field("_entities").getValue();
                    if (entities == null || entities.isEmpty()) return List.<Transaction>of();
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> txns = (List<Map<String, Object>>) entities.get(0).get("transactions");
                    if (txns == null) return List.<Transaction>of();
                    return txns.stream()
                            .map(t -> new Transaction(
                                    (String) t.get("id"),
                                    (String) t.get("accountId"),
                                    ((Number) t.get("amount")).doubleValue(),
                                    (String) t.get("description"),
                                    (String) t.get("date")))
                            .toList();
                });
    }
}
