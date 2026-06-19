package org.local.transactionsgraphql.controller;

import org.local.transactionsgraphql.model.Account;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.graphql.data.federation.EntityMapping;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.stereotype.Controller;

@Controller
public class AccountEntityController {

    private static final Logger log = LoggerFactory.getLogger(AccountEntityController.class);

    @EntityMapping
    public Account account(@Argument String id) {
        log.info("EntityMapping: account id={}", id);
        return new Account(id);
    }
}
