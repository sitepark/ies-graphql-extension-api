[![Known Vulnerabilities](https://snyk.io/test/github/sitepark/ies-4-graphql-extension-api/badge.svg)](https://snyk.io/test/github/sitepark/ies-4-graphql-extension-api)

# IES GraphQL Extension API

The GraphQL extension of the IES serves the GraphQL API of the IES. Other extensions extend its
schema through this API.

The IES builds the schema with [Spring for GraphQL](https://spring.io/projects/spring-graphql) on
[GraphQL Java](https://www.graphql-java.com/) 25 whenever the extensions are loaded.

```xml
<dependency>
	<groupId>com.sitepark.ies.extensions</groupId>
	<artifactId>ies-graphql-extension-api</artifactId>
	<version>2.0.0</version>
	<scope>provided</scope>
</dependency>
```

## Extending the schema

An extension implements `GraphQLSchemaExtension`. The GraphQL extension finds all implementations
and calls them with a `GraphQLSchemaExtensionConfiguration`:

```java
public class ExampleGraphQLSchemaExtension implements GraphQLSchemaExtension {

  private final ExampleController controller;

  private final ExampleBatchLoader batchLoader;

  @Inject
  public ExampleGraphQLSchemaExtension(
      ExampleController controller, ExampleBatchLoader batchLoader) {
    this.controller = controller;
    this.batchLoader = batchLoader;
  }

  @Override
  public void initialize(GraphQLSchemaExtensionConfiguration config) {
    config
        .schemaResource(ExampleGraphQLSchemaExtension.class, "/graphql/example.graphqls")
        .controllers(this.controller)
        .batchLoader(ExampleBatchLoader.KEY, this.batchLoader);
  }
}
```

The controllers must be created by the dependency injection of the IES (constructor injection),
so that their security annotations are evaluated.

| Method | Contribution |
|---|---|
| `schemaResource(Class, String)`, `schemaString(String)` | schema definitions; extensions extend the root types with `extend type Query`, `extend type Mutation` and `extend type Subscription` |
| `controllers(Object...)` | Spring for GraphQL controllers |
| `batchLoader(String, BatchLoaderWithContext)` | the batch function of a data loader, created for every request |
| `dictionary(String, Class)` | the class of an object type of a union or an interface whose simple name differs from the name of the type |

## Controllers

A controller is a class annotated with `org.springframework.stereotype.Controller`. Its methods are
mapped to the fields of the schema with the annotations of Spring for GraphQL:

```graphqls
type Example {
  id: ID!
  name: String!
  parent: Example
}

type ExampleQuery {
  getAllExamples: [Example!]!
}

extend type Query {
  example: ExampleQuery
}
```

```java
@Controller
public class ExampleController {

  /** Namespace: the fields of ExampleQuery are mapped below. */
  @QueryMapping
  @Unauthenticated
  public ExampleQuery example() {
    return new ExampleQuery();
  }

  @SchemaMapping(typeName = "ExampleQuery")
  @Authenticated
  public List<Example> getAllExamples() {
    return List.of();
  }

  /** A field of the type Example, resolved with a data loader. */
  @SchemaMapping(typeName = "Example", field = "parent")
  @Authenticated
  public CompletableFuture<Example> parent(Example example, DataFetchingEnvironment env) {
    DataLoader<String, Example> loader = env.getDataLoader(ExampleBatchLoader.KEY);
    return loader.load(example.parentId());
  }
}
```

- Arguments are bound with `@Argument`, input types to records or classes with a constructor or
  setters.
- Fields of the result objects without a mapping are read from their properties (getters or record
  components).
- Union and interface types are resolved by the simple name of the class, or by the name given with
  `dictionary(String, Class)`.

Every public method of a controller must declare whether it requires an authentication:
`@Authenticated` or `@Unauthenticated` of the IES security API
(`com.sitepark.ies.sharedkernel.security.annotation`). The IES rejects a call of a method with
`@Authenticated` without an authentication.

## Data loaders

A data loader solves the N+1 problem of GraphQL. The extension registers its batch function with
`batchLoader(String, BatchLoaderWithContext)`; the IES creates a data loader for every request. The
context of the batch loader environment is the execution scope of the request, so that the batch
function runs in it:

```java
public class ExampleBatchLoader
    implements BatchLoaderWithContext<String, Example>, DataLoaderBuilder {

  public static final String KEY = "example.examples";

  @Override
  public CompletionStage<List<Example>> load(List<String> keys, BatchLoaderEnvironment environment) {
    return ExecutionScope.transferSupplyAsync(environment.getContext(), () -> load(keys));
  }

  @Override
  public DataLoader<String, Example> buildDataLoader(DataLoaderOptions options) {
    return DataLoaderFactory.newDataLoader(this, options);
  }
}
```

A batch loader that implements `DataLoaderBuilder` creates its data loader itself, e.g. with its own
cache.

## Subscriptions

Subscriptions are mapped with `@SubscriptionMapping` and return a `org.reactivestreams.Publisher`
(e.g. a Reactor `Flux`). They are served via WebSocket at `/api/graphql/subscriptions` with the
protocol `graphql-transport-ws`; the payload of `connection_init` names the session. The
subscription `status` can be used to test the connection.

## Migrating from 1.x

Version 2.0 is the API of IES 4, where Spring for GraphQL builds and serves the GraphQL schema.

- GraphQL Java 25 and java-dataloader 6 are required. GraphQL Java Tools is no longer used.
- `resolvers(GraphQLResolver...)` has been replaced by `controllers(Object...)`: the resolvers become
  Spring for GraphQL controllers (`GraphQLQueryResolver` → `@QueryMapping`, `GraphQLMutationResolver`
  → `@MutationMapping`, `GraphQLSubscriptionResolver` → `@SubscriptionMapping`, `GraphQLResolver<T>`
  → `@SchemaMapping(typeName = "T")`).
- `getSchemaParserBuilder()` has been removed; `GraphQLSchemaExtensionConfiguration` only collects
  the contributions, the IES evaluates them.
- `dataLoader(String, DataLoader)` and `getDataLoaders()` have been removed: a data loader must be
  created for every request. Register its batch function with `batchLoader(String,
  BatchLoaderWithContext)`.
