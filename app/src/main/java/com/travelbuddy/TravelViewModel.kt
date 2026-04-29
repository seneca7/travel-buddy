package com.travelbuddy

import androidx.lifecycle.ViewModel
import com.travelbuddy.model.MatchProfile
import com.travelbuddy.model.PendingChatPreview
import com.travelbuddy.trips.TripDraft
import com.travelbuddy.trips.TripFormatting
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Trip dashboard copy + readiness:
 *
 * Until [publishTrip] succeeds once ([publishedTripSnapshot] is still null]), Home stays on the fixed
 * onboarding teaser headline + demo readiness — draft edits stored in VM do not overwrite Home yet.
 *
 * After publish, headline/readiness derive from [publishedTripSnapshot] via [TripFormatting] (immutable
 * on Home until another successful publish replaces the snapshot).
 *
 * Matches feed stays sample data until Firebase.
 */
class TravelViewModel : ViewModel() {

    private val _tripDraft = MutableStateFlow(TripDraft.defaults())
    val tripDraft: StateFlow<TripDraft> = _tripDraft.asStateFlow()

    private val _publishedTripSnapshot = MutableStateFlow<TripDraft?>(null)

    /** Pre-publish: demo teaser; post-publish: [TripFormatting.headlinePreview] on last published trip. */
    private val _tripHeadline = MutableStateFlow(TripDraft.DemoDashboardHeadline)
    val tripHeadline: StateFlow<String> = _tripHeadline.asStateFlow()

    /** Pre-publish: fixed onboarding progress; post-publish: [TripFormatting.readiness01]. */
    private val _readiness = MutableStateFlow(DEFAULT_DEMO_READINESS)
    val readiness: StateFlow<Float> = _readiness.asStateFlow()

    private val _matches = MutableStateFlow<List<MatchProfile>>(emptyList())
    val matches: StateFlow<List<MatchProfile>> = _matches.asStateFlow()

    /** Shown on Chat until real messaging exists — seeded when user taps Send on join flow. */
    private val _pendingChatPreview = MutableStateFlow<PendingChatPreview?>(null)
    val pendingChatPreview: StateFlow<PendingChatPreview?> = _pendingChatPreview.asStateFlow()

    init {
        loadSampleData()
        refreshTripPresentation()
    }

    fun updateDraft(transform: TripDraft.() -> TripDraft) {
        _tripDraft.value = TripDraft.trimFields(transform(_tripDraft.value))
    }

    /**
     * Validates current draft and, on success, copies it into the published snapshot that drives Home.
     */
    fun publishTrip(): TripDraft.Validation {
        return when (val verdict = _tripDraft.value.validate()) {
            is TripDraft.Validation.Error -> verdict
            is TripDraft.Validation.Ok -> {
                val published = TripDraft.trimFields(_tripDraft.value)
                _publishedTripSnapshot.value = published
                _tripDraft.value = published
                refreshTripPresentation()
                TripDraft.Validation.Ok
            }
        }
    }

    private fun refreshTripPresentation() {
        val pub = _publishedTripSnapshot.value
        if (pub == null) {
            _tripHeadline.value = TripDraft.DemoDashboardHeadline
            _readiness.value = DEFAULT_DEMO_READINESS
        } else {
            _tripHeadline.value = TripFormatting.headlinePreview(pub)
            _readiness.value = TripFormatting.readiness01(pub)
        }
    }

    private fun loadSampleData() {
        _matches.value = listOf(
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

    fun matchById(id: String): MatchProfile? = _matches.value.find { it.id == id }

    fun matchSummaryLine(profile: MatchProfile): String =
        "${profile.name} · ${profile.score}"

    /** Persists lightweight context for Chat header/snippet wiring (Firestore thread id later). */
    fun rememberJoinSent(matchId: String, peerDisplayName: String?, messageBody: String) {
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
            sentAtMillis = System.currentTimeMillis(),
        )
    }

    companion object {
        private const val DEFAULT_DEMO_READINESS = 0.64f
    }
}
