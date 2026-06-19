package org.local.transactionsgraphql.controller;

import org.local.transactionsgraphql.model.Account;
import org.springframework.graphql.data.federation.EntityMapping;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.stereotype.Controller;

@Controller
public class AccountEntityController {

    @EntityMapping
    public Account account(@Argument String id) {
        return new Account(id);
    }
}
