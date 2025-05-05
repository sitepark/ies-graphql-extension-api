package com.sitepark.ies.extensions.graphql.api;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
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
import org.dataloader.DataLoader;

@SuppressFBWarnings(value = {"EI_EXPOSE_REP", "EI_EXPOSE_REP2"})
public class GraphQLSchemaExtensionConfiguration {

  private final SchemaParserBuilder schemaParserBuilder;

  private final Map<String, DataLoader<?, ?>> dataLoaders = new ConcurrentHashMap<>();

  private final Map<String, BatchLoaderWithContext<?, ?>> batchLoaders = new ConcurrentHashMap<>();

  public GraphQLSchemaExtensionConfiguration(SchemaParserBuilder schemaParserBuilder) {
    this.schemaParserBuilder = schemaParserBuilder;
  }

  public Map<String, DataLoader<?, ?>> getDataLoaders() {
    return Collections.unmodifiableMap(this.dataLoaders);
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

  public GraphQLSchemaExtensionConfiguration dataLoader(String key, DataLoader<?, ?> dataLoader) {
    Objects.requireNonNull(key, "key is null");
    Objects.requireNonNull(dataLoader, "dataLoader is null");
    this.dataLoaders.put(key, dataLoader);
    return this;
  }

  public GraphQLSchemaExtensionConfiguration batchLoader(
      String key, BatchLoaderWithContext<?, ?> batchLoader) {
    Objects.requireNonNull(key, "key is null");
    Objects.requireNonNull(batchLoader, "batchLoader is null");
    this.batchLoaders.put(key, batchLoader);
    return this;
  }

  public SchemaParserBuilder getSchemaParserBuilder() {
    return this.schemaParserBuilder;
  }

  private String readResource(Class<?> cls, String name) {
    try (InputStream in = cls.getResourceAsStream(name); ) {
      if (in == null) {
        throw new UncheckedIOException(new IOException("resource " + name + " not found"));
      }
      return new String(in.readAllBytes(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
