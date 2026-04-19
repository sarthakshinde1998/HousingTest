package org.example.tests;

import static org.assertj.core.api.Assertions.assertThat;

import org.example.core.TestBase;
import org.example.models.SortMode;
import org.example.pages.HousingSearchPage;
import org.junit.jupiter.api.Test;

public class HousingSortingBehaviorTest extends TestBase {

  @Test
  void pricingLowestToHighest_thenHighestToLowest_fromEnglishHousingFlow() {
    HousingSearchPage housing = new HousingSearchPage(page);
    housing.openHome();
    housing.translateToEnglish();
    housing.clickHousingFromHome();
    housing.search("a");

    // Step 1: lowest to highest
    housing.setSortMode(SortMode.PRICE_ASC);
    assertThat(page.url())
        .as("Expected URL to reflect ascending price sort")
        .contains("sort=priceasc");

    // Step 2: highest to lowest
    housing.setSortMode(SortMode.PRICE_DESC);
    assertThat(page.url())
        .as("Expected URL to reflect descending price sort")
        .contains("sort=pricedsc");
  }
}

