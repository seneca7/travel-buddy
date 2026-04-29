package com.travelbuddy.trips

/**
 * Editing buffer for Publish flow. Dates are ISO-8601 calendar strings (`YYYY-MM-DD`) for simple forms and eventual Firestore parity.
 */
data class TripDraft(
    val destination: String,
    val startDateIso: String,
    val endDateIso: String,
) {
    companion object {
        /** Fallback home banner before the user publishes their first trip (onboarding teaser). */
        const val DemoDashboardHeadline = "Lisbon · 12 days left"

        fun defaults(): TripDraft = TripDraft(
            destination = "Barcelona",
            startDateIso = "2026-07-10",
            endDateIso = "2026-07-18",
        )

        fun trimFields(d: TripDraft): TripDraft = TripDraft(
            destination = d.destination.trim(),
            startDateIso = d.startDateIso.trim(),
            endDateIso = d.endDateIso.trim(),
        )
    }

    sealed class Validation {
        data object Ok : Validation()
        data class Error(val message: String) : Validation()
    }

    /** Client-side MVP validation before navigating to Matches. */
    fun validate(): Validation {
        val t = trimFields(this)
        if (t.destination.isBlank()) return Validation.Error("Add a destination")
        TripFormatting.parseDates(t) ?: return Validation.Error("Use valid dates (YYYY-MM-DD) with end on or after start")
        return Validation.Ok
    }
}
