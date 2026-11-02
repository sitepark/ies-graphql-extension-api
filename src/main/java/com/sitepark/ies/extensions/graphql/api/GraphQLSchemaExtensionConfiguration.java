package com.sitepark.ies.extensions.graphql.api;

import graphql.kickstart.tools.GraphQLResolver;
import graphql.kickstart.tools.SchemaParserBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import org.dataloader.BatchLoaderWithContext;

/**
 * What an extension contributes to the GraphQL schema of the IES: schema files, resolvers, the
 * classes of union and interface types, and batch loaders.
 *
 * <p>The schema is built with GraphQL Java Tools and executed by Spring for GraphQL whenever the
 * extensions are loaded. A batch loader is the batch function of a data loader that the IES
 * creates for every request; if it implements {@link DataLoaderBuilder}, it creates the data loader
 * itself, e.g. with its own cache.
 */
public class GraphQLSchemaExtensionConfiguration {

  private final SchemaParserBuilder schemaParserBuilder;

  private final Map<String, BatchLoaderWithContext<?, ?>> batchLoaders = new ConcurrentHashMap<>();

  private final ResourceLoader resourceLoader;

  public GraphQLSchemaExtensionConfiguration(SchemaParserBuilder schemaParserBuilder) {
    this(schemaParserBuilder, new ResourceLoader());
  }

  public GraphQLSchemaExtensionConfiguration(
      SchemaParserBuilder schemaParserBuilder, ResourceLoader resourceLoader) {
    this.schemaParserBuilder = schemaParserBuilder;
    this.resourceLoader = resourceLoader;
  }

  public Map<String, BatchLoaderWithContext<?, ?>> getBatchLoaders() {
    return Collections.unmodifiableMap(this.batchLoaders);
  }

  public GraphQLSchemaExtensionConfiguration dictionary(String name, Class<?> clazz) {
    Objects.requireNonNull(name, "name is null");
    Objects.requireNonNull(clazz, "clazz is null");
    this.schemaParserBuilder.dictionary(name, clazz);
    return this;
  }

  public GraphQLSchemaExtensionConfiguration schemaString(String schemaString) {
    Objects.requireNonNull(schemaString, "schemaString is null");
    this.schemaParserBuilder.schemaString(schemaString);
    return this;
  }

  public GraphQLSchemaExtensionConfiguration schemaResource(Class<?> cls, String name) {
    Objects.requireNonNull(cls, "cls is null");
    Objects.requireNonNull(name, "name is null");
    String schemaString = this.readResource(cls, name);
    this.schemaString(schemaString);
    return this;
  }

  public GraphQLSchemaExtensionConfiguration resolvers(GraphQLResolver<?>... resolvers) {
    Objects.requireNonNull(resolvers, "resolvers is null");
    for (GraphQLResolver<?> resolver : resolvers) {
      Objects.requireNonNull(resolver, "resolver in resolvers is null");
    }
    this.schemaParserBuilder.resolvers(resolvers);
    return this;
  }

  /**
   * Registers the batch function of a data loader, available per request under the key ({@code
   * environment.getDataLoader(key)}).
   */
  public GraphQLSchemaExtensionConfiguration batchLoader(
      String key, BatchLoaderWithContext<?, ?> batchLoader) {
    Objects.requireNonNull(key, "key is null");
    Objects.requireNonNull(batchLoader, "batchLoader is null");
    this.batchLoaders.put(key, batchLoader);
    return this;
  }

  private String readResource(Class<?> cls, String name) {
    try (InputStream in = this.resourceLoader.getResourceAsStream(cls, name)) {
      if (in == null) {
        throw new UncheckedIOException(new IOException("resource " + name + " not found"));
      }
      return new String(in.readAllBytes(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
