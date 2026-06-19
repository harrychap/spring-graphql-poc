# Spring GraphQL POC

A proof-of-concept exploring GraphQL Federation with Spring Boot. Two independent subgraph services are linked via a federation gateway that composes their schemas into a unified supergraph.

## Services

| Service | Port | Description |
|---|---|---|
| `accounts-graphql` | 8081 | Accounts subgraph — owns the `Account` entity |
| `transactions-graphql` | 8082 | Transactions subgraph — owns `Transaction`, extends `Account` with `transactions` |
| `spring-router-graphql` | 8080 | Federation gateway — composes both subgraphs into a unified API |

`Account.id` is the federation join key: the gateway uses it to navigate from transactions back to account data and vice versa.

## Running

Start all three services in separate terminals. The subgraphs must be running before the gateway starts making requests.

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

**Gateway**
```bash
cd spring-router-graphql
./gradlew bootRun
```

## GraphiQL

All services expose GraphiQL for interactive querying:

- Gateway (unified): http://localhost:8080/graphiql
- Accounts subgraph: http://localhost:8081/graphiql
- Transactions subgraph: http://localhost:8082/graphiql

## Example Queries

Send these to the gateway at http://localhost:8080/graphiql.

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

**Transaction with its account (cross-service)**
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

## Data Model

### Account
| Field | Type | Description |
|---|---|---|
| `id` | ID | Unique account identifier (e.g. `acc-1`) — federation `@key` |
| `name` | String | Account holder name |
| `email` | String | Account holder email |
| `balance` | Float | Current balance |
| `transactions` | [Transaction] | Resolved by the transactions subgraph via `_entities` |

### Transaction
| Field | Type | Description |
|---|---|---|
| `id` | ID | Unique transaction identifier (e.g. `txn-1`) — federation `@key` |
| `accountId` | ID | References an Account `id` |
| `amount` | Float | Positive for credits, negative for debits |
| `description` | String | Human-readable description |
| `date` | String | Date in `YYYY-MM-DD` format |
| `account` | Account | Resolved by the accounts subgraph via `_entities` |

## Requirements

- Java 25
- No additional setup required — dependencies are fetched by Gradle on first run

## Federation Architecture

Each subgraph uses `federation-graphql-java-support` to expose `_service { sdl }` and `_entities` endpoints. The `@EntityMapping` annotation in each controller handles entity resolution when the gateway calls `_entities` with a list of typed representations (e.g. `{__typename: "Account", id: "acc-1"}`).

The gateway (`spring-router-graphql`) is a WebFlux app that:
1. Routes top-level queries to the appropriate subgraph via `HttpGraphQlClient`
2. Resolves `Account.transactions` by calling `_entities` on the transactions subgraph
3. Resolves `Transaction.account` by calling `_entities` on the accounts subgraph
