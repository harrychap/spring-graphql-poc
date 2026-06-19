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
public class TransactionGatewayController {

    private static final String TRANSACTION_FIELDS = "{ id accountId amount description date }";

    private final HttpGraphQlClient transactionsClient;
    private final HttpGraphQlClient accountsClient;

    public TransactionGatewayController(
            @Qualifier("transactionsClient") HttpGraphQlClient transactionsClient,
            @Qualifier("accountsClient") HttpGraphQlClient accountsClient) {
        this.transactionsClient = transactionsClient;
        this.accountsClient = accountsClient;
    }

    @QueryMapping
    public Mono<List<Transaction>> transactions() {
        return transactionsClient
                .document("{ transactions " + TRANSACTION_FIELDS + " }")
                .retrieve("transactions")
                .toEntityList(Transaction.class);
    }

    @QueryMapping
    public Mono<Transaction> transaction(@Argument String id) {
        return transactionsClient
                .document("query($id: ID!) { transaction(id: $id) " + TRANSACTION_FIELDS + " }")
                .variable("id", id)
                .retrieve("transaction")
                .toEntity(Transaction.class);
    }

    @QueryMapping
    public Mono<List<Transaction>> transactionsByAccountId(@Argument String accountId) {
        return transactionsClient
                .document("query($accountId: ID!) { transactionsByAccountId(accountId: $accountId) " + TRANSACTION_FIELDS + " }")
                .variable("accountId", accountId)
                .retrieve("transactionsByAccountId")
                .toEntityList(Transaction.class);
    }

    @SchemaMapping(typeName = "Transaction", field = "account")
    public Mono<Account> account(Transaction transaction) {
        var representations = List.of(Map.of("__typename", "Account", "id", transaction.accountId()));
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
