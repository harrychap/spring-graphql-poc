package org.local.transactionsgraphql.controller;

import org.local.transactionsgraphql.model.Account;
import org.local.transactionsgraphql.model.Transaction;
import org.springframework.graphql.data.federation.EntityMapping;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class TransactionEntityController {

    private static final List<Transaction> TRANSACTIONS = List.of(
        new Transaction("txn-1", "acc-1", -49.99,  "Grocery store",    "2025-06-01"),
        new Transaction("txn-2", "acc-1", -12.50,  "Coffee shop",      "2025-06-03"),
        new Transaction("txn-3", "acc-2", 1500.00, "Salary deposit",   "2025-06-01"),
        new Transaction("txn-4", "acc-2", -200.00, "Rent payment",     "2025-06-05"),
        new Transaction("txn-5", "acc-3", -89.00,  "Online shopping",  "2025-06-02"),
        new Transaction("txn-6", "acc-3", 300.00,  "Freelance income", "2025-06-04")
    );

    private static final Map<String, Transaction> BY_ID = TRANSACTIONS.stream()
        .collect(Collectors.toMap(Transaction::id, t -> t));

    @EntityMapping
    public Transaction transaction(@Argument String id) {
        return BY_ID.get(id);
    }

    @SchemaMapping(typeName = "Account", field = "transactions")
    public List<Transaction> transactions(Account account) {
        return TRANSACTIONS.stream()
            .filter(t -> t.accountId().equals(account.id()))
            .toList();
    }
}
