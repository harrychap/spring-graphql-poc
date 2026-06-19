package org.local.notificationsgraphql.controller;

import org.local.notificationsgraphql.model.Account;
import org.local.notificationsgraphql.model.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.graphql.data.federation.EntityMapping;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class AccountEntityController {

    private static final Logger log = LoggerFactory.getLogger(AccountEntityController.class);

    private static final Map<String, List<Notification>> BY_ACCOUNT_ID =
        NotificationController.NOTIFICATIONS.stream()
            .collect(Collectors.groupingBy(Notification::accountId));

    @EntityMapping
    public Account account(@Argument String id) {
        log.info("EntityMapping: account id={}", id);
        return new Account(id);
    }

    @SchemaMapping(typeName = "Account", field = "notifications")
    public List<Notification> notifications(Account account) {
        log.info("SchemaMapping: Account.notifications accountId={}", account.id());
        return BY_ACCOUNT_ID.getOrDefault(account.id(), List.of());
    }
}
