package com.travelbuddy.repository

import com.travelbuddy.model.MatchProfile
import com.travelbuddy.trips.TripDraft
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class InMemoryRepositoryTest {

    @Test fun `initial state — defaults, no published trip, no pending chat`() {
        val repo = InMemoryRepository()
        assertEquals(TripDraft.defaults(), repo.tripDraft.value)
        assertNull(repo.publishedTrip.value)
        assertNull(repo.pendingChatPreview.value)
        assertEquals(3, repo.matches.value.size)
    }

    @Test fun `updateTripDraft trims whitespace on both ends`() {
        val repo = InMemoryRepository()
        repo.updateTripDraft { copy(destination = "  Lisbon  ", startDateIso = " 2026-07-10 ") }
        assertEquals("Lisbon", repo.tripDraft.value.destination)
        assertEquals("2026-07-10", repo.tripDraft.value.startDateIso)
    }

    @Test fun `publishTrip returns Error and leaves publishedTrip null when draft invalid`() {
        val repo = InMemoryRepository()
        repo.updateTripDraft { copy(destination = "", startDateIso = "junk", endDateIso = "junk") }
        val verdict = repo.publishTrip()
        assertTrue("expected Error, got $verdict", verdict is TripDraft.Validation.Error)
        assertNull(repo.publishedTrip.value)
    }

    @Test fun `publishTrip on valid draft copies into publishedTrip and trims fields`() {
        val repo = InMemoryRepository()
        repo.updateTripDraft {
            copy(destination = "  Lisbon  ", startDateIso = "2026-07-10", endDateIso = "2026-07-18")
        }
        val verdict = repo.publishTrip()
        assertEquals(TripDraft.Validation.Ok, verdict)
        val pub = repo.publishedTrip.value
        assertNotNull(pub)
        assertEquals("Lisbon", pub!!.destination)
        assertEquals("2026-07-10", pub.startDateIso)
        assertEquals("2026-07-18", pub.endDateIso)
    }

    @Test fun `matchById returns null for unknown id`() {
        val repo = InMemoryRepository()
        assertNull(repo.matchById("does-not-exist"))
    }

    @Test fun `matchById returns the seeded match for a known id`() {
        val first = InMemoryRepository.DEFAULT_SAMPLE_MATCHES.first()
        val repo = InMemoryRepository()
        assertEquals(first.id, repo.matchById(first.id)?.id)
    }

    @Test fun `custom seedMatches replaces the defaults`() {
        val custom = listOf(MatchProfile("x1", "Onlyperson", 50, "test"))
        val repo = InMemoryRepository(seedMatches = custom)
        assertEquals(1, repo.matches.value.size)
        assertEquals("x1", repo.matches.value[0].id)
    }

    @Test fun `rememberJoinSent stores a short message verbatim with clock-injected timestamp`() {
        val repo = InMemoryRepository(clock = { 1_700_000_000_000L })
        repo.rememberJoinSent("m1", "Alina", "Coffee at Manteigaria?")
        val pv = repo.pendingChatPreview.value!!
        assertEquals("m1", pv.matchId)
        assertEquals("Alina", pv.peerDisplayName)
        assertEquals("Coffee at Manteigaria?", pv.lastSentSnippet)
        assertEquals(1_700_000_000_000L, pv.sentAtMillis)
    }

    @Test fun `rememberJoinSent truncates messages over 140 chars to 140 chars ending in ellipsis`() {
        val repo = InMemoryRepository()
        repo.rememberJoinSent("m1", "Alina", "a".repeat(200))
        val snippet = repo.pendingChatPreview.value!!.lastSentSnippet
        assertEquals(140, snippet.length)
        assertTrue("snippet should end with ellipsis", snippet.endsWith("…"))
    }

    @Test fun `rememberJoinSent uses 'Traveler' when peer name is null`() {
        val repo = InMemoryRepository()
        repo.rememberJoinSent("m1", null, "hi")
        assertEquals("Traveler", repo.pendingChatPreview.value!!.peerDisplayName)
    }

    @Test fun `rememberJoinSent yields a placeholder snippet for blank-only messages`() {
        val repo = InMemoryRepository()
        repo.rememberJoinSent("m1", "Alina", "   ")
        assertEquals("(empty message)", repo.pendingChatPreview.value!!.lastSentSnippet)
    }
}
