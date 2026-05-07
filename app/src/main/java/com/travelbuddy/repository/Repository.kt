package com.travelbuddy.repository

import com.travelbuddy.model.MatchProfile
import com.travelbuddy.model.PendingChatPreview
import com.travelbuddy.trips.TripDraft
import kotlinx.coroutines.flow.StateFlow

/**
 * Single source of truth for app data, swappable behind one interface.
 * The Sprint A in-memory impl drives previews + tests; Sprint B's
 * `FirestoreRepository` will drop in here without touching the ViewModel.
 *
 * Holds raw state only — derived/presentation state (headline strings,
 * readiness percentage) stays in the ViewModel via `TripFormatting`.
 */
interface Repository {

    /** In-flight editing buffer for the publish form. */
    val tripDraft: StateFlow<TripDraft>

    /** Last successfully published snapshot. Null until first publish. */
    val publishedTrip: StateFlow<TripDraft?>

    /** Suggested matches for the current viewer. Sample data for now. */
    val matches: StateFlow<List<MatchProfile>>

    /** Last join-request snippet, used to seed the Chat preview thread. */
    val pendingChatPreview: StateFlow<PendingChatPreview?>

    fun updateTripDraft(transform: TripDraft.() -> TripDraft)

    /** Validates the current draft; on Ok, copies it into [publishedTrip]. */
    fun publishTrip(): TripDraft.Validation

    fun matchById(id: String): MatchProfile?

    fun rememberJoinSent(matchId: String, peerDisplayName: String?, messageBody: String)
}
