package com.travelbuddy.model

/**
 * Bridges join-request drafts to Chat UX until realtime messaging + Firestore thread ids exist.
 */
data class PendingChatPreview(
    val matchId: String,
    val peerDisplayName: String,
    val lastSentSnippet: String,
    val sentAtMillis: Long,
)
