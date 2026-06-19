package org.local.transactionsgraphql.model;

public record Transaction(String id, String accountId, String cardId, double amount, String description, String date) {}
