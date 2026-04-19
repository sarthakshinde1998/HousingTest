package org.example.core;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.Tracing;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import org.example.config.TestConfig;

public final class Artifacts {
  private Artifacts() {}

  public static Path testArtifactDir(String testId) {
    String ts = DateTimeFormatter.ISO_INSTANT.format(Instant.now()).replace(':', '-');
    return TestConfig.artifactsDir().resolve(safeFilePart(testId)).resolve(ts);
  }

  public static void ensureDir(Path dir) {
    try {
      Files.createDirectories(dir);
    } catch (IOException e) {
      throw new RuntimeException("Failed to create artifacts dir: " + dir, e);
    }
  }

  public static Path screenshotPath(Path dir) {
    return dir.resolve("screenshot.png");
  }

  public static Path tracePath(Path dir) {
    return dir.resolve("trace.zip");
  }

  public static void captureScreenshot(Page page, Path path) {
    page.screenshot(new Page.ScreenshotOptions().setPath(path).setFullPage(true));
  }

  public static void stopTrace(Tracing tracing, Path traceZip) {
    tracing.stop(new Tracing.StopOptions().setPath(traceZip));
  }

  private static String safeFilePart(String input) {
    return input.replaceAll("[^a-zA-Z0-9._-]+", "_");
  }
}

