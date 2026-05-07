package com.travelbuddy.model

/**
 * Request from one traveler to join another's trip. Chat between the
 * two unlocks only when [status] becomes [Status.ACCEPTED] — the spec's
 * core safety invariant ("Chat unlocks after approval").
 *
 * Fields mirror the web prototype's `JoinRequest` in `types.ts` exactly
 * so Firestore round-trips are identical for both clients.
 *
 * @property sentAt epoch millis (matches web's number-typed sentAt)
 */
data class JoinRequest(
    val id: String,
    val fromUserId: String,
    val toUserId: String,
    val tripId: String,
    val proposal: Proposal,
    val message: String,
    val meetupSuggestion: String? = null,
    val activityId: String? = null,
    val activityLabel: String? = null,
    val status: Status,
    val sentAt: Long,
) {
    enum class Proposal { ACTIVITY, FEW_DAYS, WHOLE_TRIP }
    enum class Status { PENDING, ACCEPTED, DECLINED }
}
