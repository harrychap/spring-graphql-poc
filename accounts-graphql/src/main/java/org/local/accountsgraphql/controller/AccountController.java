package org.local.accountsgraphql.controller;

import org.local.accountsgraphql.model.Account;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;

@Controller
public class AccountController {

    private static final Logger log = LoggerFactory.getLogger(AccountController.class);

    private static final List<Account> ACCOUNTS = List.of(
        new Account("acc-1", "Alice Smith",   "alice@example.com",  2500.00),
        new Account("acc-2", "Bob Johnson",   "bob@example.com",    1800.50),
        new Account("acc-3", "Carol Williams","carol@example.com",  9400.75)
    );

    private static final Map<String, Account> BY_ID = Map.of(
        "acc-1", ACCOUNTS.get(0),
        "acc-2", ACCOUNTS.get(1),
        "acc-3", ACCOUNTS.get(2)
    );

    @QueryMapping
    public List<Account> accounts() {
        log.info("Query: accounts count={}", ACCOUNTS.size());
        return ACCOUNTS;
    }

    @QueryMapping
    public Account account(@Argument String id) {
        log.info("Query: account id={}", id);
        return BY_ID.get(id);
    }
}
