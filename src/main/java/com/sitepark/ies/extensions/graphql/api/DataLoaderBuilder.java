package com.sitepark.ies.extensions.graphql.api;

import org.dataloader.DataLoader;
import org.dataloader.DataLoaderOptions;

/**
 * A batch loader that creates its data loader itself, e.g. with its own cache map. The IES calls it
 * for every request with the options it needs, e.g. the context provider of the execution scope.
 */
@SuppressWarnings("PMD.ImplicitFunctionalInterface")
public interface DataLoaderBuilder {
  DataLoader<?, ?> buildDataLoader(DataLoaderOptions options);
}
