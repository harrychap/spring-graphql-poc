package org.local.transactionsgraphql.controller;

import org.local.transactionsgraphql.model.Transaction;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class TransactionController {

    private static final List<Transaction> TRANSACTIONS = List.of(
        new Transaction("txn-1", "acc-1", "card-1", -49.99,  "Grocery store",    "2025-06-01"),
        new Transaction("txn-2", "acc-1", "card-2", -12.50,  "Coffee shop",      "2025-06-03"),
        new Transaction("txn-3", "acc-2", null,     1500.00, "Salary deposit",   "2025-06-01"),
        new Transaction("txn-4", "acc-2", "card-3", -200.00, "Rent payment",     "2025-06-05"),
        new Transaction("txn-5", "acc-3", "card-4", -89.00,  "Online shopping",  "2025-06-02"),
        new Transaction("txn-6", "acc-3", null,     300.00,  "Freelance income", "2025-06-04")
    );

    private static final Map<String, Transaction> BY_ID = TRANSACTIONS.stream()
        .collect(Collectors.toMap(Transaction::id, t -> t));

    @QueryMapping
    public List<Transaction> transactions() {
        return TRANSACTIONS;
    }

    @QueryMapping
    public Transaction transaction(@Argument String id) {
        return BY_ID.get(id);
    }

    @QueryMapping
    public List<Transaction> transactionsByAccountId(@Argument String accountId) {
        return TRANSACTIONS.stream()
            .filter(t -> t.accountId().equals(accountId))
            .toList();
    }
}
