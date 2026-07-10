package com.blindspot.app.util

import com.blindspot.app.data.model.Place
import java.util.Locale

/**
 * Humanizes a raw place category value (e.g. an API enum like "Fine_dining_restaurant") into a
 * short display label ("Fine Dining"). Never leaks underscores or lowercase enum strings to UI.
 */
fun formatCategory(raw: String): String {
    val words = raw
        .replace('_', ' ')
        .trim()
        .split(Regex("\\s+"))
        .filter { it.isNotBlank() }
    if (words.isEmpty()) return ""

    val cleaned = words
        // "Fine_dining_restaurant" reads better as "Fine Dining" — drop a trailing generic noun
        // when it follows a more specific descriptor.
        .let { if (it.size > 1 && it.last().lowercase(Locale.US) in GENERIC_SUFFIXES) it.dropLast(1) else it }

    return cleaned.joinToString(" ") { word ->
        word.lowercase(Locale.US).replaceFirstChar { it.titlecase(Locale.US) }
    }
}

private val GENERIC_SUFFIXES = setOf("restaurant", "establishment", "place", "venue")

val Place.categoryLabel: String
    get() = formatCategory(category)

/** Price level (1–4) rendered as "$"–"$$$$". */
val Place.priceLabel: String?
    get() = priceLevel?.let { "$".repeat(it.coerceIn(1, 4)) }

/** Rating formatted with a single decimal, e.g. "4.5". */
val Place.ratingLabel: String?
    get() = rating?.let { String.format(Locale.US, "%.1f", it) }
