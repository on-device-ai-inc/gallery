package ai.ondevice.app.ui.conversationlist

import ai.ondevice.app.data.ConversationThread

sealed interface ConversationListUiState {
    object Loading : ConversationListUiState
    data class Success(val conversations: List<ConversationItem>) : ConversationListUiState
    object Empty : ConversationListUiState
    data class Error(val message: String) : ConversationListUiState
}

data class ConversationItem(
    val thread: ConversationThread,
    val lastMessage: String?,
    val messageCount: Int,
    val formattedTimestamp: String,
    val dateGroup: String = "" // e.g., "Today", "Yesterday", "This Week", etc.
)
