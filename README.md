# Spring GraphQL POC

A proof-of-concept exploring GraphQL with Spring Boot, structured as two independent services that share a common entity key — laying the groundwork for GraphQL Federation.

## Services

| Service | Port | Description |
|---|---|---|
| `accounts-graphql` | 8081 | Manages account data |
| `transactions-graphql` | 8082 | Manages transaction data |

Transactions reference accounts via a shared `accountId` field, which will serve as the federation join key.

## Running

Each service is a standalone Spring Boot app. Run them independently in separate terminals.

**Accounts service**
```bash
cd accounts-graphql
./gradlew bootRun
```

**Transactions service**
```bash
cd transactions-graphql
./gradlew bootRun
```

## GraphiQL

Both services expose GraphiQL for interactive querying:

- Accounts: http://localhost:8081/graphiql
- Transactions: http://localhost:8082/graphiql

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
| `id` | ID | Unique account identifier (e.g. `acc-1`) |
| `name` | String | Account holder name |
| `email` | String | Account holder email |
| `balance` | Float | Current balance |

### Transaction
| Field | Type | Description |
|---|---|---|
| `id` | ID | Unique transaction identifier (e.g. `txn-1`) |
| `accountId` | ID | References an Account `id` — federation join key |
| `amount` | Float | Positive for credits, negative for debits |
| `description` | String | Human-readable description |
| `date` | String | Date in `YYYY-MM-DD` format |

## Requirements

- Java 25
- No additional setup required — dependencies are fetched by Gradle on first run

## Next Steps

- GraphQL Federation: use `accountId` as the join key to stitch accounts and transactions into a unified supergraph
