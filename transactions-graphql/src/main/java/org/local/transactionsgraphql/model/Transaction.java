package org.local.transactionsgraphql.model;

public record Transaction(String id, String accountId, double amount, String description, String date) {}
