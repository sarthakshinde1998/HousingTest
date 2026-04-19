package org.example.models;

import java.util.Optional;

public record ListingCard(String title, String priceRaw, Optional<Integer> priceValue) {}

