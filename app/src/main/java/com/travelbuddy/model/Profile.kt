package com.travelbuddy.model

/**
 * Traveler profile — the matching scorer reads vibes, budget, and
 * languages from this. Keep field names aligned with the web prototype's
 * `Profile` type so the Firestore schema (Sprint B) is one mapping for
 * both clients.
 *
 * Distinct from the legacy UI DTO `MatchProfile`, which is a precomputed
 * card display and will be retired when Sprint B replaces sample data.
 *
 * @property replyRate fraction in [0, 1]
 */
data class Profile(
    val id: String,
    val name: String,
    val age: Int,
    val photo: String,
    val bio: String,
    val languages: List<String>,
    val vibes: List<Vibe>,
    val budget: Budget,
    val verified: Boolean,
    val replyRate: Float,
    val tripsCount: Int,
)
