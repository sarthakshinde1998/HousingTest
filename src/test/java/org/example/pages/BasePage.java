package org.example.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import java.time.Duration;
import org.example.core.Waits;

public abstract class BasePage {
  protected final Page page;

  protected BasePage(Page page) {
    this.page = page;
  }

  protected void click(Locator locator) {
    locator.first().click();
  }

  protected void waitVisible(Locator locator, Duration timeout) {
    Waits.waitVisible(locator.first(), timeout);
  }
}

