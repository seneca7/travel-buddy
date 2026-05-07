package com.travelbuddy.model

/**
 * Ordered cheapest → priciest. The matching scorer uses the ordinal
 * distance between two travelers' budgets, so the order here is
 * load-bearing — keep it.
 */
enum class Budget {
    SHOESTRING,
    MID,
    COMFORT,
    LUXE,
}
