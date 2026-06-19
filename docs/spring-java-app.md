# Spring Java Application Standards

## Tech Stack

- **Spring Boot**: `4.1.0` (via `org.springframework.boot` plugin)
- **Java**: `25` (set via Gradle toolchain: `JavaLanguageVersion.of(25)`)
- **Build tool**: Gradle with Kotlin DSL (`build.gradle.kts`)
- **GraphQL federation**: `com.apollographql.federation:federation-graphql-java-support:6.1.0`

## Folder Structure

Source lives under `src/main/java/<group-package>/<module-name>/`. Organise by role:

```
src/main/java/org/local/<module>/
├── model/          # Data classes (records or POJOs)
├── controller/     # @Controller classes (GraphQL query/mutation handlers)
├── config/         # @Configuration classes (beans, federation setup)
└── <App>.java      # @SpringBootApplication entry point
```

GraphQL schemas go in `src/main/resources/graphql/schema.graphqls`.

Sub-packages are always required. The `<App>.java` entry point stays at the top-level package.

## Data Classes

Use Java `record`s for immutable data transfer objects:

```java
public record Account(String id, String name, String email, double balance) {}
```

Only introduce a full class when mutability or inheritance is genuinely required.

## Controllers

Annotate GraphQL controllers with `@Controller` (not `@RestController`). Use Spring GraphQL annotations:

- `@QueryMapping` — top-level query fields
- `@MutationMapping` — mutations
- `@Argument` — bind a named GraphQL argument to a method parameter
- `@EntityMapping` — resolve federated entity references (see federation section)

```java
@Controller
public class AccountController {

    @QueryMapping
    public List<Account> accounts() { ... }

    @QueryMapping
    public Account account(@Argument String id) { ... }
}
```

Keep controllers thin: they resolve GraphQL fields and delegate to a service or repository for any non-trivial logic.

## GraphQL Federation

When a subgraph participates in Apollo Federation:

1. **Dependency** — `com.apollographql.federation:federation-graphql-java-support:6.1.0` must be on the classpath.

2. **Configuration bean** — register `FederationSchemaFactory` and wire it into the `GraphQlSourceBuilderCustomizer`:

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

3. **Schema** — mark entity types with `@key` in the `.graphqls` file:

```graphql
type Account @key(fields: "id") {
    id: ID!
    name: String!
}
```

4. **Entity resolver** — add a dedicated `@Controller` with `@EntityMapping` to resolve entities by key:

```java
@Controller
public class AccountEntityController {

    @EntityMapping
    public Account account(@Argument String id) { ... }
}
```

One entity controller per type; do not mix entity resolution with query mappings in the same class.

## Configuration

Place all `@Bean` definitions in `@Configuration` classes under `config/`. Do not scatter bean definitions inside controllers or the application entry point.

## Design Patterns

| Pattern | Where to apply |
|---|---|
| **Repository / data-access object** | Wrap data access (DB, HTTP client) behind an interface; inject into controllers |
| **Factory** | Use when object construction involves conditional logic (e.g. `FederationSchemaFactory`) |
| **Strategy** | Swap business rules at runtime without changing callers |
| **Builder** | Construct complex objects step-by-step rather than telescoping constructors |

Prefer composition and dependency injection over inheritance. Keep classes small and focused on a single responsibility.

## Naming Conventions

| Artifact | Convention | Example |
|---|---|---|
| Record / model class | `PascalCase` noun | `Account`, `Transaction` |
| Query controller | `<Entity>Controller` | `AccountController` |
| Entity (federation) controller | `<Entity>EntityController` | `AccountEntityController` |
| Config class | `<Concern>Config` | `FederationConfig`, `GatewayConfig` |
| GraphQL schema file | `schema.graphqls` | `src/main/resources/graphql/schema.graphqls` |

## Dependencies (standard set)

```kotlin
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-graphql")
    implementation("org.springframework.boot:spring-boot-starter-web")
    // add only for federation subgraphs:
    implementation("com.apollographql.federation:federation-graphql-java-support:6.1.0")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.graphql:spring-graphql-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
```
