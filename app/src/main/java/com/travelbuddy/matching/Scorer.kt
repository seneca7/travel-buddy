package com.travelbuddy.matching

import com.travelbuddy.model.Budget
import com.travelbuddy.model.Profile
import com.travelbuddy.model.Trip
import com.travelbuddy.model.Vibe
import java.time.temporal.ChronoUnit
import kotlin.math.abs
import kotlin.math.max

/**
 * Pure-function matching scorer. No platform deps, no I/O — call it from
 * any layer (ViewModel, repository, test).
 *
 * Weights mirror `travel-buddy-project-spec.md` exactly:
 * destination 40 / dates 25 / vibes 15 / budget 10 / languages 5 / interests 5
 * = 100 max. Ported 1:1 from the web prototype's `src/lib/scorer.ts` so
 * both clients rank the same Firestore corpus identically once Sprint B lands.
 */
object Scorer {

    data class ScoreBreakdown(
        val destination: Int,
        val dates: Int,
        val vibes: Int,
        val budget: Int,
        val languages: Int,
        val interests: Int,
        val total: Int,
        val overlapDays: Int,
        val sharedVibes: List<Vibe>,
        val sharedLanguages: List<String>,
    )

    enum class Bucket { STRONG, WORTH_A_LOOK, WEAK }

    fun score(
        meProfile: Profile,
        meTrip: Trip,
        candidateProfile: Profile,
        candidateTrip: Trip,
    ): ScoreBreakdown {
        val destination = if (meTrip.destinationId == candidateTrip.destinationId) 40 else 0

        val overlap = dateOverlapDays(meTrip, candidateTrip)
        val myWindow = tripWindowDays(meTrip)
        val dates = Math.round((overlap.toDouble() / myWindow) * 25).toInt()

        val sharedVibes = meProfile.vibes.filter { candidateProfile.vibes.contains(it) }
        val vibeRatio = sharedVibes.size.toDouble() / max(1, meProfile.vibes.size)
        val vibes = Math.round(vibeRatio * 15).toInt()

        val budgetDiff = abs(meProfile.budget.ordinal - candidateProfile.budget.ordinal)
        val budget = when (budgetDiff) {
            0 -> 10
            1 -> 6
            2 -> 2
            else -> 0
        }

        val sharedLanguages = meProfile.languages.filter { candidateProfile.languages.contains(it) }
        val languages = when (sharedLanguages.size) {
            0 -> 0
            1 -> 3
            else -> 5
        }

        // Interests reuse sharedVibes count — spec-matched dimension reuse,
        // not a bug. See web prototype scorer.ts for the same pattern.
        val interests = when {
            sharedVibes.size >= 3 -> 5
            sharedVibes.size == 2 -> 3
            sharedVibes.size == 1 -> 1
            else -> 0
        }

        val total = destination + dates + vibes + budget + languages + interests
        return ScoreBreakdown(
            destination = destination,
            dates = dates,
            vibes = vibes,
            budget = budget,
            languages = languages,
            interests = interests,
            total = total,
            overlapDays = overlap,
            sharedVibes = sharedVibes,
            sharedLanguages = sharedLanguages,
        )
    }

    fun bucketFor(score: Int, overlap: Int, sameDestination: Boolean): Bucket = when {
        !sameDestination -> Bucket.WEAK
        score >= 75 && overlap >= 3 -> Bucket.STRONG
        score >= 55 -> Bucket.WORTH_A_LOOK
        else -> Bucket.WEAK
    }

    private fun dateOverlapDays(a: Trip, b: Trip): Int {
        val start = maxOf(a.startDate, b.startDate)
        val end = minOf(a.endDate, b.endDate)
        val days = ChronoUnit.DAYS.between(start, end).toInt() + 1
        return max(0, days)
    }

    private fun tripWindowDays(t: Trip): Int {
        val days = ChronoUnit.DAYS.between(t.startDate, t.endDate).toInt() + 1
        return max(1, days)
    }
}
