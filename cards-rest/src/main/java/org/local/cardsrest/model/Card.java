package org.local.cardsrest.model;

public record Card(String id, String accountId, String lastFour, String type, String expiry, String status) {}
