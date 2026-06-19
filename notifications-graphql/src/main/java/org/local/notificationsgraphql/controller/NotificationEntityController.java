package org.local.notificationsgraphql.controller;

import org.local.notificationsgraphql.model.Account;
import org.local.notificationsgraphql.model.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.graphql.data.federation.EntityMapping;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class NotificationEntityController {

    private static final Logger log = LoggerFactory.getLogger(NotificationEntityController.class);

    private static final Map<String, Notification> BY_ID = NotificationController.NOTIFICATIONS.stream()
        .collect(Collectors.toMap(Notification::id, n -> n));

    @EntityMapping
    public Notification notification(@Argument String id) {
        log.info("EntityMapping: notification id={}", id);
        return BY_ID.get(id);
    }

    @SchemaMapping(typeName = "Notification", field = "account")
    public Account account(Notification notification) {
        log.info("SchemaMapping: Notification.account notificationId={}", notification.id());
        return new Account(notification.accountId());
    }
}
