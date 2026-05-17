package com.travelbuddy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travelbuddy.model.MatchProfile
import com.travelbuddy.model.PendingChatPreview
import com.travelbuddy.repository.InMemoryRepository
import com.travelbuddy.repository.Repository
import com.travelbuddy.trips.TripDraft
import com.travelbuddy.trips.TripFormatting
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Presentation layer. Reads raw state from [Repository] and exposes derived
 * presentation state (dashboard headline, readiness percentage) via
 * [TripFormatting].
 *
 * Pre-publish: Home shows the fixed onboarding teaser headline + a demo
 * readiness value. Post-publish: both derive from the latest snapshot in
 * [Repository.publishedTrip].
 *
 * Default constructor wires an [InMemoryRepository] so Compose's `viewModel()`
 * keeps working with no factory. Sprint B's `FirestoreRepository` will inject
 * via Hilt; the test suite injects fakes directly.
 */
class TravelViewModel(
    private val repository: Repository = InMemoryRepository(),
) : ViewModel() {

    val tripDraft: StateFlow<TripDraft> = repository.tripDraft
    val matches: StateFlow<List<MatchProfile>> = repository.matches
    val pendingChatPreview: StateFlow<PendingChatPreview?> = repository.pendingChatPreview

    private val _tripHeadline = MutableStateFlow(TripDraft.DemoDashboardHeadline)
    val tripHeadline: StateFlow<String> = _tripHeadline.asStateFlow()

    private val _readiness = MutableStateFlow(DEFAULT_DEMO_READINESS)
    val readiness: StateFlow<Float> = _readiness.asStateFlow()

    init {
        viewModelScope.launch {
            repository.publishedTrip.collect { pub ->
                if (pub == null) {
                    _tripHeadline.value = TripDraft.DemoDashboardHeadline
                    _readiness.value = DEFAULT_DEMO_READINESS
                } else {
                    _tripHeadline.value = TripFormatting.headlinePreview(pub)
                    _readiness.value = TripFormatting.readiness01(pub)
                }
            }
        }
    }

    fun updateDraft(transform: TripDraft.() -> TripDraft) {
        repository.updateTripDraft(transform)
    }

    fun publishTrip(): TripDraft.Validation = repository.publishTrip()

    fun matchById(id: String): MatchProfile? = repository.matchById(id)

    fun matchSummaryLine(profile: MatchProfile): String =
        "${profile.name} · ${profile.score}"

    fun rememberJoinSent(matchId: String, peerDisplayName: String?, messageBody: String) {
        repository.rememberJoinSent(matchId, peerDisplayName, messageBody)
    }

    companion object {
        private const val DEFAULT_DEMO_READINESS = 0.64f
    }
}
