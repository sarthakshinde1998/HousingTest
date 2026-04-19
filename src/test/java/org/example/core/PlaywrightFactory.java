package org.example.core;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;
import org.example.config.TestConfig;

public final class PlaywrightFactory {
  private PlaywrightFactory() {}

  public static Playwright createPlaywright() {
    return Playwright.create();
  }

  public static Browser launchChromium(Playwright playwright) {
    return playwright.chromium().launch(
        new BrowserType.LaunchOptions()
            .setHeadless(TestConfig.headless())
            .setSlowMo((double) TestConfig.slowMoMs())
    );
  }
}

