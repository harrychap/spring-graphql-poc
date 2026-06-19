# Spring GraphQL POC

A proof-of-concept exploring GraphQL Federation with Spring Boot. Subgraph services and a REST API expose data that can be queried through either the Spring-based gateway or Apollo Router.

## Services

| Service | Port | Description |
|---|---|---|
| `accounts-graphql` | 8081 | Accounts subgraph — owns the `Account` entity |
| `transactions-graphql` | 8082 | Transactions subgraph — owns `Transaction`, extends `Account` with `transactions` |
| `cards-rest` | 8083 | Cards REST API — serves card data linked to accounts |
| `spring-router-graphql` | 8080 | Spring-based federation gateway — proxies GraphQL subgraphs and the cards REST API |
| `apollo-router` | 4000 | Apollo Router — production-grade federation router that composes the GraphQL subgraphs |

`Account.id` is the federation join key: the router uses it to navigate between accounts, transactions, and cards.

## Running

Start the subgraphs and REST API first. Both gateway options depend on them.

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

Then start whichever gateway you want to use:

**Apollo Router**
```bash
cd apollo-router
./start.sh
```

`start.sh` composes the supergraph from the subgraph SDL files and starts the router. The Apollo Router binary is downloaded automatically on first run.

**Spring gateway**
```bash
cd spring-router-graphql
./gradlew bootRun
```

## Interactive querying

**Apollo Router** serves Apollo Sandbox at http://localhost:4000

**Spring gateway** serves GraphiQL at http://localhost:8080/graphiql

The subgraphs also expose GraphiQL directly:
- Accounts: http://localhost:8081/graphiql
- Transactions: http://localhost:8082/graphiql

## Example Queries

**List all accounts** — works via Apollo Router and Spring gateway
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

**Account with its transactions (cross-service)** — works via Apollo Router and Spring gateway
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

**List all transactions** — works via Apollo Router and Spring gateway
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

**Transactions for a specific account** — works via Apollo Router and Spring gateway
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

**Transaction with its account (cross-service)** — Spring gateway only
```graphql
query {
  transaction(id: "txn-3") {
    amount
    description
    account {
      name
      email
      balance
    }
  }
}
```

> `Transaction.account` is only available through the Spring gateway. It is not declared in any subgraph schema so Apollo Router does not expose it.

**Cards for an account (cross-service)** — works via Apollo Router and Spring gateway
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

**List all cards** — works via Apollo Router and Spring gateway
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

**Card with its account** — Spring gateway only
```graphql
query {
  card(id: "card-1") {
    lastFour
    type
    account {
      name
      email
      balance
    }
  }
}
```

> `Card.account` is only available through the Spring gateway. It is not declared in the connector schema so Apollo Router does not expose it.

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
| `amount` | Float | Positive for credits, negative for debits |
| `description` | String | Human-readable description |
| `date` | String | Date in `YYYY-MM-DD` format |
| `account` | Account | Spring gateway only — resolved by the accounts subgraph via `_entities` |

### Card
| Field | Type | Description |
|---|---|---|
| `id` | ID | Unique card identifier (e.g. `card-1`) — federation `@key` |
| `accountId` | ID | References an Account `id` |
| `lastFour` | String | Last four digits of the card number |
| `type` | String | `DEBIT` or `CREDIT` |
| `expiry` | String | Expiry in `YYYY-MM` format |
| `status` | String | `ACTIVE` or `BLOCKED` |
| `account` | Account | Spring gateway only — resolved by the accounts subgraph via `_entities` |

## Requirements

- Java 25 — no additional setup for Spring services; dependencies are fetched by Gradle on first run
- Apollo Router: `rover` must be installed (`curl -sSL https://rover.apollo.dev/nix/latest | sh`); the router binary is downloaded automatically by `start.sh` on first run

## Federation Architecture

Each subgraph uses `federation-graphql-java-support` to expose `_service { sdl }` and `_entities` endpoints. The `@EntityMapping` annotation in each controller handles entity resolution when a router calls `_entities` with a list of typed representations (e.g. `{__typename: "Account", id: "acc-1"}`).

**Apollo Router** reads the subgraph SDL files at startup via `rover supergraph compose`, produces a composed supergraph schema, and handles all cross-subgraph query planning and entity resolution automatically. The `cards` subgraph is a **connector** — it has no GraphQL server; instead, `@connect` directives in `apollo-router/cards-connector.graphqls` map each field directly to `cards-rest` REST endpoints, and the router makes those HTTP calls at query time.

**Spring gateway** (`spring-router-graphql`) is a WebFlux app that manually routes queries to each subgraph via `HttpGraphQlClient` and to `cards-rest` via `WebClient`. Cross-service field resolution (e.g. `Account.cards`, `Card.account`) is wired explicitly in the gateway controllers.
