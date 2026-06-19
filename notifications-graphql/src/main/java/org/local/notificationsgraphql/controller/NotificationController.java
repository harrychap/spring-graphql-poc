package org.local.notificationsgraphql.controller;

import org.local.notificationsgraphql.model.Notification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SubscriptionMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

@Controller
public class NotificationController {

    private static final Logger log = LoggerFactory.getLogger(NotificationController.class);

    static final List<Notification> NOTIFICATIONS = List.of(
        new Notification("notif-1", "acc-1", "Payment of $49.99 at Grocery store", "PAYMENT", "2025-06-01T10:00:00Z"),
        new Notification("notif-2", "acc-1", "Low balance alert: $100 remaining",   "ALERT",   "2025-06-03T14:30:00Z"),
        new Notification("notif-3", "acc-2", "Salary deposit of $1500 received",    "PAYMENT", "2025-06-01T09:00:00Z"),
        new Notification("notif-4", "acc-2", "Rent payment of $200 processed",      "PAYMENT", "2025-06-05T11:00:00Z"),
        new Notification("notif-5", "acc-3", "Online shopping purchase: $89.00",    "PAYMENT", "2025-06-02T16:00:00Z"),
        new Notification("notif-6", "acc-3", "Freelance income received: $300.00",  "INFO",    "2025-06-04T12:00:00Z")
    );

    private static final Map<String, List<Notification>> BY_ACCOUNT_ID = NOTIFICATIONS.stream()
        .collect(Collectors.groupingBy(Notification::accountId));

    private static final Random RANDOM = new Random();

    @QueryMapping
    public List<Notification> notifications() {
        log.info("Query: notifications count={}", NOTIFICATIONS.size());
        return NOTIFICATIONS;
    }

    @QueryMapping
    public List<Notification> notificationsByAccountId(@Argument String accountId) {
        log.info("Query: notificationsByAccountId accountId={}", accountId);
        return BY_ACCOUNT_ID.getOrDefault(accountId, List.of());
    }

    @SubscriptionMapping
    public Flux<Notification> notificationAdded() {
        return Flux.interval(Duration.ofSeconds(10))
            .map(i -> {
                Notification notification = NOTIFICATIONS.get(RANDOM.nextInt(NOTIFICATIONS.size()));
                log.info("Subscription: notificationAdded sending id={} accountId={}", notification.id(), notification.accountId());
                return notification;
            });
    }
}
