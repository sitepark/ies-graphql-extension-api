package com.sitepark.ies.extensions.graphql.api;

@SuppressWarnings("PMD.ImplicitFunctionalInterface")
public interface GraphQLSchemaExtension {
  void initialize(GraphQLSchemaExtensionConfiguration config);
}
