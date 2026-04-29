package com.travelbuddy.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.HealthAndSafety
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.PhoneInTalk
import androidx.compose.material.icons.outlined.TravelExplore
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.travelbuddy.BuildConfig
import com.travelbuddy.TravelViewModel
import com.travelbuddy.model.MatchProfile
import com.travelbuddy.trips.TripDraft
import kotlin.math.roundToInt

private val VerticalRhythm = 22.dp

@Composable
private fun SectionTitle(
    title: String,
    modifier: Modifier = Modifier,
    caption: String? = null,
) {
    Column(modifier) {
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (caption != null) {
            Text(
                caption,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun EmptyStateCard(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    iconContent: @Composable () -> Unit,
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
    ) {
        Column(
            Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                iconContent()
            }
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(
                body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun TimelineDot(isUnread: Boolean) {
    Box(
        modifier = Modifier
            .padding(top = 6.dp)
            .size(10.dp)
            .clip(CircleShape)
            .background(
                if (isUnread) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant,
            ),
    )
}

private data class InAppTimelineItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val timeLabel: String,
    val isUnread: Boolean,
)

private val sampleTimeline = listOf(
    InAppTimelineItem(
        id = "1",
        title = "Join request sent",
        subtitle = "Marek’s group acknowledged your Porto overlap.",
        timeLabel = "Today · 9:41",
        isUnread = false,
    ),
    InAppTimelineItem(
        id = "2",
        title = "New match suggestion",
        subtitle = "Alina fits your foodie + museum vibes for Lisbon.",
        timeLabel = "Yesterday",
        isUnread = true,
    ),
    InAppTimelineItem(
        id = "3",
        title = "Trip reminder",
        subtitle = "Trip readiness is 72% · add visa proof to finish.",
        timeLabel = "Wed",
        isUnread = false,
    ),
)

private data class ChatTurn(
    val id: String,
    val mine: Boolean,
    val senderLabel: String,
    val body: String,
    val time: String,
)

@Composable
fun HomeScreen(
    vm: TravelViewModel,
    onCreateTrip: () -> Unit,
) {
    val headline by vm.tripHeadline.collectAsState()
    val readiness by vm.readiness.collectAsState()
    val matches by vm.matches.collectAsState()
    val percent = (readiness * 100).roundToInt()
    val topPreview = matches.take(2)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(VerticalRhythm),
        contentPadding = PaddingValues(vertical = 20.dp),
    ) {
        item {
            Text(
                headline,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            ) {
                Column(Modifier.padding(20.dp)) {
                    SectionTitle("Trip readiness", caption = "${percent}% — profile & dates")
                    LinearProgressIndicator(
                        progress = { readiness },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                            .clip(MaterialTheme.shapes.small),
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
        item {
            Button(onClick = onCreateTrip, modifier = Modifier.fillMaxWidth()) {
                Text("Create trip")
            }
        }
        item {
            SectionTitle("Top matches", caption = "From your readiness sample")
        }
        if (topPreview.isEmpty()) {
            item {
                EmptyStateCard(
                    title = "No preview yet",
                    body = "Create a trip to see travelers who fit your pace and routes.",
                    iconContent = {
                        Icon(
                            Icons.Outlined.Chat,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(16.dp),
                        )
                    },
                )
            }
        } else {
            items(topPreview, key = { it.id }) { m ->
                Text(
                    vm.matchSummaryLine(m),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.small)
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                )
            }
        }
    }
}

@Composable
fun CreateTripScreen(
    vm: TravelViewModel,
    onPublishSuccess: () -> Unit,
) {
    val draft by vm.tripDraft.collectAsState()
    var publishError by remember { mutableStateOf<String?>(null) }

    Column(
        Modifier
            .fillMaxSize()
            .padding(vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(VerticalRhythm),
    ) {
        Text(
            "Publish a journey",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        OutlinedTextField(
            value = draft.destination,
            onValueChange = { vm.updateDraft { copy(destination = it) } },
            label = { Text("Destination") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        OutlinedTextField(
            value = draft.startDateIso,
            onValueChange = { vm.updateDraft { copy(startDateIso = it) } },
            label = { Text("Start date (YYYY-MM-DD)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        OutlinedTextField(
            value = draft.endDateIso,
            onValueChange = { vm.updateDraft { copy(endDateIso = it) } },
            label = { Text("End date (YYYY-MM-DD)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        publishError?.let { err ->
            Text(
                err,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = {
                publishError = null
                when (val r = vm.publishTrip()) {
                    is TripDraft.Validation.Error -> publishError = r.message
                    TripDraft.Validation.Ok -> onPublishSuccess()
                }
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Publish and find matches")
        }
    }
}

@Composable
fun MatchesScreen(
    vm: TravelViewModel,
    onOpenProfile: (matchId: String) -> Unit,
) {
    val matches by vm.matches.collectAsState()

    LazyColumn(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(VerticalRhythm),
        contentPadding = PaddingValues(vertical = 20.dp),
    ) {
        item {
            SectionTitle("Compatible travelers", caption = "Ranked by your trip fit")
        }
        if (matches.isEmpty()) {
            item {
                EmptyStateCard(
                    title = "No matches surfaced",
                    body = "Tune your dates or destinations — refreshed pairs land here.",
                    iconContent = {
                        Icon(
                            Icons.Outlined.TravelExplore,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(16.dp),
                        )
                    },
                )
            }
        }
        items(matches, key = { it.id }) { match ->
            MatchCard(match) { onOpenProfile(match.id) }
        }
    }
}

@Composable
private fun MatchCard(match: MatchProfile, onOpen: () -> Unit) {
    ElevatedCard(
        onClick = onOpen,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(match.name, style = MaterialTheme.typography.titleMedium)
            Text(
                "Score ${match.score}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                match.reason,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
fun ProfileScreen(
    vm: TravelViewModel,
    matchId: String,
    onJoinRequest: (matchId: String) -> Unit,
) {
    val match = vm.matchById(matchId)

    Column(
        Modifier
            .fillMaxSize()
            .padding(vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(VerticalRhythm),
    ) {
        when (match) {
            null ->
                ElevatedCard(
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                    ),
                ) {
                    Text(
                        "Traveler not found (id=$matchId).",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(20.dp),
                    )
                }
            else -> {
                Text(match.name, style = MaterialTheme.typography.headlineSmall)
                Text(
                    "Verified · 4.9/5 · 92% reply rate",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                SectionTitle("Compatibility", caption = "Score ${match.score} — ${match.reason}")
                Text(
                    "Why you match: overlap, budget, and shared interests align with our sample scorer.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Button(onClick = { onJoinRequest(match.id) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Send join request")
                }
            }
        }
    }
}

@Composable
fun JoinRequestScreen(
    vm: TravelViewModel,
    matchId: String,
    onSend: () -> Unit,
) {
    var message by rememberSaveable(matchId) {
        mutableStateOf("Would love to join for food walks and one museum day.")
    }
    val peerName = vm.matchById(matchId)?.name

    Column(
        Modifier
            .fillMaxSize()
            .padding(vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(VerticalRhythm),
    ) {
        Text("Join request", style = MaterialTheme.typography.headlineSmall)
        if (peerName != null) {
            SectionTitle(
                title = peerName,
                caption = "Chat unlocks once they accept — we never fake scarcity timers.",
                modifier = Modifier,
            )
        }
        OutlinedTextField(
            value = message,
            onValueChange = { message = it },
            label = { Text("Message") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
        )
        Button(
            onClick = {
                vm.rememberJoinSent(matchId, peerName, message)
                onSend()
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Send request")
        }
    }
}

@Composable
fun ChatScreen(vm: TravelViewModel) {
    val preview by vm.pendingChatPreview.collectAsState()
    val peerTitle = preview?.peerDisplayName ?: "Match chat preview"
    var draft by rememberSaveable { mutableStateOf("") }
    val localExtras = remember { mutableStateListOf<ChatTurn>() }

    val baseThread = remember(preview?.matchId, preview?.lastSentSnippet) {
        val key = preview?.matchId ?: "none"
        buildList {
            add(
                ChatTurn(
                    id = "welcome-$key",
                    mine = false,
                    senderLabel = peerTitle.split(" ").firstOrNull() ?: "Buddy",
                    body = preview?.lastSentSnippet?.let {
                        "Thanks for the note: \"$it\" — ping me once you arrive in-town."
                    } ?: "Trip overlap looks strong — confirm public meetup preferences first?",
                    time = preview?.let { "~ now" } ?: "Sample",
                ),
            )
            add(
                ChatTurn(
                    id = "you-$key",
                    mine = true,
                    senderLabel = "You",
                    body = preview?.lastSentSnippet?.let { "Sent: $it" } ?: "Hi! Still good for the shared museum block?",
                    time = "10:04",
                ),
            )
        }
    }

    Column(Modifier.fillMaxSize()) {
        SectionTitle(
            title = peerTitle,
            caption = preview?.let { "Last join note → ${it.lastSentSnippet}" }
                ?: "Sample thread — realtime sync comes with chat backend.",
        )
        Spacer(Modifier.height(12.dp))
        LazyColumn(
            Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 12.dp),
        ) {
            items(baseThread + localExtras, key = { it.id }) { bubble ->
                ChatBubbleRow(bubble)
            }
        }
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                value = draft,
                onValueChange = { draft = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Say something kind and specific…") },
                maxLines = 4,
            )
            IconButton(
                enabled = draft.isNotBlank(),
                onClick = {
                    val msg = draft.trim()
                    draft = ""
                    localExtras.add(
                        ChatTurn(
                            id = "local-${System.nanoTime()}",
                            mine = true,
                            senderLabel = "You",
                            body = msg,
                            time = "now",
                        ),
                    )
                },
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send")
            }
        }
    }
}

@Composable
private fun ChatBubbleRow(line: ChatTurn) {
    val align = if (line.mine) Alignment.End else Alignment.Start
    val bg = if (line.mine) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainerHigh
    }
    val textColor = if (line.mine) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    Column(Modifier.fillMaxWidth(), horizontalAlignment = align) {
        Text(
            line.senderLabel,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Surface(
            color = bg,
            shape = MaterialTheme.shapes.medium,
        ) {
            Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                Text(line.body, color = textColor, style = MaterialTheme.typography.bodyMedium)
                Text(
                    line.time,
                    Modifier.padding(top = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
fun NotificationsScreen() {
    var devEmptyToggle by remember { mutableStateOf(false) }
    val feed = remember(devEmptyToggle) {
        if (devEmptyToggle) emptyList() else sampleTimeline
    }

    LazyColumn(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(vertical = 24.dp),
    ) {
        item {
            SectionTitle("Timeline", caption = "Join requests, matches, reminders")
            if (BuildConfig.DEBUG) {
                TextButton(onClick = { devEmptyToggle = !devEmptyToggle }) {
                    Text(if (devEmptyToggle) "Load sample alerts" else "Preview empty state (debug)")
                }
            }
        }
        if (feed.isEmpty()) {
            item {
                EmptyStateCard(
                    title = "Quiet inbox",
                    body = "We only interrupt for trip-critical moments — no churn tricks.",
                    iconContent = {
                        Icon(
                            Icons.Outlined.NotificationsNone,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(16.dp),
                        )
                    },
                )
            }
        } else {
            items(feed, key = { it.id }) { item ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.medium)
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    TimelineDot(isUnread = item.isUnread)
                    Column(Modifier.weight(1f)) {
                        Text(item.title, style = MaterialTheme.typography.titleMedium)
                        Text(
                            item.subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            item.timeLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SafetyScreen(
    onOpenBlockedTravelers: () -> Unit,
    onOpenReporting: () -> Unit,
) {
    LazyColumn(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(VerticalRhythm),
        contentPadding = PaddingValues(vertical = 24.dp),
    ) {
        item {
            SectionTitle(
                "Safety center",
                caption = "You control escalation — we hide sensitive details until you approve chat.",
            )
        }
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                ),
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Principles",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    listOf(
                        "No artificial scarcity timers on trust cues.",
                        "No confirm-shaming to stay in chats you ended.",
                        "Notifications stay granular — mute without losing safety alerts.",
                        "Meet-ups default to daytime public spaces until mutually agreed otherwise.",
                    ).forEachIndexed { idx, bullet ->
                        Text(
                            "${idx + 1}. $bullet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
        item {
            ElevatedCard(
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f),
                ),
            ) {
                Column(
                    Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Outlined.HealthAndSafety,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                        )
                        Column {
                            Text("Emergency & SOS", style = MaterialTheme.typography.titleSmall)
                            Text(
                                "Use local emergency numbers when life safety is at risk.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    Text(
                        "Displaying your itinerary to trusted contacts stays opt-in.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
        item {
            ElevatedCard(
                onClick = onOpenReporting,
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                ),
            ) {
                Row(
                    Modifier.padding(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Outlined.Flag, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Column(Modifier.weight(1f)) {
                        Text("Report a concern", style = MaterialTheme.typography.titleSmall)
                        Text(
                            "Route to moderated review queues (placeholder wiring).",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Icon(
                        Icons.AutoMirrored.Filled.NavigateNext,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        item {
            ElevatedCard(
                onClick = onOpenBlockedTravelers,
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                ),
            ) {
                Row(
                    Modifier.padding(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Outlined.Block, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                    Column(Modifier.weight(1f)) {
                        Text("Blocked travelers & groups", style = MaterialTheme.typography.titleSmall)
                        Text(
                            "Muted threads stay unread — swap for server sync soon.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Icon(
                        Icons.AutoMirrored.Filled.NavigateNext,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        item {
            ElevatedCard(
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                ),
            ) {
                Row(
                    Modifier.padding(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Outlined.PhoneInTalk, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Column {
                        Text("Voice check-ins", style = MaterialTheme.typography.titleSmall)
                        Text(
                            "Short audio nudges for meet-ups stay optional and ephemeral by default.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SafetyToolkitPlaceholderScreen(
    title: String,
    supporting: String,
    onNavigateBack: () -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(title) },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
        )
        Box(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            ElevatedCard(
                modifier = Modifier.widthIn(max = 460.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                ),
            ) {
                Text(
                    supporting,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(24.dp),
                )
            }
        }
    }
}
