package org.example.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads posting/recency timestamps from a Craigslist listing detail page.
 * Craigslist list sort follows <strong>activity</strong>: use {@code updated} when shown, else {@code posted}.
 */
public final class PostingDetailPage {
  private PostingDetailPage() {}

  private static final Pattern YMD = Pattern.compile("(\\d{4})[/-](\\d{2})[/-](\\d{2})");

  public static LocalDateTime readActivitySortKey(Page page) {
    page.waitForLoadState(LoadState.NETWORKIDLE);
    page.waitForTimeout(400);
    revealAbsoluteTimesBestEffort(page);

    Optional<String> updatedIso = firstDatetimeInParagraphContaining(page, "updated", "actualiz");
    Optional<String> postedIso = firstDatetimeInParagraphContaining(page, "posted", "publicado", "public");

    String raw = updatedIso.orElseGet(() -> postedIso.orElseThrow(() ->
        new AssertionError("Could not find posted/updated <time datetime> on listing page: " + page.url())));

    return parseToLocalDateTime(raw);
  }

  private static void revealAbsoluteTimesBestEffort(Page page) {
    Locator times = page.locator("time.date, time.timeago, .date.timeago");
    int n = Math.min(times.count(), 8);
    for (int i = 0; i < n; i++) {
      try {
        if (times.nth(i).isVisible()) {
          times.nth(i).click();
          page.waitForTimeout(120);
        }
      } catch (RuntimeException ignored) {
      }
    }
  }

  private static Optional<String> firstDatetimeInParagraphContaining(Page page, String... needles) {
    Locator paragraphs = page.locator("main p, .postinginfos p, p.postinginfo, section p");
    int c = paragraphs.count();
    for (int i = 0; i < c; i++) {
      String text = paragraphs.nth(i).innerText().toLowerCase();
      boolean match = Arrays.stream(needles).map(String::toLowerCase).anyMatch(text::contains);
      if (!match) {
        continue;
      }
      Locator t = paragraphs.nth(i).locator("time[datetime]");
      if (t.count() > 0) {
        String dt = t.first().getAttribute("datetime");
        if (dt != null && !dt.isBlank()) {
          return Optional.of(dt.trim());
        }
      }
    }
    return Optional.empty();
  }

  static LocalDateTime parseToLocalDateTime(String raw) {
    String s = raw.trim().replace(' ', 'T');
    try {
      return OffsetDateTime.parse(s).toLocalDateTime();
    } catch (DateTimeParseException ignored) {
    }
    try {
      return LocalDateTime.parse(s, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    } catch (DateTimeParseException ignored) {
    }
    Matcher m = YMD.matcher(raw);
    if (m.find()) {
      return LocalDateTime.of(
          Integer.parseInt(m.group(1)),
          Integer.parseInt(m.group(2)),
          Integer.parseInt(m.group(3)),
          0, 0);
    }
    throw new AssertionError("Unparseable datetime: " + raw);
  }
}
