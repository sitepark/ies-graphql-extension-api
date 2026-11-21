package com.sitepark.ies.extensions.graphql.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import org.dataloader.BatchLoaderWithContext;
import org.junit.jupiter.api.Test;

class GraphQLSchemaExtensionConfigurationTest {

  private final GraphQLSchemaExtensionConfiguration config =
      new GraphQLSchemaExtensionConfiguration();

  @Test
  void testDictionary() {
    this.config.dictionary("test", Object.class);
    assertThat(this.config.getDictionary())
        .as("the class should be registered for the type")
        .containsEntry("test", Object.class);
  }

  @Test
  void testSchemaString() {
    this.config.schemaString("test");
    assertThat(this.config.getSchemas()).as("the schema should be added").containsExactly("test");
  }

  @Test
  void testSchemaResource() {
    this.config.schemaResource(
        GraphQLSchemaExtensionConfigurationTest.class, "/testSchemaResource.txt");
    assertThat(this.config.getSchemas())
        .as("the content of the resource should be added")
        .containsExactly("loaded");
  }

  @Test
  void testInvalidSchemaResource() {
    assertThrows(
        UncheckedIOException.class,
        () ->
            this.config.schemaResource(
                GraphQLSchemaExtensionConfigurationTest.class, "/invalid.txt"));
  }

  @Test
  void testControllers() {
    Object first = new Object();
    Object second = new Object();
    this.config.controllers(first).controllers(second);
    assertThat(this.config.getControllers())
        .as("the controllers should be kept in their order")
        .containsExactly(first, second);
  }

  @Test
  void testBatchLoader() {
    BatchLoaderWithContext<?, ?> batchLoader = mock(BatchLoaderWithContext.class);
    this.config.batchLoader("test", batchLoader);

    assertThat(this.config.getBatchLoaders())
        .as("the batch loader should be registered by its key")
        .containsEntry("test", batchLoader);
  }

  @Test
  void testReadResourceThrowsIOException() throws IOException {

    ResourceLoader resourceLoader = mock(ResourceLoader.class);
    InputStream inputStream = mock(InputStream.class);
    when(resourceLoader.getResourceAsStream(any(), any())).thenReturn(inputStream);
    when(inputStream.readAllBytes()).thenThrow(new IOException("test"));

    GraphQLSchemaExtensionConfiguration config =
        new GraphQLSchemaExtensionConfiguration(resourceLoader);

    assertThrows(
        UncheckedIOException.class, () -> config.schemaResource(this.getClass(), "/invalid.txt"));
  }
}
