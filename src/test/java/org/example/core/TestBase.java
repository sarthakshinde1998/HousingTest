package org.example.core;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Tracing;
import java.nio.file.Path;
import java.time.Duration;
import org.example.config.TestConfig;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class TestBase implements JunitArtifactsExtension.ArtifactContextProvider {
  private static final Logger log = LoggerFactory.getLogger(TestBase.class);

  protected static Playwright playwright;
  protected static Browser browser;

  protected BrowserContext context;
  protected Page page;

  protected Path artifactDir;

  @RegisterExtension
  final JunitArtifactsExtension artifactsExtension = new JunitArtifactsExtension(this);

  @BeforeAll
  static void beforeAll() {
    playwright = PlaywrightFactory.createPlaywright();
    browser = PlaywrightFactory.launchChromium(playwright);
  }

  @AfterAll
  static void afterAll() {
    if (browser != null) browser.close();
    if (playwright != null) playwright.close();
  }

  @BeforeEach
  void beforeEach(TestInfo testInfo) {
    String testId = testInfo.getTestClass().map(Class::getSimpleName).orElse("Test")
        + "-" + testInfo.getDisplayName();
    artifactDir = Artifacts.testArtifactDir(testId);
    Artifacts.ensureDir(artifactDir);

    context = browser.newContext(new Browser.NewContextOptions()
        .setRecordVideoDir(artifactDir.resolve("video"))
        .setViewportSize(1280, 800));
    context.setDefaultTimeout(TestConfig.timeoutMs());
    context.tracing().start(new Tracing.StartOptions()
        .setScreenshots(true)
        .setSnapshots(true)
        .setSources(true));

    page = context.newPage();
    page.setDefaultTimeout(TestConfig.timeoutMs());
  }

  @AfterEach
  void afterEach() {
    try {
      if (!artifactsExtension.failed() && context != null) {
        // Avoid leaking traces on success; keep artifacts only on failure.
        try {
          context.tracing().stop();
        } catch (RuntimeException ignored) {
        }
      }
    } finally {
      try {
        if (context != null) context.close();
      } catch (RuntimeException e) {
        log.warn("Failed to close context: {}", e.getMessage());
      } finally {
        context = null;
        page = null;
      }
    }
  }

  protected void open(String absoluteOrRelativeUrl) {
    String url = absoluteOrRelativeUrl.startsWith("http")
        ? absoluteOrRelativeUrl
        : TestConfig.baseUrl() + absoluteOrRelativeUrl;
    page.navigate(url, new Page.NavigateOptions().setTimeout((double) TestConfig.timeoutMs()));
    Waits.waitForNetworkIdle(page, Duration.ofSeconds(10));
  }

  @Override
  public Page page() {
    return page;
  }

  @Override
  public BrowserContext context() {
    return context;
  }

  @Override
  public Path artifactDir() {
    return artifactDir;
  }
}

