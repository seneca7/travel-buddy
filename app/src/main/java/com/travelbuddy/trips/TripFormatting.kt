package com.travelbuddy.trips

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit

/**
 * Pure functions for dashboards — easy to swap for unit tests once repositories exist.
 */
object TripFormatting {

    private val Iso = DateTimeFormatter.ISO_LOCAL_DATE

    fun parseDates(draft: TripDraft): Pair<LocalDate, LocalDate>? {
        val trimmed = TripDraft.trimFields(draft)
        return try {
            val start = LocalDate.parse(trimmed.startDateIso, Iso)
            val end = LocalDate.parse(trimmed.endDateIso, Iso)
            if (!end.isBefore(start)) start to end else null
        } catch (_: DateTimeParseException) {
            null
        }
    }

    /** Headline for home / dashboards from any draft-shaped values (preview + published use same formatter). */
    fun headlinePreview(draft: TripDraft): String {
        val trimmed = TripDraft.trimFields(draft)
        val dest = trimmed.destination.takeIf { it.isNotEmpty() }

        val span = parseDates(trimmed)
        if (dest == null || dest.isEmpty()) return TripDraft.DemoDashboardHeadline
        val today = LocalDate.now()
        val start = span?.first
        val end = span?.second
        val daysUntilStart = start?.let { ChronoUnit.DAYS.between(today, it).coerceAtLeast(0) }
        val daysUntilEnd = end?.let { ChronoUnit.DAYS.between(today, it).coerceAtLeast(0) }

        return when {
            start == null ->
                "$dest · set valid start and end dates (YYYY-MM-DD)"
            today.isBefore(start) ->
                "$dest · ${daysUntilStart?.toInt() ?: 0} days until start"
            !today.isAfter(end) ->
                "$dest · in progress (${daysUntilEnd?.toInt() ?: 0} days until end)"
            else ->
                "$dest · wrap up highlights or plan the next chapter"
        }
    }

    /**
     * 0..1 score from spec-aligned “trip readiness” heuristics (destination + valid window + nearing departure).
     */
    fun readiness01(draft: TripDraft): Float {
        val trimmed = TripDraft.trimFields(draft)
        val dest = trimmed.destination.trim()
        val span = parseDates(trimmed)
        val today = LocalDate.now()

        var score = 0f
        if (dest.isNotEmpty()) score += 0.32f

        span?.let { (start, _) ->
            if (!start.isBefore(today.plusDays(1))) score += 0.28f
            if (!today.isBefore(start) && span.second.isAfter(span.first)) score += 0.18f
        }

        val daysAhead = span?.first?.let { ChronoUnit.DAYS.between(today, it) } ?: -1
        when {
            daysAhead in 0..14 -> score += 0.12f
            daysAhead in 15..45 -> score += 0.18f
            daysAhead > 45 -> score += 0.12f
        }

        if (span == null && dest.isNotEmpty()) score += 0.08f

        return score.coerceIn(0f, 1f)
    }
}
