# GraphQL Federation Standards

Standards for implementing Apollo Federation subgraphs in this project, grounded in the [Apollo Federation docs](https://www.apollographql.com/docs/graphos/schema-design/federated-schemas/entities) and the [Spring GraphQL federation docs](https://docs.spring.io/spring-graphql/reference/federation.html).

## Setup

Every federation subgraph requires a `FederationSchemaFactory` bean wired into `GraphQlSource.Builder`:

```java
@Configuration
public class FederationConfig {

    @Bean
    public FederationSchemaFactory federationSchemaFactory() {
        return new FederationSchemaFactory();
    }

    @Bean
    public GraphQlSourceBuilderCustomizer federationCustomizer(FederationSchemaFactory schemaFactory) {
        return builder -> builder.schemaFactory(schemaFactory::createGraphQLSchema);
    }
}
```

## The Three Entity Roles

There are three distinct roles a subgraph can play for a given entity type. Each has a different schema pattern and controller requirement.

---

### 1. Owning an Entity

The subgraph that defines the entity and is the source of truth for its core fields.

**Schema:**
```graphql
type Account @key(fields: "id") {
    id: ID!
    name: String!
    email: String!
    balance: Float!
}
```

**Controllers:** A `@QueryMapping` controller for top-level queries, and a separate `@EntityMapping` controller to handle `_entities` lookups from the router:

```java
@Controller
public class AccountController {
    @QueryMapping
    public List<Account> accounts() { ... }

    @QueryMapping
    public Account account(@Argument String id) { ... }
}

@Controller
public class AccountEntityController {
    @EntityMapping
    public Account account(@Argument String id) {
        return accountService.findById(id);
    }
}
```

---

### 2. Contributing Fields to an Entity

The subgraph adds fields to an entity it does not own. The router resolves those fields by sending `_entities` queries to this subgraph.

**Schema** (Federation v2 — no `@extends` or `@external` on the key needed):
```graphql
type Account @key(fields: "id") {
    id: ID!
    transactions: [Transaction!]!
}
```

**Controllers:** A stub `@EntityMapping` to handle the `_entities` lookup, and a `@SchemaMapping` to resolve each contributed field. Per the naming conventions, keep the entity mapping and field mappings in the same entity controller:

```java
@Controller
public class AccountEntityController {

    @EntityMapping
    public Account account(@Argument String id) {
        return new Account(id); // stub — only the key is needed
    }

    @SchemaMapping(typeName = "Account", field = "transactions")
    public List<Transaction> transactions(Account account) {
        return transactionService.findByAccountId(account.id());
    }
}
```

The stub `@EntityMapping` is necessary: without it the router cannot invoke this subgraph to resolve `Account.transactions`, even though the entity data itself lives elsewhere.

---

### 3. Referencing an Entity (no fields contributed)

The subgraph uses another subgraph's entity as a return type (e.g. `Transaction.card: Card`) but adds no fields to it.

**Schema:**
```graphql
type Card @key(fields: "id") {
    id: ID!
}
```

> **Note:** The Apollo spec recommends `@key(fields: "id", resolvable: false)` for this pattern, which signals the router that this subgraph does not resolve Card entities. However, `federation-graphql-java-support:6.1.0` does not support the `resolvable` argument and will fail to start if it is used. Use the plain `@key` stub above instead.

**Controller:** A stub `@EntityMapping` is required. `FederationSchemaFactory` validates that every `@key` type has a mapped entity resolver — even pure stubs:

```java
@Controller
public class CardEntityController {

    @EntityMapping
    public Card card(@Argument String id) {
        return new Card(id); // stub — router fetches full card from the owning subgraph
    }
}
```

**Field resolver:** A `@SchemaMapping` on the referencing type returns a stub with only the key populated. The router then uses that key to fetch the full entity from the owning subgraph:

```java
@SchemaMapping(typeName = "Transaction", field = "card")
public Card card(Transaction transaction) {
    return transaction.cardId() != null ? new Card(transaction.cardId()) : null;
}
```

---

## Controller Rules

- One entity controller per entity type — do not mix `@EntityMapping` with `@QueryMapping` in the same class.
- `@SchemaMapping` for fields contributed to an external entity belongs in that entity's controller (e.g. `Account.transactions` goes in `AccountEntityController`).
- `@SchemaMapping` for fields on an owned type that reference an external entity (e.g. `Transaction.card`) belongs in the owning type's entity controller.

## Schema Rules

- Use Federation v2 syntax: no `@extends` or `@external` on key fields.
- Do not use `resolvable: false` — not supported by `federation-graphql-java-support:6.1.0`.
- Every `@key` type must have a corresponding `@EntityMapping` controller.

## REST Subgraphs (Apollo Connector)

For REST APIs that are not Spring GraphQL apps, use `@connect` directives in the Apollo Router connector schema rather than a Spring subgraph. The connector schema references the REST base URL via `@source` and maps each field to an HTTP endpoint:

```graphql
extend schema
  @link(url: "https://specs.apollo.dev/federation/v2.10", import: ["@key"])
  @link(url: "https://specs.apollo.dev/connect/v0.2", import: ["@connect", "@source"])
  @source(name: "cards", http: { baseURL: "http://localhost:8083" })

type Card @key(fields: "id") {
  id: ID!
  lastFour: String!
}

type Account @key(fields: "id") {
  id: ID!
  cards: [Card!]!
    @connect(
      source: "cards"
      http: { GET: "/api/cards/account/{$this.id}" }
      selection: "id lastFour type expiry status"
    )
}
```

Entity resolution for connector-backed types is handled by the router — no `@EntityMapping` is needed.
