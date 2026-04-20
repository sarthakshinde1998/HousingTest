package org.example.tests;

import static org.assertj.core.api.Assertions.assertThat;

import com.microsoft.playwright.options.LoadState;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.example.core.TestBase;
import org.example.pages.HousingSearchPage;
import org.example.pages.PostingDetailPage;
import org.junit.jupiter.api.Test;

public class HousingSortingBehaviorTest extends TestBase {

  private static final int LISTING_SAMPLES = 3;

  @Test
  void lowestToHighestPrice_listMatchesAscendingSort() {
    HousingSearchPage housing = new HousingSearchPage(page);
    housing.openHome();
    housing.translateToEnglish();
    housing.clickHousingFromHome();

    housing.selectLowestToHighestFromSortDropdown();

    List<Integer> original = new ArrayList<>(housing.readEuroPricesForOrdering(25));
    assertThat(original.size())
        .as("Need at least 2 prices to compare order")
        .isGreaterThanOrEqualTo(2);

    List<Integer> sortedAscending = new ArrayList<>(original);
    Collections.sort(sortedAscending);

    assertThat(original)
        .as("Displayed prices should already be in ascending order after selecting lowest→highest")
        .isEqualTo(sortedAscending);
  }

  @Test
  void highestToLowestPrice_listMatchesDescendingSort() {
    HousingSearchPage housing = new HousingSearchPage(page);
    housing.openHome();
    housing.translateToEnglish();
    housing.clickHousingFromHome();

    housing.selectHighestToLowestFromSortDropdown();

    List<Integer> original = new ArrayList<>(housing.readEuroPricesForOrdering(25));
    assertThat(original.size())
        .as("Need at least 2 prices to compare order")
        .isGreaterThanOrEqualTo(2);

    List<Integer> sortedDescending = new ArrayList<>(original);
    sortedDescending.sort(Collections.reverseOrder());

    assertThat(original)
        .as("Displayed prices should already be in descending order after selecting highest→lowest")
        .isEqualTo(sortedDescending);
  }

  /**
   * "Newest" list order follows activity: {@code updated} when present, else {@code posted}.
   */
  @Test
  void newestFirst_activityRecencyMatchesDescendingOrder() {
    HousingSearchPage housing = new HousingSearchPage(page);
    housing.openHome();
    housing.translateToEnglish();
    housing.clickHousingFromHome();

    housing.selectNewestFromSortDropdown();
    String listUrl = page.url();

    List<String> urls = housing.topListingAbsoluteUrls(LISTING_SAMPLES);
    assertThat(urls.size())
        .as("Need at least 2 listings to compare recency order")
        .isGreaterThanOrEqualTo(2);

    List<LocalDateTime> original = new ArrayList<>();
    for (String url : urls) {
      page.navigate(url);
      page.waitForLoadState(LoadState.NETWORKIDLE);
      original.add(PostingDetailPage.readActivitySortKey(page));
      page.navigate(listUrl);
      page.waitForLoadState(LoadState.NETWORKIDLE);
    }

    List<LocalDateTime> sortedNewestFirst = new ArrayList<>(original);
    sortedNewestFirst.sort(Comparator.reverseOrder());

    assertThat(original)
        .as("Listing order should match newest-first by activity (updated, else posted)")
        .isEqualTo(sortedNewestFirst);
  }

  /**
   * "Oldest" list order: same activity key, sorted ascending (oldest activity first).
   */
  @Test
  void oldestFirst_activityRecencyMatchesAscendingOrder() {
    HousingSearchPage housing = new HousingSearchPage(page);
    housing.openHome();
    housing.translateToEnglish();
    housing.clickHousingFromHome();

    housing.selectOldestFromSortDropdown();
    String listUrl = page.url();

    List<String> urls = housing.topListingAbsoluteUrls(LISTING_SAMPLES);
    assertThat(urls.size())
        .as("Need at least 2 listings to compare recency order")
        .isGreaterThanOrEqualTo(2);

    List<LocalDateTime> original = new ArrayList<>();
    for (String url : urls) {
      page.navigate(url);
      page.waitForLoadState(LoadState.NETWORKIDLE);
      original.add(PostingDetailPage.readActivitySortKey(page));
      page.navigate(listUrl);
      page.waitForLoadState(LoadState.NETWORKIDLE);
    }

    List<LocalDateTime> sortedOldestFirst = new ArrayList<>(original);
    sortedOldestFirst.sort(Comparator.naturalOrder());

    assertThat(original)
        .as("Listing order should match oldest-first by activity (updated, else posted)")
        .isEqualTo(sortedOldestFirst);
  }
}
