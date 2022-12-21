package com.sitepark.ies.extensions.graphql.api;

import org.dataloader.DataLoader;
import org.dataloader.DataLoaderOptions;

public interface DataLoaderBuilder {
	DataLoader<?, ?> buildDataLoader(DataLoaderOptions options);
}
