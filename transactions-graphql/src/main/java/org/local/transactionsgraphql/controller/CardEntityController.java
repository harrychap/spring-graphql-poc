package org.local.transactionsgraphql.controller;

import org.local.transactionsgraphql.model.Card;
import org.springframework.graphql.data.federation.EntityMapping;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.stereotype.Controller;

@Controller
public class CardEntityController {

    @EntityMapping
    public Card card(@Argument String id) {
        return new Card(id);
    }
}
