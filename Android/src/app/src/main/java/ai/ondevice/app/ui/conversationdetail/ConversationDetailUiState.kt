package ai.ondevice.app.ui.conversationdetail

import ai.ondevice.app.data.ConversationMessage
import ai.ondevice.app.data.ConversationThread

sealed interface ConversationDetailUiState {
    object Loading : ConversationDetailUiState
    data class Success(val thread: ConversationThread, val messages: List<ConversationMessage>) : ConversationDetailUiState
    object Error : ConversationDetailUiState
}
