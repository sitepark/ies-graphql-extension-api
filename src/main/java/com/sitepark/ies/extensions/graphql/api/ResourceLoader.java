package com.sitepark.ies.extensions.graphql.api;

import java.io.InputStream;
public class ResourceLoader {
  public InputStream getResourceAsStream(Class<?> cls, String name) {
    return cls.getResourceAsStream(name);
  }
}
