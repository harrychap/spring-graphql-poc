package org.local.springroutergraphql.model;

public record Transaction(String id, String accountId, double amount, String description, String date) {}
