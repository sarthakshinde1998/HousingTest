package org.example.config;

import java.nio.file.Path;

public final class TestConfig {
  private TestConfig() {}

  public static String baseUrl() {
    return System.getProperty("baseUrl", "https://madrid.craigslist.org");
  }

  public static boolean headless() {
    return Boolean.parseBoolean(System.getProperty("headless", "true"));
  }

  public static int slowMoMs() {
    return Integer.parseInt(System.getProperty("slowMo", "0"));
  }

  public static int timeoutMs() {
    return Integer.parseInt(System.getProperty("timeoutMs", "30000"));
  }

  public static Path artifactsDir() {
    return Path.of(System.getProperty("artifactsDir", "target/artifacts"));
  }
}

