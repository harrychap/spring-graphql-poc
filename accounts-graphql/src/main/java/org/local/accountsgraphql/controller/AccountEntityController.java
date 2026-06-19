package org.local.accountsgraphql.controller;

import org.local.accountsgraphql.model.Account;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.graphql.data.federation.EntityMapping;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
public class AccountEntityController {

    private static final Logger log = LoggerFactory.getLogger(AccountEntityController.class);

    private static final Map<String, Account> BY_ID = Map.of(
        "acc-1", new Account("acc-1", "Alice Smith",    "alice@example.com",  2500.00),
        "acc-2", new Account("acc-2", "Bob Johnson",    "bob@example.com",    1800.50),
        "acc-3", new Account("acc-3", "Carol Williams", "carol@example.com",  9400.75)
    );

    @EntityMapping
    public Account account(@Argument String id) {
        log.info("EntityMapping: account id={}", id);
        return BY_ID.get(id);
    }
}
