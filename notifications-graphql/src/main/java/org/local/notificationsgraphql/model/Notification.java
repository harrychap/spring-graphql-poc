package org.local.notificationsgraphql.model;

public record Notification(String id, String accountId, String message, String type, String timestamp) {}
