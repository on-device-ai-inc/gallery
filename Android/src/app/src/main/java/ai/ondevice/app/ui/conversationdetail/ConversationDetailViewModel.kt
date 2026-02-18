package ai.ondevice.app.ui.conversationdetail

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ai.ondevice.app.data.ConversationDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConversationDetailViewModel @Inject constructor(
    private val conversationDao: ConversationDao,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private companion object { const val TAG = "ConversationDetailVM" }

    private val threadId: Long = savedStateHandle["threadId"] ?: 0L

    private val _uiState = MutableStateFlow<ConversationDetailUiState>(ConversationDetailUiState.Loading)
    val uiState: StateFlow<ConversationDetailUiState> = _uiState.asStateFlow()

    init { loadConversation() }

    private fun loadConversation() = viewModelScope.launch {
        conversationDao.getThreadById(threadId)?.let { thread ->
            conversationDao.getMessagesForThreadFlow(threadId)
                .catch { e ->
                    Log.e(TAG, "Message flow error", e)
                    _uiState.value = ConversationDetailUiState.Error
                }
                .collect { messages ->
                    _uiState.value = ConversationDetailUiState.Success(thread, messages)
                }
        } ?: run {
            _uiState.value = ConversationDetailUiState.Error
        }
    }
}
