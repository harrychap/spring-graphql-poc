# Spring GraphQL POC

A proof-of-concept exploring GraphQL Federation with Spring Boot. Subgraph services and a REST API expose data that can be queried through either the Spring-based gateway or Apollo Router.

## Architecture

```
                         ┌──────────┐
                         │  Client  │
                         └────┬─────┘
                              │
                   ┌──────────▼──────────┐
                   │    Apollo Router    │  :4000
                   └──┬────┬──────┬──┬───┘
                      │    │      │  │
     ┌────────────────┘    │      │  └────────────────┐
     │                 ┌───┘      └───┐               │ @connect (HTTP)
     │                 │              │               │
     ▼                 ▼              ▼               ▼
┌──────────┐ ┌──────────────┐ ┌──────────────┐ ┌──────────────┐
│ accounts │ │transactions  │ │notifications │ │  cards-rest  │
│ graphql  │ │  graphql     │ │  graphql     │ │  (REST API)  │
│  :8081   │ │  :8082       │ │  :8084       │ │   :8083      │
└──────────┘ └──────────────┘ └──────────────┘ └──────────────┘
```

## Services

| Service | Port | Description |
|---|---|---|
| `accounts-graphql` | 8081 | Accounts subgraph — owns the `Account` entity |
| `transactions-graphql` | 8082 | Transactions subgraph — owns `Transaction`, extends `Account` with `transactions`, references `Card` entity |
| `cards-rest` | 8083 | Cards REST API — serves card data linked to accounts |
| `notifications-graphql` | 8084 | Notifications subgraph — owns `Notification`, contributes `notifications` to `Account`, exposes a `notificationAdded` subscription |
| `apollo-router` | 4000 | Apollo Router — federation router that composes the GraphQL subgraphs |

## Running

Start the subgraphs and REST API first.

**Accounts subgraph**
```bash
cd accounts-graphql
./gradlew bootRun
```

**Transactions subgraph**
```bash
cd transactions-graphql
./gradlew bootRun
```

**Cards REST API**
```bash
cd cards-rest
./gradlew bootRun
```

**Notifications subgraph**
```bash
cd notifications-graphql
./gradlew bootRun
```

**Apollo Router**
```bash
cd apollo-router
./start.sh
```

`start.sh` composes the supergraph from the subgraph SDL files and starts the router. The Apollo Router binary is downloaded automatically on first run.

> **GraphOS credentials required** — federated subscriptions require a GraphOS Developer plan or above. Set `APOLLO_KEY` and `APOLLO_GRAPH_REF` before running `start.sh`. See [GraphOS credentials](#graphos-credentials) below.

## Interactive querying

Apollo Router serves Apollo Sandbox at http://localhost:4000

The subgraphs also expose GraphiQL directly:
- Accounts: http://localhost:8081/graphiql
- Transactions: http://localhost:8082/graphiql
- Notifications: http://localhost:8084/graphiql

## Example Queries

**List all accounts**
```graphql
query {
  accounts {
    id
    name
    email
    balance
  }
}
```

**Account with its transactions (cross-service)**
```graphql
query {
  account(id: "acc-1") {
    name
    email
    balance
    transactions {
      id
      amount
      description
      date
    }
  }
}
```

**List all transactions**
```graphql
query {
  transactions {
    id
    accountId
    amount
    description
    date
  }
}
```

**Transactions for a specific account**
```graphql
query {
  transactionsByAccountId(accountId: "acc-1") {
    id
    amount
    description
    date
  }
}
```

**Transaction with its card (cross-service)**
```graphql
query {
  transaction(id: "txn-1") {
    id
    amount
    description
    cardId
    card {
      lastFour
      type
      status
    }
  }
}
```

**Cards for an account (cross-service)**
```graphql
query {
  account(id: "acc-1") {
    name
    cards {
      id
      lastFour
      type
      expiry
      status
    }
  }
}
```

**List all cards**
```graphql
query {
  cards {
    id
    accountId
    lastFour
    type
    expiry
    status
  }
}
```

**Notifications for an account (cross-service)**
```graphql
query {
  account(id: "acc-1") {
    name
    notifications {
      id
      message
      type
      timestamp
    }
  }
}
```

**Live notification stream (subscription)**

A random notification is pushed every 10 seconds. The `account` field is resolved by the accounts subgraph via entity lookup, so the full cross-subgraph chain is available in a single subscription.
```graphql
subscription {
  notificationAdded {
    id
    message
    type
    timestamp
    account {
      name
      email
      balance
      transactions {
        amount
        description
        card { lastFour }
      }
    }
  }
}
```

## Data Model

### Account
| Field | Type | Description |
|---|---|---|
| `id` | ID | Unique account identifier (e.g. `acc-1`) — federation `@key` |
| `name` | String | Account holder name |
| `email` | String | Account holder email |
| `balance` | Float | Current balance |
| `transactions` | [Transaction] | Resolved by the transactions subgraph via `_entities` |
| `cards` | [Card] | Fetched from `cards-rest` via the Apollo Connector (Apollo Router) or `WebClient` (Spring gateway) |

### Transaction
| Field | Type | Description |
|---|---|---|
| `id` | ID | Unique transaction identifier (e.g. `txn-1`) — federation `@key` |
| `accountId` | ID | References an Account `id` |
| `cardId` | ID | References a Card `id`; `null` for non-card transactions (e.g. salary deposits) |
| `card` | Card | Resolved by the cards subgraph via entity lookup on `cardId` |
| `amount` | Float | Positive for credits, negative for debits |
| `description` | String | Human-readable description |
| `date` | String | Date in `YYYY-MM-DD` format |
### Card
| Field | Type | Description |
|---|---|---|
| `id` | ID | Unique card identifier (e.g. `card-1`) — federation `@key` |
| `accountId` | ID | References an Account `id` |
| `lastFour` | String | Last four digits of the card number |
| `type` | String | `DEBIT` or `CREDIT` |
| `expiry` | String | Expiry in `YYYY-MM` format |
| `status` | String | `ACTIVE` or `BLOCKED` |

### Notification
| Field | Type | Description |
|---|---|---|
| `id` | ID | Unique notification identifier (e.g. `notif-1`) — federation `@key` |
| `accountId` | ID | References an Account `id` |
| `account` | Account | Resolved by the accounts subgraph via entity lookup on `accountId` |
| `message` | String | Human-readable notification text |
| `type` | NotificationType | `PAYMENT`, `ALERT`, or `INFO` |
| `timestamp` | String | ISO 8601 timestamp |

## GraphOS credentials

Federated subscriptions require Apollo Router to be connected to GraphOS (Developer plan or above).

`apollo-router/start.sh` reads `APOLLO_KEY` and `APOLLO_GRAPH_REF` from the environment, falling back to placeholder values if they are not set. Replace the placeholders by exporting the real values before running `./start.sh`, or set them in your shell profile.

```bash
export APOLLO_KEY="service:your-graph-name:xxxxxxxxxxxxxxxxxxxxxxxxxxxx"
export APOLLO_GRAPH_REF="your-graph-name@current"
```

**Getting the values from Apollo Studio:**

1. Open [studio.apollographql.com](https://studio.apollographql.com) and select your graph.
2. `APOLLO_GRAPH_REF` — shown at the top of the graph overview page in the format `<graph-id>@<variant>` (e.g. `my-graph@current`).
3. `APOLLO_KEY` — go to **Settings → This Graph → API Keys**, create a new **Graph API Key**, and copy the value (starts with `service:`).

## Requirements

- Java 25 — no additional setup for Spring services; dependencies are fetched by Gradle on first run
- Apollo Router: `rover` must be installed (`curl -sSL https://rover.apollo.dev/nix/latest | sh`); the router binary is downloaded automatically by `start.sh` on first run

## Federation Architecture

Each subgraph uses `federation-graphql-java-support` to expose `_service { sdl }` and `_entities` endpoints. The `@EntityMapping` annotation in each controller handles entity resolution when a router calls `_entities` with a list of typed representations (e.g. `{__typename: "Account", id: "acc-1"}`).

Apollo Router reads the subgraph SDL files at startup via `rover supergraph compose`, produces a composed supergraph schema, and handles all cross-subgraph query planning and entity resolution automatically. The `cards` subgraph is a **connector** — it has no GraphQL server; instead, `@connect` directives in `apollo-router/cards-connector.graphqls` map each field directly to `cards-rest` REST endpoints, and the router makes those HTTP calls at query time.
