package com.sitepark.ies.extensions.graphql.api;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import graphql.kickstart.tools.GraphQLResolver;
import graphql.kickstart.tools.SchemaParserBuilder;
import java.io.UncheckedIOException;
import org.dataloader.BatchLoaderWithContext;
import org.dataloader.DataLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GraphQLSchemaExtensionConfigurationTest {

  private SchemaParserBuilder schemaBuilder;

  private GraphQLSchemaExtensionConfiguration config;

  @BeforeEach
  void setup() {
    this.schemaBuilder = mock(SchemaParserBuilder.class);
    this.config = new GraphQLSchemaExtensionConfiguration(schemaBuilder);
  }

  @Test
  void testGetSchemaBuilder() {
    assertNotNull(this.config.getSchemaParserBuilder(), "schema-builder expected");
  }

  @Test
  void testDictionary() {
    this.config.dictionary("test", Object.class);
    verify(this.schemaBuilder).dictionary("test", Object.class);
  }

  @Test
  void testSchemaString() {
    this.config.schemaString("test");
    verify(this.schemaBuilder).schemaString("test");
  }

  @Test
  void testSchemaResource() {
    this.config.schemaResource(
        GraphQLSchemaExtensionConfigurationTest.class, "/testSchemaResource.txt");
    verify(this.schemaBuilder).schemaString("loaded");
  }

  @Test
  void testInvalidSchemaResource() {
    assertThrows(
        UncheckedIOException.class,
        () -> {
          this.config.schemaResource(GraphQLSchemaExtensionConfigurationTest.class, "/invalid.txt");
        });
  }

  @Test
  void testResolvers() {
    GraphQLResolver<?> resolver = mock(GraphQLResolver.class);
    this.config.resolvers(resolver);
    verify(this.schemaBuilder).resolvers(resolver);
  }

  @Test
  void testDataLoader() {
    DataLoader<?, ?> dataLoader = mock(DataLoader.class);
    this.config.dataLoader("test", dataLoader);

    assertNotNull(this.config.getDataLoaders().get("test"), "dataLoder not found");
  }

  @Test
  void testBatchLoader() {
    BatchLoaderWithContext<?, ?> batchLoader = mock(BatchLoaderWithContext.class);
    this.config.batchLoader("test", batchLoader);

    assertNotNull(this.config.getBatchLoaders().get("test"), "dataLoder not found");
  }
}
