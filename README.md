GraphQL is a query language for API. Sie [here](https://graphql.org/) for details.

This extension provides an endpoint over which the requests are made. The extension is intended to be extended by other extensions.

# How to extend

The schema is extended via additional extensions. Details how to create an extension can be found [here](https://github.com/sitepark/ies-extension-api)

For the extension of the GraphQL schema the GraphQL extension provides an API.

Dependency to use the API.

```xml
<dependency>
	<groupId>com.sitepark.ies.extensions</groupId>
	<artifactId>ies-graphql-extension-api</artifactId>
	<version>1.0</version>
	<scope>provided</scope>
</dependency>
```

The API classes are located in the package `com.sitepark.ies.extensions.graphql.api`

To extend a schema, the interface `com.sitepark.ies.extensions.graphql.api.GraphQLSchemaExtension` must be implemented.

```java
package com.sitepark.ies.extensions.example.graphql;

import com.sitepark.ies.extensions.graphql.api.GraphQLSchemaExtension;
import com.sitepark.ies.extensions.graphql.api.GraphQLSchemaExtensionConfiguration;

public class ExampleGraphQLSchemaExtension implements GraphQLSchemaExtension {

	@Override
	public void initialize(GraphQLSchemaExtensionConfiguration config) {
		config
				.schemaResource(MyGraphQLSchemaExtension.class, "schema.graphqls")
				.resolvers(...),
				.batchLoader(...);
	}
}
```

The GraphQL extension automatically searches for all implementations of this interface and registers them independently.

The GraphQL Extension uses [GraphQL Java](https://www.graphql-java.com/) and [GraphQL Java Tools](https://www.graphql-java-kickstart.com/tools/) to provide the GraphQL endpoint.

## Defining a schema

First, the scheme is defined in a text file. More detailed information about how a GraphQL schema must look like can be found [here](https://graphql.org/learn/schema/).

The schema should be stored as a resource in the same path as the package of the extension. In the above example the package `com.sitepark.ies.extensions.example.graphql` is used. The schema file should then be placed in the directory `src/main/resources/com/sitepark/ies/extensions/example/graphql/`. The schema can be split into several files. If one file is sufficient the name `schema.graphqls` should be used.

`schema.graphqls`
```graphqls
# Example object to show how to define a type
type Example {
# Id of the example object
id: ID
# Name of the example object
name: String
}
```

Please use [GraphQL Descriptions](https://www.graphql-java-kickstart.com/tools/schema-definition/#graphql-descriptions) to comment the schema.

The GraphQL extension provides the type `Query`. This can be extended to add custom root queries to the schema.

`schema.graphqls`
```graphqls
# Query Root
extend type Query {
# Returns all example objects
allExamples: [Example]
}
```

The schema must now be passed via our `ExampleGraphQLSchemaExtension`.

```java
package com.sitepark.ies.extensions.example.graphql;

import com.sitepark.ies.extensions.graphql.api.GraphQLSchemaExtension;
import com.sitepark.ies.extensions.graphql.api.GraphQLSchemaExtensionConfiguration;

public class ExampleGraphQLSchemaExtension implements GraphQLSchemaExtension {

	@Override
	public void initialize(GraphQLSchemaExtensionConfiguration config) {
		config
				.schemaResource(ExampleGraphQLSchemaExtension.class, "schema.graphqls")
	}
}
```

The schema is loaded with relative resource path over its own class. See [Class.getResourceAsStream(String)](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/lang/Class.html#getResourceAsStream(java.lang.String)) for details.


Our schema consists of a query and a type. Both are represented by their own Java class.

Please use the builder pattern for the definition of the types

```java
package com.sitepark.ies.extensions.example.graphql;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

@JsonDeserialize(builder = Example.Builder.class)
public class Example {

	private final int id;

	private final String name;

	private Example(Builder builder) {
		this.id = builder.id;
		this.name = builder.name;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public static Builder builder() {
		return new Builder();
	}

	public Builder toBuilder() {
		return new Builder(this);
	}

	@Override
	public String toString() {
		return this.name + " (" + this.id + ")";
	}

	@JsonPOJOBuilder(withPrefix = "", buildMethodName = "build")
	public static class Builder {

		private int id;

		private String name;

		private Builder() { }

		private Builder(Example example) {
			this.id = example.id;
			this.name = example.name;
		}

		public Builder id(int id) {
			this.id = id;
			return this;
		}

		public Builder name(String name) {
			this.name = name;
			return this;
		}

		public Example build() {
			return new Example(this);
		}
	}
}
```

The `Query` class provides the `allExamples()` method.

```java
package com.sitepark.ies.extensions.example.graphql;

import java.util.List;

import com.sitepark.ies.extensions.graphql.api.annotations.UserSecured;

import graphql.kickstart.tools.GraphQLQueryResolver;

public class Query implements GraphQLQueryResolver {

	@UserSecured
	public Example[] allExamples() {
		return new Example[] = {
				Example.builder().id(1).name("First").build(),
				Example.builder().id(2).name("Second").build()
		};
	}
}
```

It is important to specify for the resolver method via an annotation whether the call should be protected.

For methods without a corresponding annotation, an error `missing secured annotation` is output.

The following annotations are possible:

- `@UserSecured` : indicates that this method can only be called for authenticated users.
- `@Unsecured` : indicates that this method can be called without a security check.

The interface `GraphQLQueryResolver` indicates that it is a root query and it is expected that the corresponding methods are defined in the type `Query` of the schema.

With implementations of `GraphQLQueryResolver<?>` methods of other types can be provided. See also [Use Resolver to extend type](#use-resolver-to-extend-type).

With implementations of `GraphQLMutationResolver` mutation methods can be provided. See also [Use Mutations](#use-mutations).

The query resolver must now still be registered via the `ExampleGraphQLSchemaExtension` class.

```java
package com.sitepark.ies.extensions.example.graphql;

import javax.inject.Inject;

import com.sitepark.ies.extensions.graphql.api.GraphQLSchemaExtension;
import com.sitepark.ies.extensions.graphql.api.GraphQLSchemaExtensionConfiguration;

public class ExampleGraphQLSchemaExtension implements GraphQLSchemaExtension {

	private final Query query;

	@Inject
	public ExampleGraphQLSchemaExtension(Query query) {
		this.query = query;
	}

	@Override
	public void initialize(GraphQLSchemaExtensionConfiguration config) {
		config
				.schemaResource(ExampleGraphQLSchemaExtension.class, "schema.graphqls")
				.resolvers(this.query)
	}
}
```



It is important that the Query object is created via the Dependency Injector. Only then can the security annotations be evaluated.

Thus the schema is defined and the resolver is registered and we can call the GraphQL endpoint. For this purpose, the IES provides the [GraphQL IDE](https://github.com/graphql/graphiql), which can be used to test the calls.

Available at https://example.domain.de/ies3/[client-anchor]/graphiql

The graphql endpoint is https://example.domain.de/ies3/[client-anchor]/graphql

The example query:
```graphql
{
allExamples {
	id
	name
}
}
```
The response:
```json
{
"data": {
	"allExamples": [
	{
		"id": 1,
		"name": "First"
	},
	{
		"id": 2,
		"name": "Second"
	}
	]
}
}
```

More details about queries sie [here](https://graphql.org/learn/queries/).

## Use Resolver to extend type

Resolvers can extend types. The resolver must implement the `GraphQLResolver<T>` interface, where `T` is the type whose fields are to be extended.

Suppose we want to extend our Example Type with the method `parentExample`.

`schema.graphqls`
```graphqls
extend type Example {
# Returns parent example
parentExample: Example
}
```

The resolver implements the `parentExample()` method.

```java
package com.sitepark.ies.extensions.example.graphql;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.dataloader.DataLoader;

import com.sitepark.ies.extensions.graphql.api.annotations.UserSecured;

import graphql.kickstart.tools.GraphQLResolver;
import graphql.schema.DataFetchingEnvironment;

public class ExampleResolver implements GraphQLResolver<Example> {

	@UserSecured
	public Example parentExample(Example example) {
		Example parent = null;
		// determine the parent
		return parent;
	}
}
```

The resolver must now still be registered via the `ExampleGraphQLSchemaExtension` class.

```java
package com.sitepark.ies.extensions.example.graphql;

import javax.inject.Inject;

import com.sitepark.ies.extensions.graphql.api.GraphQLSchemaExtension;
import com.sitepark.ies.extensions.graphql.api.GraphQLSchemaExtensionConfiguration;

public class ExampleGraphQLSchemaExtension implements GraphQLSchemaExtension {

	private final Query query;
	private final ExampleResolver exampleResolver;

	@Inject
	public ExampleGraphQLSchemaExtension(Query query, ExampleResolver exampleResolver) {
		this.query = query;
		this.exampleResolver = exampleResolver;
	}

	@Override
	public void initialize(GraphQLSchemaExtensionConfiguration config) {
		config
				.schemaResource(ExampleGraphQLSchemaExtension.class, "schema.graphqls")
				.resolvers(this.query)
				.resolvers(this.exampleResolver);
	}
}
```

## Use Dataloader

DataLoader is a generic utility to be used as part of your application's data fetching layer to provide a simplified and consistent API over various remote data sources such as databases or web services via batching and caching.

Dataloaders should be used to solve the [N+1 problem in GraphQL](https://unchained.shop/blog/n-plus-one-problem-in-graphql).

The GraphQL extension uses the [java-dataloader](https://github.com/graphql-java/java-dataloader/). Further information can be found here:
- [GraphQL Java Kickstart / Dataloaders](https://www.graphql-java-kickstart.com/servlet/dataloaders/)
- [GraphQL Java / Batching](https://www.graphql-java.com/documentation/batching)

The GraphQL extension takes care of the necessary stuff. So only the dataloader has to be defined.

Suppose we want to extend our Example Type with the method `childExamples`.

`schema.graphqls`
```graphqls
extend type Example {
# Returns child examples
childExamples: [Example]
}
```

Then we need a resolver that returns the list. This resolver is to use a dataloader.


Two DataLoader classes are available, which can be extended

`org.dataloader.BatchLoader`
`org.dataloader.BatchLoaderWithContext`

We use the `BatchLoaderWithContext`, because only this way the calls executed by the Batchloader can be executed in the `UseCaseScope`.

More details about `BatchLoaderWithContext` can be found [here](https://github.com/graphql-java/java-dataloader#calling-the-batch-loader-function-with-call-context-environment). More details about the UseCaseScope can be found [here](#use-case-scope).

We additionally implement the `DataLoaderBuilder` interface to be able to influence the creation of the DataLoader from the BachLoader. The interface expects an implementation of `DataLoader<?, ?> buildDataLoader(DataLoaderOptions options)`.

```java
package com.sitepark.ies.extensions.example.graphql;

import com.sitepark.ies.di.UseCaseScope;
import com.sitepark.ies.extensions.graphql.api.DataLoaderBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.dataloader.BatchLoaderEnvironment;
import org.dataloader.BatchLoaderWithContext;

public class ExampleBatchLoader implements
		BatchLoaderWithContext<Integer, Example>,
		DataLoaderBuilder {

	@Override
	public CompletionStage<List<Example>> load(List<Integer> keys,
			BatchLoaderEnvironment environment) {

		return UseCaseScope.transferSupplyAsync(environment.getContext(), () -> {
				List<Example> list = new ArrayList<>();
				// process example-id list
				return list;
		});
	}

	@Override
	public DataLoader<Long, Entity[]> buildDataLoader(DataLoaderOptions options) {
		options.setCacheMap(CacheMap.simpleMap());
		return DataLoaderFactory.newDataLoader(this, options);
	}
}
```

We define a batch loader. Internally, `DataLoaderFactory.newDataLoader()` then creates the DataLoader from this, which is then used by the resolver. If the `DataLoaderBuilder` interface is implemented, the `buildDataLoader()` method will be used.

The batch loader must now still be registered via the `ExampleGraphQLSchemaExtension` class.

```java
package com.sitepark.ies.extensions.example.graphql;

import javax.inject.Inject;

import com.sitepark.ies.extensions.graphql.api.GraphQLSchemaExtension;
import com.sitepark.ies.extensions.graphql.api.GraphQLSchemaExtensionConfiguration;

public class ExampleGraphQLSchemaExtension implements GraphQLSchemaExtension {

	private final Query query;
	private final ExampleBatchLoader exampleBatchLoader;

	public static final String KEY = "examples";

	@Inject
	public ExampleGraphQLSchemaExtension(Query query, ExampleBatchLoader exampleBatchLoader) {
		this.query = query;
		this.exampleBatchLoader = exampleBatchLoader;
	}

	@Override
	public void initialize(GraphQLSchemaExtensionConfiguration config) {
		config
				.schemaResource(ExampleGraphQLSchemaExtension.class, "schema.graphqls")
				.resolvers(this.query)
				.batchLoader(ExampleGraphQLSchemaExtension.KEY, this.exampleBatchLoader)
	}
}
```

For the `childExamples` now a resolver is needed which uses the DataLoader.

```java
package com.sitepark.ies.extensions.example.graphql;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.dataloader.DataLoader;

import com.sitepark.ies.extensions.graphql.api.annotations.UserSecured;

import graphql.kickstart.tools.GraphQLResolver;
import graphql.schema.DataFetchingEnvironment;

public class ExampleResolver implements GraphQLResolver<Example> {

	@UserSecured
	public CompletableFuture<Example> childExamples(Example example, DataFetchingEnvironment dfe)
			throws InterruptedException, ExecutionException {

		final DataLoader<Integer, Example> exampleDataloader =
				dfe.getDataLoaderRegistry().getDataLoader(ExampleGraphQLSchemaExtension.KEY);

		return exampleDataloader.load(example.getId());
	}
}
```

The resolver must now still be registered via the `ExampleGraphQLSchemaExtension` class.

```java
package com.sitepark.ies.extensions.example.graphql;

import javax.inject.Inject;

import com.sitepark.ies.extensions.graphql.api.GraphQLSchemaExtension;
import com.sitepark.ies.extensions.graphql.api.GraphQLSchemaExtensionConfiguration;

public class ExampleGraphQLSchemaExtension implements GraphQLSchemaExtension {

	private final Query query;
	private final ExampleBatchLoader exampleBatchLoader;
	private final ExampleResolver exampleResolver;

	@Inject
	public ExampleGraphQLSchemaExtension(
			Query query,
			ExampleBatchLoader exampleBatchLoader,
			ExampleResolver exampleResolver) {

		this.query = query;
		this.exampleBatchLoader = exampleBatchLoader;
		this.exampleResolver = exampleResolver;
	}

	@Override
	public void initialize(GraphQLSchemaExtensionConfiguration config) {
		config
				.schemaResource(ExampleGraphQLSchemaExtension.class, "schema.graphqls")
				.resolvers(this.query)
				.resolvers(this.exampleResolver)
				.batchLoader(ExampleGraphQLSchemaExtension.KEY, this.exampleBatchLoader)
	}
}
```

## Use Mutations

Mutations are used to modify server-side data. Mutations are also realized via resolver and implement the `GraphQLMutationResolver` interface.

Extend the schema

```graphqls
# Mutation Root
extend type Mutation {
createExample(example: Example): Example
}
```

```java
package com.sitepark.ies.extensions.example.graphql;

import com.sitepark.ies.extensions.graphql.api.annotations.UserSecured;

import graphql.kickstart.tools.GraphQLMutationResolver;

public class Mutation implements GraphQLMutationResolver {

	@UserSecured
	public Example createExample(Example example) {
		// use a repository
		Example storedExample = ...;
		return storedExample;
	}
}
```

The mutation resolver must now still be registered via the `ExampleGraphQLSchemaExtension` class.

```java
package com.sitepark.ies.extensions.example.graphql;

import javax.inject.Inject;

import com.sitepark.ies.extensions.graphql.api.GraphQLSchemaExtension;
import com.sitepark.ies.extensions.graphql.api.GraphQLSchemaExtensionConfiguration;

public class ExampleGraphQLSchemaExtension implements GraphQLSchemaExtension {

	private final Query query;
	private final Mutation mutation;

	@Inject
	public ExampleGraphQLSchemaExtension(Query query, Mutation mutation) {
		this.query = query;
		this.mutation = mutation;
	}

	@Override
	public void initialize(GraphQLSchemaExtensionConfiguration config) {
		config
				.schemaResource(ExampleGraphQLSchemaExtension.class, "schema.graphqls")
				.resolvers(this.query)
				.resolvers(this.mutation);
	}
}
```

## Use Subscriptions

Subscriptions are used to push updates from the server when data they are interested in changes. See [here](https://graphql.org/blog/subscriptions-in-graphql-and-relay/) for more details.

Subscriptions are also realized via resolver and implement the `GraphQLSubscriptionResolver` interface.

Extend the schema

```graphqls
# ExampleStatus returned by the subscription
type ProgressStatus {
	id: String
	message: String
}

# Subscription Root
extend type Subscription {
	exampleStatus: ExampleStatus
}
```

```java
import javax.inject.Inject;

import org.reactivestreams.Publisher;

import com.sitepark.ies.extensions.graphql.api.ProgressStatus;
import com.sitepark.ies.extensions.graphql.api.annotations.Unsecured;

import graphql.kickstart.tools.GraphQLSubscriptionResolver;
import graphql.schema.DataFetchingEnvironment;

public class Subscription implements GraphQLSubscriptionResolver {

	private final ExampleStatusPublisher exampleStatusPublisher;

	@Inject
	protected Subscription(ExampleStatusPublisher exampleStatusPublisher) {
		this.exampleStatusPublisher = exampleStatusPublisher;
	}

	@Unsecured
	public Publisher<ExampleStatus> exampleStatus(DataFetchingEnvironment env) {
		return exampleStatusPublisher;
	}
}
```

Subscription methods expect an `org.reactivestreams.Publisher` as return value. Details about this can be found [here](https://github.com/graphql-java/graphql-java-subscription-example).

Subscriptions are made via a WebSocket connection.
The endpoint for subscriptions is therefore not the same as for queries and mutations.
The graphql subscription endpoint is

wss://example.domain.de/[client-anchor]/api/graphql/subscriptions

Here is an example of how to set up a subscription via JavaScript.

```js
/*
	Websockets keep alive
	https://websockets.readthedocs.io/en/stable/topics/timeouts.html
	https://stackoverflow.com/questions/9056159/websocket-closing-connection-automatically
*/
function setupWebSocket() {
	var url = "wss://example.domain.de/[client-anchor]/api/graphql/subscriptions?ies-session=...";
	var exampleSocket = new WebSocket(url);

	exampleSocket.onopen = (event) => {
		var query = "subscription { exampleStatus { id message } }";
		log.debug("send", query);
		var json = JSON.stringify({
			query : query,
			variables : {}
		});
		exampleSocket.send(json);
	};

	exampleSocket.onmessage = (event) => {
		console.log(event.data);
	}

	exampleSocket.onclose = (event) => {
		console.log("close", event);
		setTimeout(setupWebSocket, 1000);
	}

	exampleSocket.onerror = (event) => {
		console.log("error", event);
	}
}
setupWebSocket();
//exampleSocket.close();
```

### Apache configuration

For the websocket connection via Apache to work, a rewrite rule must be set so that the web sockets requests go to the IES via the `ws` protocol.

```apache
# WebSocket Support
RewriteCond %{HTTP:Upgrade} =websocket [NC]
RewriteRule /(.*)           ws://${IES_IES_BALANCER_NAME}:8080/$1 [P,L]
```

### Test with status-subscription

A `status` subscription is provided which can be used to test the websocket connection.

```js
var url = "wss://example.domain.de/[client-anchor]/api/graphql/subscriptions?ies-session=...";
var exampleSocket = new WebSocket(url);

exampleSocket.onopen = (event) => {
	var query = "subscription { status }";
	var json = JSON.stringify({
		query : query,
		variables : {}
	});
	exampleSocket.send(json);
};

exampleSocket.onmessage = (event) => {
	let message = JSON.parse(event.data);
	console.log("Status is: " + message.data.status);
	exampleSocket.close();
}
```

## Use case scope

The IES provides functionalities in the form of UseCases. These can be integrated and called via dependency injection. These UseCases must be called in a Guice scope provided by IES (the `com.sitepark.ies.di.UseCaseScope`).

Here a hurdle arises when using GraphQL. GraphQl, especially with the use of DataLoader does not process a query in a single thread. But the `UseCaseScope` is bound to a thread. Therefore it is necessary to transfer the scope to the other used threads.

This small code example should clarify the functionality.

```java
UseCaseScoper useCaseScoper = UseCaseScope.transfer();

Thread thread = new Thread(){
	public void run() {
		try (
			UseCaseScoper.CloseableScope closable = useCaseScoper.open();
		) {
			// execute thread
		}
	}
}

thread.start();
```

UseCaseScope.transfer()` returns a transferable scope containing the state of the current thread. Inside the new thread, `useCaseScoper.open()` is called and the states are transferred to the current thread. With the help of the try-with-resources statement, the scope is removed from the thead after the function has been executed.

DataLoader expects a `CompletionStage` as return value. With the help of the `BatchLoaderEnvironment`, in which the `UseCaseScope` is stored, a `CompletionStage` can be created via the method `UseCaseScope.transferSupplyAsync()`, which transfers the `UseCaseScope` and also removes it from the thread after the execution of the method.

Use `UseCaseScope` in a `BatchLoaderWithContext`
```java
@Override
public CompletionStage<List<Examples>> load(List<Long> keys, BatchLoaderEnvironment environment) {

	// environment.getContext() returns the current UseCaseScope
	return UseCaseScope.transferSupplyAsync(environment.getContext(), () -> {
		/*
		When the method is executed, the scope is already
		transferred and is also removed again after execution.
		*/

		// Execute a method in which a UseCaseScope must be called.
	});
}
```

## Sign in a user

To do this, the HTTP header `X-IES-Session'` must be supplied with a valid session ID.

```
X-IES-Session: 844621...
```

To create a new session for a user the mutation `signinUser` must be used, which is provided by the IES GraphQL Extension.

```graphql
mutation signin {
signinUser(auth: {login: "peterpan", password: "mysecret", module: "test"}) {
	session {
	id
	user {
		id
		name
		login
		type
	}
	}
}
}
```

JSON is returned as the response:

```json
{
"data": {
	"signinUser": {
	"session": {
		"id": "8237653176368714756",
		"user": {
		"id": "100560100000001001",
		"name": "Peter Pan (peterpan)",
		"login": "peterpan",
		"type": "USER"
		}
	}
	}
}
}
```

The [GraphQL IDE](https://github.com/graphql/graphiql) which comes with the IES GraphQL Extension is adapted. It evaluates the session automatically. With it first a `signin` request can be made. The session is automatically stored in the `sessionStorage` of the browser and is used for all following requests. It is important that the mutation is named `signin`.

```graphql
mutation signin {
signinUser(...
```

And not

```graphql
mutation {
signinUser(...
```

## Extend types of other extensions

GraphQL Extensions can extend other GraphQL Extensions. This can be done as explained in section [Use Resolver to extend type](#use-resolver-to-extend-type).

For this it is necessary to use the extension to be extended as a dependency. For the example, this Maven dependency would have to be entered.

```xml
<dependency>
	<groupId>com.sitepark.ies.extensions</groupId>
	<artifactId>ies-example-extension</artifactId>
	<version>${ies.version}</version>
	<scope>provided</scope>
</dependency>
```

# Implementation details

The GraphQL Extension uses [GraphQL Java](https://www.graphql-java.com/) and [GraphQL Java Tools](https://www.graphql-java-kickstart.com/tools/) to provide the GraphQL endpoint.

## GraphQL Servlet

The starting point is the class `GraphQLEndpoint`. The article [GraphQL Java Kickstart / Getting started](https://www.graphql-java-kickstart.com/servlet/getting-started/) explains the basics.

The class `GraphQLConfigurationCache` holds the configuration of all extensions that extend the GraphQL Extension. In the future, it should also be possible to dynamically add or update extensions. In this case the `rebuild()` method shall rebuild the configuration.

## DataLoader support

An important point is the support of the DataLoader. Here it is important that the `AsyncExecutionStrategy` is used witch is activated by default. See also [GraphQL Java
/ Data Loader only works with AsyncExecutionStrategy](https://www.graphql-java.com/documentation/batching#data-loader-only-works-with-asyncexecutionstrategy)

## JSON Web Token support

The JSON Web Token support is included in the GraphQL Extension but not active. It is currently used to experiment with it.

To identify authenticated users, [JSON Web Token](https://jwt.io/). The IES GraphQL Extension uses the implementation [Java JWT](https://github.com/jwtk/jjwt).

For the signature of the tokens a private key is needed. This is managed by the class `JwtPrivatKeyManager`. To create a token and read token the `JwtService` is used. The token is read from the HTTP header using the ServletFilter `JwtAuthenticationFilter`.

## Resolver Security

Inspired by the [Secure your GraphQL API within a Spring-Boot App](https://medium.com/@philippechampion58/secure-your-graphql-api-within-a-spring-boot-app-72961fbe9232) article, accesses are also protected in the GraphQL Extension.

We will use [guice's APO support](https://github.com/google/guice/wiki/AOP) to make this happen.

The following classes are relevant:

- `MatchersForGraphQL` : We need our own matcher. The goal is to monitor the methods of all classes that implement the interfaces of the root resolver (`GraphQLQueryResolver`, `GraphQLMutationResolver`, `GraphQLSubscriptionResolver`). But then all methods that `java.lang.Object` provides will be monitored as well. To filter out these methods the own matcher is needed.
- `MethodSecureInterceptor` : The implementation of the `MethodInterceptor` from Guice. Here it is checked if the monitored method uses your secure annotation and if the conditions for the policy defined by the annotation are met.

The `MethodSecureInterceptor` needs the `Session` object of the authenticated user to check if the condition for `@UserSecured` is met. The session is kept in the `UseCaseScope` and set to the UseCaseScope via `com.sitepark.ies.extension.core.servlet.SessionFilter` if the `X-IES-Session` header is set and the session is valid.

There is a technical hurdle here that prevents the `Provider<Session>` from returning the `Session` object. The problem is that because of the necessary DataLoader support, the `AsyncExecutionStrategy` must be used. This ensures that the resolvers are not executed in the same thread as `com.sitepark.ies.extension.core.servlet.UseCaseScopeActivationFilter`. The data for the UseCaseScope of guice are bound to the thread and cannot be read out via another thread.

The solution here is to implement a `GraphQlAsyncTaskDecorator`. This is used to transfer the scope to other threads with the help of `UseCaseScope.transfer()`. See also [here](#use-case-scope)
