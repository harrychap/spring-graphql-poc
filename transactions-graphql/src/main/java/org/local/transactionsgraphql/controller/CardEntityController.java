package org.local.transactionsgraphql.controller;

import org.local.transactionsgraphql.model.Card;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.graphql.data.federation.EntityMapping;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.stereotype.Controller;

@Controller
public class CardEntityController {

    private static final Logger log = LoggerFactory.getLogger(CardEntityController.class);

    @EntityMapping
    public Card card(@Argument String id) {
        log.info("EntityMapping: card id={}", id);
        return new Card(id);
    }
}
