package com.sitepark.ies.extensions.graphql.api;

import org.dataloader.DataLoader;
import org.dataloader.DataLoaderOptions;

@SuppressWarnings("PMD.ImplicitFunctionalInterface")
public interface DataLoaderBuilder {
  DataLoader<?, ?> buildDataLoader(DataLoaderOptions options);
}
