package com.sitepark.ies.extensions.graphql.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.dataloader.BatchLoaderWithContext;
import org.dataloader.DataLoader;

import graphql.kickstart.tools.GraphQLResolver;
import graphql.kickstart.tools.SchemaParserBuilder;

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
		assert name != null : "name is null";
		assert clazz != null : "clazz is null";
		this.schemaParserBuilder.dictionary(name, clazz);
		return this;
	}

	public GraphQLSchemaExtensionConfiguration schemaString(String schemaString) {
		assert schemaString != null : "schemaString is null";
		this.schemaParserBuilder.schemaString(schemaString);
		return this;
	}

	public GraphQLSchemaExtensionConfiguration schemaResource(Class<?> cls, String name) {
		assert cls != null : "cls is null";
		assert name != null : "name is null";
		String schemaString = this.readResource(cls, name);
		this.schemaString(schemaString);
		return this;
	}

	public GraphQLSchemaExtensionConfiguration resolvers(GraphQLResolver<?>... resolvers) {
		assert resolvers != null : "resolver array is null";
		for (GraphQLResolver<?> resolver : resolvers) {
			assert resolver != null : "resolver is null";
		}
		this.schemaParserBuilder.resolvers(resolvers);
		return this;
	}

	public GraphQLSchemaExtensionConfiguration dataLoader(String key, DataLoader<?, ?> dataLoader) {
		assert key != null : "key is null";
		assert dataLoader != null : "dataLoader is null";
		this.dataLoaders.put(key, dataLoader);
		return this;
	}

	public GraphQLSchemaExtensionConfiguration batchLoader(String key, BatchLoaderWithContext<?, ?> batchLoader) {
		assert key != null : "key is null";
		assert batchLoader != null : "batchLoader is null";
		this.batchLoaders.put(key, batchLoader);
		return this;
	}

	public SchemaParserBuilder getSchemaParserBuilder() {
		return this.schemaParserBuilder;
	}

	private String readResource(Class<?> cls, String name) {
		try (
			InputStream in = cls.getResourceAsStream(name);
		) {
			return new String(in.readAllBytes(), StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
}
