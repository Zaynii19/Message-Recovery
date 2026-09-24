package com.example.messagerecovery.presentation.ui.chatlist

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.messagerecovery.presentation.theme.AlertRed
import com.example.messagerecovery.presentation.theme.CyberObsidian
import com.example.messagerecovery.presentation.theme.CyberSurfaceDark
import com.example.messagerecovery.presentation.theme.CyberSurfaceElevated
import com.example.messagerecovery.presentation.theme.ElectricCyan
import com.example.messagerecovery.presentation.theme.MessageRecoveryTheme
import com.example.messagerecovery.presentation.theme.TextPrimary
import com.example.messagerecovery.presentation.theme.TextSecondary
import com.example.messagerecovery.presentation.theme.WarningYellow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatListContent(
    state: ChatListUiState,
    onSearchQueryChange: (String) -> Unit,
    onAppFilterSelect: (String) -> Unit,
    onToggleOnlyDeleted: () -> Unit,
    onThreadClick: (String) -> Unit,
    onDeleteThread: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var threadToDelete by remember { mutableStateOf<ChatThreadUiModel?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberObsidian)
    ) {
        // Search Bar
        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = {
                Text(
                    text = "Search contacts or messages...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = ElectricCyan
                )
            },
            trailingIcon = {
                if (state.searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear search",
                            tint = TextSecondary
                        )
                    }
                }
            },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = CyberSurfaceDark,
                unfocusedContainerColor = CyberSurfaceDark,
                focusedBorderColor = ElectricCyan,
                unfocusedBorderColor = CyberSurfaceElevated,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            singleLine = true
        )

        // Filter Pills Row
        val filters = listOf(
            "ALL" to "All",
            "com.whatsapp" to "WhatsApp",
            "com.whatsapp.w4b" to "WA Business",
            "com.facebook.orca" to "Messenger",
            "com.instagram.android" to "Instagram"
        )

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filters) { (pkg, label) ->
                val isSelected = state.selectedAppFilter == pkg
                FilterChip(
                    selected = isSelected,
                    onClick = { onAppFilterSelect(pkg) },
                    label = { Text(text = label, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = CyberSurfaceElevated,
                        labelColor = TextSecondary,
                        selectedContainerColor = ElectricCyan.copy(alpha = 0.2f),
                        selectedLabelColor = ElectricCyan
                    )
                )
            }

            item {
                FilterChip(
                    selected = state.onlyDeletedFilter,
                    onClick = onToggleOnlyDeleted,
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = if (state.onlyDeletedFilter) AlertRed else WarningYellow
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Deleted Only", fontSize = 12.sp)
                        }
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = CyberSurfaceElevated,
                        labelColor = TextSecondary,
                        selectedContainerColor = AlertRed.copy(alpha = 0.25f),
                        selectedLabelColor = AlertRed
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Content List / Empty State
        if (state.threads.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(CyberSurfaceDark),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ChatBubbleOutline,
                            contentDescription = null,
                            tint = ElectricCyan.copy(alpha = 0.6f),
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    val hasActiveFilter = state.searchQuery.isNotEmpty() || state.onlyDeletedFilter || state.selectedAppFilter != "ALL"

                    val emptyTitle = when {
                        state.searchQuery.isNotEmpty() -> "No matching conversations"
                        state.onlyDeletedFilter -> "No deleted messages found"
                        state.selectedAppFilter != "ALL" -> "No conversations for this app"
                        else -> "No intercepted messages yet"
                    }
                    val emptySubtitle = when {
                        state.searchQuery.isNotEmpty() -> "Try searching for a different contact name or message keyword."
                        state.onlyDeletedFilter -> "When contacts delete or unsend messages, they will automatically appear here."
                        state.selectedAppFilter != "ALL" -> "Incoming messages from this app will be saved here once received."
                        else -> "Incoming notifications and unsend events from WhatsApp, WA Business, Messenger, and Instagram will be logged here automatically."
                    }

                    Text(
                        text = emptyTitle,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = emptySubtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    if (hasActiveFilter) {
                        Spacer(modifier = Modifier.height(16.dp))
                        androidx.compose.material3.Button(
                            onClick = {
                                onSearchQueryChange("")
                                onAppFilterSelect("ALL")
                            },
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = ElectricCyan,
                                contentColor = Color(0xFF00363F)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Show All Chats",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    items = state.threads,
                    key = { it.id }
                ) { thread ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = CyberSurfaceDark),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .combinedClickable(
                                onClick = { onThreadClick(thread.id) },
                                onLongClick = { threadToDelete = thread }
                            )
                            .border(
                                width = 1.dp,
                                color = if (thread.hasDeletedMessages) AlertRed.copy(alpha = 0.4f) else CyberSurfaceElevated,
                                shape = RoundedCornerShape(14.dp)
                            )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Avatar with App Badge
                            Box(
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(getAppColor(thread.packageName).copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = thread.title.take(1).uppercase(),
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = getAppColor(thread.packageName)
                                    )
                                }

                                // App Origin Dot
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .align(Alignment.BottomEnd)
                                        .clip(CircleShape)
                                        .background(getAppColor(thread.packageName))
                                        .border(2.dp, CyberSurfaceDark, CircleShape)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            // Contact Title, Last Snippet, Deleted Badge
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = thread.title,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = TextPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = formatTimestamp(thread.lastTimestamp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondary
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = thread.lastSnippet.ifBlank { "Attachment received" },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                if (thread.hasDeletedMessages) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(AlertRed.copy(alpha = 0.18f))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Warning,
                                                contentDescription = null,
                                                tint = AlertRed,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "DELETED DETECTED",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 10.sp
                                                ),
                                                color = AlertRed
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    threadToDelete?.let { thread ->
        AlertDialog(
            onDismissRequest = { threadToDelete = null },
            title = {
                Text(
                    text = "Delete Conversation?",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete all recovered messages and data for \"${thread.title}\"? This action cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteThread(thread.id)
                        threadToDelete = null
                    }
                ) {
                    Text("Delete", color = AlertRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { threadToDelete = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = CyberSurfaceDark,
            icon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = AlertRed
                )
            }
        )
    }
}

fun getAppColor(packageName: String): Color {
    return when (packageName) {
        "com.whatsapp" -> Color(0xFF25D366) // WhatsApp Emerald Green
        "com.whatsapp.w4b" -> Color(0xFF00A884) // WhatsApp Business Teal
        "com.facebook.orca" -> Color(0xFF0084FF) // Messenger Electric Blue
        "com.instagram.android" -> Color(0xFFE1306C) // Instagram Magenta
        else -> ElectricCyan
    }
}

fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    val date = Date(timestamp)
    return when {
        diff < 60_000L -> "Just now"
        diff < 24 * 3600_000L -> SimpleDateFormat("h:mm a", Locale.getDefault()).format(date)
        diff < 48 * 3600_000L -> "Yesterday"
        else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(date)
    }
}

@Preview(showSystemUi = true)
@Composable
fun ChatListContentPreview() {
    MessageRecoveryTheme(darkTheme = true) {
        ChatListContent(
            state = ChatListUiState(
                threads = listOf(
                    ChatThreadUiModel(
                        id = "wa_alice",
                        packageName = "com.whatsapp",
                        title = "Alice Smith",
                        lastSnippet = "Are we still meeting at 5?",
                        lastTimestamp = System.currentTimeMillis() - 120_000L,
                        unreadCount = 2,
                        hasDeletedMessages = true
                    ),
                    ChatThreadUiModel(
                        id = "wab_acme",
                        packageName = "com.whatsapp.w4b",
                        title = "Acme Supplies",
                        lastSnippet = "Your invoice has been updated.",
                        lastTimestamp = System.currentTimeMillis() - 3600_000L,
                        unreadCount = 0,
                        hasDeletedMessages = false
                    ),
                    ChatThreadUiModel(
                        id = "fb_bob",
                        packageName = "com.facebook.orca",
                        title = "Bob Vance",
                        lastSnippet = "Check out this document",
                        lastTimestamp = System.currentTimeMillis() - 86400_000L,
                        unreadCount = 0,
                        hasDeletedMessages = true
                    )
                )
            ),
            onSearchQueryChange = {},
            onAppFilterSelect = {},
            onToggleOnlyDeleted = {},
            onThreadClick = {},
            onDeleteThread = {}
        )
    }
}
