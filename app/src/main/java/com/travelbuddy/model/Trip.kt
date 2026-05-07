package com.travelbuddy.model

import java.time.LocalDate

/**
 * Published trip — destination, date window, intent, group size.
 * Distinct from the in-flight `TripDraft` used by the publish form.
 *
 * Field names mirror the web prototype's `Trip` (Firestore parity).
 */
data class Trip(
    val id: String,
    val ownerId: String,
    val destinationId: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val intent: Intent,
    val groupSize: Int,
) {
    enum class Intent {
        ANY,
        ACTIVITY,
        FEW_DAYS,
        WHOLE_TRIP,
    }
}
