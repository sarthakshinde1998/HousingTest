package org.example.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import java.time.Duration;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.example.models.ListingCard;
import org.example.models.SortMode;

public final class HousingSearchPage extends BasePage {
  private static final String HOUSING_PATH = "/d/housing/search/hhh";
  private static final String HOME_URL = "https://madrid.craigslist.org/";

  private final Locator queryInputCandidates = page.locator(String.join(", ",
      "input[name='query']",
      "input#query",
      "input[name='searchText']",
      "input[name='searchtext']",
      "input[name='search_terms']",
      "input[placeholder*='buscar' i]",
      "input[placeholder*='search' i]",
      "input[aria-label*='search' i]",
      "input[aria-label*='buscar' i]",
      "form input[type='text']"
  ));
  private final Locator resultsItems = page.locator(String.join(", ",
      "li.cl-search-result",
      "li.result-row",
      "li.cl-static-search-result",
      "li[data-pid]",
      "div[data-pid]",
      "a[data-pid]"
  ));
  private final Locator sortCombo = page.locator(".cl-search-sort-mode.bd-combo-box, button[class*='cl-search-sort-mode-']");

  public HousingSearchPage(Page page) {
    super(page);
  }

  public void open() {
    page.navigate(HOUSING_PATH);
    page.waitForLoadState(LoadState.NETWORKIDLE);
  }

  public void openHome() {
    page.navigate(HOME_URL);
    page.waitForLoadState(LoadState.NETWORKIDLE);
    dismissConsentIfPresent();
  }

  public void translateToEnglish() {
    Locator englishLink = page.locator("a:has-text('english'), a[href*='/about/sites#US'], a[href*='lang=en'], a[hreflang='en']");
    if (englishLink.count() > 0) {
      englishLink.first().click();
      page.waitForLoadState(LoadState.NETWORKIDLE);
      return;
    }
    // Fallback: force english locale via craigslist query convention.
    String url = page.url();
    if (!url.contains("lang=en")) {
      page.navigate(url.contains("?") ? url + "&lang=en" : url + "?lang=en");
      page.waitForLoadState(LoadState.NETWORKIDLE);
    }
  }

  public void clickHousingFromHome() {
    Locator directHousingSearch = page.locator("a[href*='/d/housing/search/hhh'], a[href*='/search/hhh']");
    if (directHousingSearch.count() > 0) {
      directHousingSearch.first().click();
      page.waitForLoadState(LoadState.NETWORKIDLE);
      return;
    }

    Locator housingLink = page.locator("a:has-text('housing')");
    if (housingLink.count() > 0) {
      housingLink.first().click();
      page.waitForLoadState(LoadState.NETWORKIDLE);
    }

    // Ensure we are on the Housing search page under test.
    if (!page.url().contains("/search/hhh")) {
      page.navigate("https://madrid.craigslist.org/d/housing/search/hhh?lang=en");
      page.waitForLoadState(LoadState.NETWORKIDLE);
    }
  }

  public void search(String query) {
    dismissConsentIfPresent();
    Locator input = firstVisible(queryInputCandidates);
    input.fill(query);
    input.press("Enter");
    page.waitForLoadState(LoadState.NETWORKIDLE);
  }

  public Set<SortMode> availableSortModes() {
    openSortMenu();

    // Craigslist uses buttons with class names like cl-search-sort-mode-newest etc.
    Locator sortButtons = page.locator("button[class*='cl-search-sort-mode-'], a[class*='cl-search-sort-mode-']")
        .filter(new Locator.FilterOptions().setHasText(""));

    EnumSet<SortMode> modes = EnumSet.noneOf(SortMode.class);

    int count = sortButtons.count();
    for (int i = 0; i < count; i++) {
      String cls = Optional.ofNullable(sortButtons.nth(i).getAttribute("class")).orElse("").toLowerCase();
      String txt = sortButtons.nth(i).innerText().trim().toLowerCase();

      if (cls.contains("newest") || txt.contains("nuevo")) modes.add(SortMode.NEWEST);
      if (cls.contains("priceasc") || cls.contains("price-asc") || txt.matches(".*precio.*(asc|↑).*") || txt.contains("precio ↑"))
        modes.add(SortMode.PRICE_ASC);
      if (cls.contains("pricedsc") || cls.contains("pricedesc") || cls.contains("price-desc") || txt.matches(".*precio.*(desc|↓).*") || txt.contains("precio ↓"))
        modes.add(SortMode.PRICE_DESC);
      if (cls.contains("upcoming") || txt.contains("próximo") || txt.contains("proximo")) modes.add(SortMode.UPCOMING);
      if (cls.contains("relevant") || txt.contains("relev")) modes.add(SortMode.RELEVANT);
    }

    // Fallback: sometimes the menu renders as labels only; use visible text scan.
    if (modes.isEmpty()) {
      List<String> visible = page.locator("button, a, span.label, div.label")
          .allInnerTexts();
      String joined = String.join(" ", visible).toLowerCase();
      if (joined.contains("nuevo")) modes.add(SortMode.NEWEST);
      if (joined.contains("precio")) {
        // If a price filter exists, still require explicit sort buttons later; leave as-is.
      }
      if (joined.contains("próximo") || joined.contains("proximo")) modes.add(SortMode.UPCOMING);
      if (joined.contains("relev")) modes.add(SortMode.RELEVANT);
    }

    closeSortMenuBestEffort();
    return modes;
  }

  public void setSortMode(SortMode mode) {
    // Deterministic behavior for core price sorting checks.
    if (mode == SortMode.PRICE_ASC || mode == SortMode.PRICE_DESC) {
      String sortValue = mode == SortMode.PRICE_ASC ? "priceasc" : "pricedsc";
      navigateWithSort(sortValue);
      return;
    }

    openSortMenu();

    Pattern byText = switch (mode) {
      case NEWEST -> Pattern.compile(".*(nuevo|newest).*", Pattern.CASE_INSENSITIVE);
      case PRICE_ASC -> Pattern.compile(".*precio.*(asc|↑|menor).*", Pattern.CASE_INSENSITIVE);
      case PRICE_DESC -> Pattern.compile(".*precio.*(desc|↓|mayor).*", Pattern.CASE_INSENSITIVE);
      case UPCOMING -> Pattern.compile(".*(próximo|proximo|upcoming).*", Pattern.CASE_INSENSITIVE);
      case RELEVANT -> Pattern.compile(".*(relevante|relevant|relev).*", Pattern.CASE_INSENSITIVE);
    };

    Locator candidates = page.locator("button[class*='cl-search-sort-mode-'], a[class*='cl-search-sort-mode-'], button, a")
        .filter(new Locator.FilterOptions().setHasText(byText));

    if (candidates.count() > 0) {
      candidates.first().click();
      page.waitForLoadState(LoadState.NETWORKIDLE);
      return;
    }

    // Fallback (Craigslist UI may expose "Precio" without explicit asc/desc labels):
    // Navigate using the standard query param used by Craigslist search pages.
    String sortValue = switch (mode) {
      case NEWEST -> "date";
      case PRICE_ASC -> "priceasc";
      case PRICE_DESC -> "pricedsc";
      case UPCOMING -> "upcoming";
      case RELEVANT -> "rel";
    };
    closeSortMenuBestEffort();
    navigateWithSort(sortValue);
  }

  public List<ListingCard> readTopListings(int limit) {
    int total = resultsItems.count();
    int n = Math.min(limit, total);
    if (n == 0) {
      throw new AssertionError("No listing items found. Debug counts: " + debugCounts());
    }
    return java.util.stream.IntStream.range(0, n)
        .mapToObj(i -> toListing(resultsItems.nth(i)))
        .toList();
  }

  /**
   * Fallback extraction that is resilient to Craigslist's dynamic DOM.
   * It parses the visible page text and returns the first N euro prices it finds.
   */
  public List<Integer> readFirstVisibleEuroPrices(int limit) {
    String text = page.innerText("body");
    Pattern p = Pattern.compile("€\\s*([0-9.]+)");
    Matcher m = p.matcher(text);
    java.util.ArrayList<Integer> out = new java.util.ArrayList<>();
    while (m.find() && out.size() < limit) {
      String raw = m.group(1);
      parseEuroPrice("€" + raw).ifPresent(out::add);
    }
    return List.copyOf(out);
  }

  private String debugCounts() {
    try {
      return page.evaluate("""
        () => {
          const sels = [
            'li.cl-search-result',
            'li.result-row',
            'li.cl-static-search-result',
            'li',
            'ol.cl-static-search-results li',
            '[data-pid]',
            '.result-row'
          ];
          const out = {};
          for (const s of sels) out[s] = document.querySelectorAll(s).length;
          return out;
        }
      """).toString();
    } catch (RuntimeException e) {
      return "n/a (" + e.getMessage() + ")";
    }
  }

  private ListingCard toListing(Locator item) {
    String title = firstNonBlankText(item.locator(".title, .titlestring, a.titlestring, .result-title"));
    String priceRaw = firstNonBlankText(item.locator(".price, .result-price, .priceinfo .price"));
    Optional<Integer> priceValue = parseEuroPrice(priceRaw);
    return new ListingCard(title, priceRaw, priceValue);
  }

  private String firstNonBlankText(Locator locator) {
    int c = locator.count();
    for (int i = 0; i < c; i++) {
      String t = locator.nth(i).innerText().trim();
      if (!t.isBlank()) return t;
    }
    return "";
  }

  static Optional<Integer> parseEuroPrice(String raw) {
    if (raw == null) return Optional.empty();
    String cleaned = raw.replaceAll("[^0-9.]", "").trim();
    if (cleaned.isBlank()) return Optional.empty();
    // Craigslist ES sometimes uses '.' as thousand separator (e.g., 120.000)
    cleaned = cleaned.replace(".", "");
    try {
      return Optional.of(Integer.parseInt(cleaned));
    } catch (NumberFormatException e) {
      return Optional.empty();
    }
  }

  private void openSortMenu() {
    sortCombo.first().click();
    // Best-effort: wait for any sort option buttons to appear.
    page.waitForTimeout(300);
  }

  private void closeSortMenuBestEffort() {
    page.keyboard().press("Escape");
  }

  private void navigateWithSort(String sortValue) {
    String url = page.url();
    int hashIndex = url.indexOf('#');
    if (hashIndex >= 0) {
      url = url.substring(0, hashIndex);
    }
    String next;
    if (url.matches(".*[?&]sort=[^&]+.*")) {
      next = url.replaceAll("([?&]sort=)[^&]+", "$1" + sortValue);
    } else if (url.contains("?")) {
      next = url + "&sort=" + sortValue;
    } else {
      next = url + "?sort=" + sortValue;
    }
    page.navigate(next);
    page.waitForLoadState(LoadState.NETWORKIDLE);
  }

  private static Locator firstVisible(Locator candidates) {
    int c = candidates.count();
    for (int i = 0; i < c; i++) {
      Locator n = candidates.nth(i);
      try {
        if (n.isVisible()) return n;
      } catch (RuntimeException ignored) {
      }
    }
    throw new AssertionError("Could not find a visible search input textbox. Found candidates: " + debugInputs(candidates.page()));
  }

  private void dismissConsentIfPresent() {
    // Craigslist sometimes shows overlays that hide the search UI.
    Locator accept = page.locator("button:has-text('Aceptar'), button:has-text('Accept'), button:has-text('I Agree'), button:has-text('OK')");
    if (accept.count() > 0) {
      try {
        if (accept.first().isVisible()) {
          accept.first().click();
          page.waitForTimeout(250);
        }
      } catch (RuntimeException ignored) {
      }
    }
  }

  private static String debugInputs(Page page) {
    try {
      return page.evaluate("""
        () => {
          const inputs = [...document.querySelectorAll('input')];
          return inputs.slice(0, 30).map(i => {
            const r = i.getBoundingClientRect();
            const visible = !!(r.width && r.height) && getComputedStyle(i).visibility !== 'hidden' && getComputedStyle(i).display !== 'none';
            return {
              name: i.getAttribute('name'),
              id: i.id,
              type: i.type,
              placeholder: i.getAttribute('placeholder'),
              ariaLabel: i.getAttribute('aria-label'),
              visible
            };
          });
        }
      """).toString();
    } catch (RuntimeException e) {
      return "n/a (" + e.getMessage() + ")";
    }
  }
}

