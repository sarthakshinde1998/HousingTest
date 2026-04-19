package org.example.core;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;
import java.time.Duration;

public final class Waits {
  private Waits() {}

  public static void waitForNetworkIdle(Page page, Duration timeout) {
    page.waitForLoadState(LoadState.NETWORKIDLE, new Page.WaitForLoadStateOptions().setTimeout(timeout.toMillis()));
  }

  public static void waitVisible(Locator locator, Duration timeout) {
    locator.waitFor(new Locator.WaitForOptions()
        .setTimeout(timeout.toMillis())
        .setState(WaitForSelectorState.VISIBLE));
  }
}

