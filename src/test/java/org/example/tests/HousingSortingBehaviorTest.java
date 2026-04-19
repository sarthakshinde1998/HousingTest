package org.example.tests;

import static org.example.assertions.SortAssertions.assertPricesNonDecreasingInts;
import static org.example.assertions.SortAssertions.assertPricesNonIncreasingInts;

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

    // Step 1: lowest to highest
    housing.setSortMode(SortMode.PRICE_ASC);
    var ascPrices = housing.readFirstVisibleEuroPrices(20);
    assertPricesNonDecreasingInts(ascPrices);

    // Step 2: highest to lowest
    housing.setSortMode(SortMode.PRICE_DESC);
    var descPrices = housing.readFirstVisibleEuroPrices(20);
    assertPricesNonIncreasingInts(descPrices);
  }
}

