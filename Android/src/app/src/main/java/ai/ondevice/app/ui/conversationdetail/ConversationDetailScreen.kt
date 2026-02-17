package ai.ondevice.app.ui.conversationdetail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationDetailScreen(
    threadId: Long,
    viewModel: ConversationDetailViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onContinueChat: (threadId: Long, modelId: String, taskId: String) -> Unit = { _, _, _ -> }
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(uiState) {
        if (uiState is ConversationDetailUiState.Success) {
            val messages = (uiState as ConversationDetailUiState.Success).messages
            if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    when (val state = uiState) {
                        is ConversationDetailUiState.Success ->
                            Text(
                                state.thread.title,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        else -> Text("Conversation")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            if (uiState is ConversationDetailUiState.Success) {
                val thread = (uiState as ConversationDetailUiState.Success).thread
                FloatingActionButton(
                    onClick = { onContinueChat(threadId, thread.modelId, thread.taskId) },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Continue chat")
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val state = uiState) {
                ConversationDetailUiState.Loading ->
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))

                ConversationDetailUiState.Error ->
                    Text(
                        text = "Failed to load conversation",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )

                is ConversationDetailUiState.Success ->
                    if (state.messages.isEmpty()) {
                        Text(
                            text = "No messages",
                            modifier = Modifier.align(Alignment.Center),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        LazyColumn(
                            state = listState,
                            contentPadding = PaddingValues(vertical = 8.dp),
                            reverseLayout = false
                        ) {
                            items(state.messages, key = { it.id }) { msg ->
                                MessageBubble(
                                    content = msg.content,
                                    isUser = msg.isUser,
                                    timestamp = formatMessageTime(msg.timestamp)
                                )
                            }
                        }
                    }
            }
        }
    }
}

@Composable
private fun MessageBubble(
    content: String,
    isUser: Boolean,
    timestamp: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = if (isUser) 16.dp else 4.dp,
                topEnd = if (isUser) 4.dp else 16.dp,
                bottomStart = 16.dp,
                bottomEnd = 16.dp
            ),
            color = if (isUser) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.surfaceVariant
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .padding(12.dp)
            ) {
                Text(
                    text = content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isUser) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = timestamp,
                    style = MaterialTheme.typography.labelSmall,
                    color = (if (isUser) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant)
                        .copy(alpha = 0.7f),
                    textAlign = TextAlign.End,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

private fun formatMessageTime(epochMilli: Long): String {
    val instant = Instant.ofEpochMilli(epochMilli)
    val dateTime = instant.atZone(ZoneId.systemDefault()).toLocalDateTime()
    val now = Instant.now().atZone(ZoneId.systemDefault()).toLocalDateTime()

    return when (val mins = ChronoUnit.MINUTES.between(dateTime, now)) {
        in 0..1 -> "Now"
        in 2..59 -> "${mins}m"
        else -> "${ChronoUnit.HOURS.between(dateTime, now)}h"
    }
}
