package org.example.core;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class JunitArtifactsExtension implements TestWatcher {
  private static final Logger log = LoggerFactory.getLogger(JunitArtifactsExtension.class);

  public interface ArtifactContextProvider {
    Page page();
    BrowserContext context();
    Path artifactDir();
  }

  private final ArtifactContextProvider provider;
  private final AtomicBoolean failed = new AtomicBoolean(false);

  public JunitArtifactsExtension(ArtifactContextProvider provider) {
    this.provider = provider;
  }

  public boolean failed() {
    return failed.get();
  }

  @Override
  public void testFailed(ExtensionContext context, Throwable cause) {
    failed.set(true);
    Path dir = provider.artifactDir();
    Artifacts.ensureDir(dir);

    try {
      Page page = provider.page();
      if (page != null) {
        Path screenshot = Artifacts.screenshotPath(dir);
        Artifacts.captureScreenshot(page, screenshot);
        log.info("Saved screenshot: {}", screenshot.toAbsolutePath());
      }
    } catch (RuntimeException e) {
      log.warn("Failed to capture screenshot: {}", e.getMessage());
    }

    try {
      BrowserContext browserContext = provider.context();
      if (browserContext != null) {
        Path traceZip = Artifacts.tracePath(dir);
        Artifacts.stopTrace(browserContext.tracing(), traceZip);
        log.info("Saved trace: {}", traceZip.toAbsolutePath());
      }
    } catch (RuntimeException e) {
      log.warn("Failed to stop trace: {}", e.getMessage());
    }

    log.info("Test failed: {} - {}", context.getDisplayName(), cause.toString());
    log.info("Artifacts saved under: {}", dir.toAbsolutePath());
  }
}

