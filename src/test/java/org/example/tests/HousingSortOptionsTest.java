package org.example.tests;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.example.core.TestBase;
import org.example.models.SortMode;
import org.example.pages.HousingSearchPage;
import org.junit.jupiter.api.Test;

public class HousingSortOptionsTest extends TestBase {

  @Test
  void defaultSortModes_includePriceAscPriceDescNewest() {
    HousingSearchPage housing = new HousingSearchPage(page);
    housing.openHome();
    housing.translateToEnglish();
    housing.clickHousingFromHome();

    Set<SortMode> modes = housing.availableSortModes();

    assertThat(modes)
        .as("Default sort modes should include price asc/desc and newest")
        .contains(SortMode.NEWEST, SortMode.PRICE_ASC, SortMode.PRICE_DESC);
  }

  @Test
  void afterSearch_sortModesIncludeUpcomingAndRelevant() {
    HousingSearchPage housing = new HousingSearchPage(page);
    housing.openHome();
    housing.translateToEnglish();
    housing.clickHousingFromHome();

    housing.search("madrid");
    Set<SortMode> modes = housing.availableSortModes();

    assertThat(modes)
        .as("After searching, sort modes should include upcoming and relevant (plus defaults)")
        .contains(SortMode.NEWEST, SortMode.PRICE_ASC, SortMode.PRICE_DESC, SortMode.UPCOMING, SortMode.RELEVANT);
  }
}

