package com.travelbuddy.repository

import com.travelbuddy.model.MatchProfile
import com.travelbuddy.model.PendingChatPreview
import com.travelbuddy.trips.TripDraft
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Sprint A's process-local implementation of [Repository]. Drives
 * @Preview composables, ViewModel construction in dev, and unit tests
 * for any consumer of the interface. Sprint B's `FirestoreRepository`
 * implements the same interface and replaces this in production wiring.
 *
 * State dies with the process — by design. No persistence here.
 */
class InMemoryRepository(
    seedMatches: List<MatchProfile> = DEFAULT_SAMPLE_MATCHES,
    clock: () -> Long = System::currentTimeMillis,
) : Repository {

    private val clock = clock

    private val _tripDraft = MutableStateFlow(TripDraft.defaults())
    override val tripDraft: StateFlow<TripDraft> = _tripDraft.asStateFlow()

    private val _publishedTrip = MutableStateFlow<TripDraft?>(null)
    override val publishedTrip: StateFlow<TripDraft?> = _publishedTrip.asStateFlow()

    private val _matches = MutableStateFlow(seedMatches)
    override val matches: StateFlow<List<MatchProfile>> = _matches.asStateFlow()

    private val _pendingChatPreview = MutableStateFlow<PendingChatPreview?>(null)
    override val pendingChatPreview: StateFlow<PendingChatPreview?> = _pendingChatPreview.asStateFlow()

    override fun updateTripDraft(transform: TripDraft.() -> TripDraft) {
        _tripDraft.value = TripDraft.trimFields(transform(_tripDraft.value))
    }

    override fun publishTrip(): TripDraft.Validation =
        when (val verdict = _tripDraft.value.validate()) {
            is TripDraft.Validation.Error -> verdict
            is TripDraft.Validation.Ok -> {
                val pub = TripDraft.trimFields(_tripDraft.value)
                _publishedTrip.value = pub
                _tripDraft.value = pub
                TripDraft.Validation.Ok
            }
        }

    override fun matchById(id: String): MatchProfile? = _matches.value.find { it.id == id }

    override fun rememberJoinSent(matchId: String, peerDisplayName: String?, messageBody: String) {
        val peer = peerDisplayName ?: "Traveler"
        val snippet = messageBody.trim().let {
            when {
                it.isEmpty() -> "(empty message)"
                it.length <= 140 -> it
                else -> it.take(137) + "…"
            }
        }
        _pendingChatPreview.value = PendingChatPreview(
            matchId = matchId,
            peerDisplayName = peer,
            lastSentSnippet = snippet,
            sentAtMillis = clock(),
        )
    }

    companion object {
        // Legacy sample matches retained until Sprint B wires the real
        // Scorer (matching/Scorer.kt) over Firestore-backed profiles.
        // Score field stays precomputed since MatchProfile is the UI DTO,
        // not the domain Profile + Trip pair the scorer consumes.
        val DEFAULT_SAMPLE_MATCHES: List<MatchProfile> = listOf(
            MatchProfile(
                id = "1",
                name = "Alina Petrova",
                score = 87,
                reason = "5-day overlap · Verified · Food and museums",
            ),
            MatchProfile(
                id = "2",
                name = "Marek Group",
                score = 82,
                reason = "3 travelers · Porto extension · Mid budget",
            ),
            MatchProfile(
                id = "3",
                name = "Jonas Costa",
                score = 79,
                reason = "Exact dates · Surf mornings · EN/PT",
            ),
        )
    }
}
