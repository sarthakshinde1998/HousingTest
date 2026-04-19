package org.example.assertions;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.example.models.ListingCard;

public final class SortAssertions {
  private SortAssertions() {}

  public static void assertPricesNonDecreasingInts(List<Integer> prices) {
    assertThat(prices)
        .as("Expected at least 2 prices to validate ordering, got: %s", prices)
        .hasSizeGreaterThanOrEqualTo(2);
    for (int i = 1; i < prices.size(); i++) {
      assertThat(prices.get(i))
          .as("Prices should be non-decreasing at index %s: %s", i, prices)
          .isGreaterThanOrEqualTo(prices.get(i - 1));
    }
  }

  public static void assertPricesNonIncreasingInts(List<Integer> prices) {
    assertThat(prices)
        .as("Expected at least 2 prices to validate ordering, got: %s", prices)
        .hasSizeGreaterThanOrEqualTo(2);
    for (int i = 1; i < prices.size(); i++) {
      assertThat(prices.get(i))
          .as("Prices should be non-increasing at index %s: %s", i, prices)
          .isLessThanOrEqualTo(prices.get(i - 1));
    }
  }

  public static void assertPricesNonDecreasing(List<ListingCard> listings) {
    List<Integer> prices = listings.stream()
        .flatMap(l -> l.priceValue().stream())
        .toList();
    assertThat(prices)
        .as("Expected at least 3 priced listings to validate ordering, got: %s", prices)
        .hasSizeGreaterThanOrEqualTo(3);

    for (int i = 1; i < prices.size(); i++) {
      assertThat(prices.get(i))
          .as("Prices should be non-decreasing at index %s: %s", i, prices)
          .isGreaterThanOrEqualTo(prices.get(i - 1));
    }
  }

  public static void assertPricesNonIncreasing(List<ListingCard> listings) {
    List<Integer> prices = listings.stream()
        .flatMap(l -> l.priceValue().stream())
        .toList();
    assertThat(prices)
        .as("Expected at least 3 priced listings to validate ordering, got: %s", prices)
        .hasSizeGreaterThanOrEqualTo(3);

    for (int i = 1; i < prices.size(); i++) {
      assertThat(prices.get(i))
          .as("Prices should be non-increasing at index %s: %s", i, prices)
          .isLessThanOrEqualTo(prices.get(i - 1));
    }
  }
}

