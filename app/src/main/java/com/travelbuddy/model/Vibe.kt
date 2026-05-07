package com.travelbuddy.model

/**
 * Travel-style tags users pick on their profile and the matching scorer
 * uses for the "shared vibes" / "shared interests" weights.
 *
 * Mirror of the `Vibe` union in the web prototype's `src/lib/types.ts` so
 * Firestore docs round-trip identically once Sprint B lands. Wire ID is
 * the lowercase enum name — keep these stable; renames break stored docs.
 */
enum class Vibe {
    FOODIE,
    HIKING,
    SURF,
    MUSEUMS,
    NIGHTLIFE,
    SLOW,
    COWORKING,
    FAMILY,
    PHOTOGRAPHY,
    WELLNESS,
}
