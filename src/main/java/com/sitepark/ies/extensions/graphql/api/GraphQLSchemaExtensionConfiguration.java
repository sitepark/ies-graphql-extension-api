package com.sitepark.ies.extensions.graphql.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import org.dataloader.BatchLoaderWithContext;

/**
 * What an extension contributes to the GraphQL schema of the IES: schema files, controllers, the
 * classes of union and interface types, and batch loaders.
 *
 * <p>The IES builds the schema with Spring for GraphQL whenever the extensions are loaded. The
 * controllers are Spring for GraphQL controllers: classes annotated with {@code
 * org.springframework.stereotype.Controller} whose methods are mapped with {@code @QueryMapping},
 * {@code @MutationMapping}, {@code @SubscriptionMapping}, {@code @SchemaMapping} or {@code
 * @BatchMapping}. A batch loader is the batch function of a data loader that the IES creates for
 * every request; if it implements {@link DataLoaderBuilder}, it creates the data loader itself,
 * e.g. with its own cache.
 */
public class GraphQLSchemaExtensionConfiguration {

  private final List<String> schemas = new ArrayList<>();

  private final List<Object> controllerList = new ArrayList<>();

  private final Map<String, Class<?>> typeClasses = new ConcurrentHashMap<>();

  private final Map<String, BatchLoaderWithContext<?, ?>> batchLoaders = new ConcurrentHashMap<>();

  private final ResourceLoader resourceLoader;

  public GraphQLSchemaExtensionConfiguration() {
    this(new ResourceLoader());
  }

  public GraphQLSchemaExtensionConfiguration(ResourceLoader resourceLoader) {
    this.resourceLoader = Objects.requireNonNull(resourceLoader, "resourceLoader is null");
  }

  /** The schema definitions, in the order they were added. */
  public List<String> getSchemas() {
    return Collections.unmodifiableList(this.schemas);
  }

  /** The controllers, in the order they were added. */
  public List<Object> getControllers() {
    return Collections.unmodifiableList(this.controllerList);
  }

  /** The classes of object types of unions and interfaces, by the name of the GraphQL type. */
  public Map<String, Class<?>> getDictionary() {
    return Collections.unmodifiableMap(this.typeClasses);
  }

  public Map<String, BatchLoaderWithContext<?, ?>> getBatchLoaders() {
    return Collections.unmodifiableMap(this.batchLoaders);
  }

  /**
   * Names the GraphQL object type of a class whose simple name differs from the name of the type,
   * e.g. {@code FullAccessPermission} for the class {@code FullAccess}. A class whose simple name is
   * the name of its type needs no entry.
   */
  public GraphQLSchemaExtensionConfiguration dictionary(String name, Class<?> clazz) {
    Objects.requireNonNull(name, "name is null");
    Objects.requireNonNull(clazz, "clazz is null");
    this.typeClasses.put(name, clazz);
    return this;
  }

  public GraphQLSchemaExtensionConfiguration schemaString(String schemaString) {
    Objects.requireNonNull(schemaString, "schemaString is null");
    this.schemas.add(schemaString);
    return this;
  }

  public GraphQLSchemaExtensionConfiguration schemaResource(Class<?> cls, String name) {
    Objects.requireNonNull(cls, "cls is null");
    Objects.requireNonNull(name, "name is null");
    return this.schemaString(this.readResource(cls, name));
  }

  /** Adds Spring for GraphQL controllers (see the description of the class). */
  public GraphQLSchemaExtensionConfiguration controllers(Object... controllers) {
    Objects.requireNonNull(controllers, "controllers is null");
    for (Object controller : controllers) {
      Objects.requireNonNull(controller, "controller in controllers is null");
    }
    this.controllerList.addAll(Arrays.asList(controllers));
    return this;
  }

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
