package com.travelbuddy.matching

import com.travelbuddy.matching.Scorer.Bucket
import com.travelbuddy.model.Budget
import com.travelbuddy.model.Profile
import com.travelbuddy.model.Trip
import com.travelbuddy.model.Vibe
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ScorerTest {

    @Test fun `perfect match scores 100`() {
        val r = Scorer.score(me, meTrip, twin, twinTrip)
        assertEquals(40, r.destination)
        assertEquals(25, r.dates)
        assertEquals(15, r.vibes)
        assertEquals(10, r.budget)
        assertEquals(5, r.languages)
        assertEquals(5, r.interests)
        assertEquals(100, r.total)
    }

    @Test fun `different destinations zeroes destination dimension`() {
        val r = Scorer.score(me, meTrip, twin, twinTrip.copy(destinationId = "porto"))
        assertEquals(0, r.destination)
    }

    @Test fun `no date overlap zeroes dates dimension`() {
        val later = twinTrip.copy(
            startDate = LocalDate.of(2026, 8, 1),
            endDate = LocalDate.of(2026, 8, 7),
        )
        val r = Scorer.score(me, meTrip, twin, later)
        assertEquals(0, r.dates)
        assertEquals(0, r.overlapDays)
    }

    @Test fun `partial date overlap scales linearly with my window`() {
        // Me: Jul 10–18 (9 days). Candidate: Jul 14–18 (5-day overlap).
        val partial = twinTrip.copy(
            startDate = LocalDate.of(2026, 7, 14),
            endDate = LocalDate.of(2026, 7, 18),
        )
        val r = Scorer.score(me, meTrip, twin, partial)
        // Math.round((5 / 9) * 25) = Math.round(13.888…) = 14
        assertEquals(14, r.dates)
        assertEquals(5, r.overlapDays)
    }

    @Test fun `vibe ratio counts shared over my vibe count`() {
        // I have 4 vibes, candidate shares 1 → round(0.25 * 15) = 4
        val partialVibes = twin.copy(vibes = listOf(Vibe.FOODIE))
        val r = Scorer.score(me, meTrip, partialVibes, twinTrip)
        assertEquals(4, r.vibes)
        assertEquals(listOf(Vibe.FOODIE), r.sharedVibes)
    }

    @Test fun `no shared vibes zeroes both vibes and interests`() {
        val different = twin.copy(vibes = listOf(Vibe.NIGHTLIFE, Vibe.WELLNESS))
        val r = Scorer.score(me, meTrip, different, twinTrip)
        assertEquals(0, r.vibes)
        assertEquals(0, r.interests)
    }

    @Test fun `budget tiers step 10-6-2-0`() {
        val zero = me.copy(budget = Budget.SHOESTRING)
        val one = me.copy(budget = Budget.MID)
        val two = me.copy(budget = Budget.COMFORT)
        val three = me.copy(budget = Budget.LUXE)
        // Score against a SHOESTRING candidate to vary the diff cleanly.
        val candidate = twin.copy(budget = Budget.SHOESTRING)
        assertEquals(10, Scorer.score(zero, meTrip, candidate, twinTrip).budget)
        assertEquals(6, Scorer.score(one, meTrip, candidate, twinTrip).budget)
        assertEquals(2, Scorer.score(two, meTrip, candidate, twinTrip).budget)
        assertEquals(0, Scorer.score(three, meTrip, candidate, twinTrip).budget)
    }

    @Test fun `language scoring is 0-3-5 capped`() {
        val none = twin.copy(languages = listOf("DE"))
        val one = twin.copy(languages = listOf("EN"))
        val two = twin.copy(languages = listOf("EN", "PT"))
        val three = twin.copy(languages = listOf("EN", "PT", "ES"))
        assertEquals(0, Scorer.score(me, meTrip, none, twinTrip).languages)
        assertEquals(3, Scorer.score(me, meTrip, one, twinTrip).languages)
        assertEquals(5, Scorer.score(me, meTrip, two, twinTrip).languages)
        assertEquals(5, Scorer.score(me, meTrip, three, twinTrip).languages)
    }

    @Test fun `interests are 0-1-3-5 by shared vibe count`() {
        val zero = twin.copy(vibes = listOf(Vibe.NIGHTLIFE))
        val one = twin.copy(vibes = listOf(Vibe.FOODIE))
        val two = twin.copy(vibes = listOf(Vibe.FOODIE, Vibe.HIKING))
        val three = twin.copy(vibes = listOf(Vibe.FOODIE, Vibe.HIKING, Vibe.MUSEUMS))
        assertEquals(0, Scorer.score(me, meTrip, zero, twinTrip).interests)
        assertEquals(1, Scorer.score(me, meTrip, one, twinTrip).interests)
        assertEquals(3, Scorer.score(me, meTrip, two, twinTrip).interests)
        assertEquals(5, Scorer.score(me, meTrip, three, twinTrip).interests)
    }

    @Test fun `empty my-vibes does not divide by zero`() {
        val noVibes = me.copy(vibes = emptyList())
        val r = Scorer.score(noVibes, meTrip, twin, twinTrip)
        assertEquals(0, r.vibes)
        assertEquals(0, r.interests)
    }

    @Test fun `total caps at 100 for the perfect case`() {
        val r = Scorer.score(me, meTrip, twin, twinTrip)
        assertTrue("total ${r.total} should be ≤ 100", r.total <= 100)
        assertEquals(100, r.total)
    }

    @Test fun `bucket WEAK when destination differs regardless of score`() {
        assertEquals(Bucket.WEAK, Scorer.bucketFor(score = 99, overlap = 7, sameDestination = false))
    }

    @Test fun `bucket STRONG requires score 75 and overlap 3 and same destination`() {
        assertEquals(Bucket.STRONG, Scorer.bucketFor(score = 75, overlap = 3, sameDestination = true))
        assertEquals(Bucket.WORTH_A_LOOK, Scorer.bucketFor(score = 74, overlap = 3, sameDestination = true))
        assertEquals(Bucket.WORTH_A_LOOK, Scorer.bucketFor(score = 75, overlap = 2, sameDestination = true))
    }

    @Test fun `bucket WORTH_A_LOOK at score 55 boundary, WEAK below`() {
        assertEquals(Bucket.WORTH_A_LOOK, Scorer.bucketFor(score = 55, overlap = 0, sameDestination = true))
        assertEquals(Bucket.WEAK, Scorer.bucketFor(score = 54, overlap = 0, sameDestination = true))
    }

    // Fixtures ---------------------------------------------------------------

    private val me = Profile(
        id = "me",
        name = "Me",
        age = 30,
        photo = "",
        bio = "",
        languages = listOf("EN", "PT"),
        vibes = listOf(Vibe.FOODIE, Vibe.HIKING, Vibe.MUSEUMS, Vibe.PHOTOGRAPHY),
        budget = Budget.MID,
        verified = true,
        replyRate = 0.9f,
        tripsCount = 4,
    )

    private val twin = me.copy(id = "twin", name = "Twin")

    private val meTrip = Trip(
        id = "t-me",
        ownerId = "me",
        destinationId = "lisbon",
        startDate = LocalDate.of(2026, 7, 10),
        endDate = LocalDate.of(2026, 7, 18),
        intent = Trip.Intent.FEW_DAYS,
        groupSize = 1,
    )

    private val twinTrip = meTrip.copy(id = "t-twin", ownerId = "twin")
}
