package org.local.cardsrest.controller;

import org.local.cardsrest.model.Card;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cards")
public class CardController {

    private static final List<Card> CARDS = List.of(
        new Card("card-1", "acc-1", "1234", "DEBIT",  "2027-08", "ACTIVE"),
        new Card("card-2", "acc-1", "5678", "CREDIT", "2026-11", "ACTIVE"),
        new Card("card-3", "acc-2", "9012", "DEBIT",  "2028-03", "ACTIVE"),
        new Card("card-4", "acc-3", "3456", "CREDIT", "2027-05", "BLOCKED"),
        new Card("card-5", "acc-3", "7890", "DEBIT",  "2025-12", "ACTIVE")
    );

    private static final Map<String, Card> BY_ID = CARDS.stream()
        .collect(Collectors.toMap(Card::id, c -> c));

    @GetMapping
    public List<Card> cards() {
        return CARDS;
    }

    @GetMapping("/{id}")
    public Card card(@PathVariable String id) {
        return BY_ID.get(id);
    }

    @GetMapping("/account/{accountId}")
    public List<Card> cardsByAccount(@PathVariable String accountId) {
        return CARDS.stream()
            .filter(c -> c.accountId().equals(accountId))
            .toList();
    }
}
