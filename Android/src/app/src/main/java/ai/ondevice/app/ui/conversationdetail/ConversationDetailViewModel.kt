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
import ai.ondevice.app.firebaseAnalytics
import androidx.core.os.bundleOf
import kotlinx.coroutines.CancellationException

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
                    if (e is CancellationException) throw e
                    firebaseAnalytics?.logEvent(
                        "error_occurred",
                        bundleOf(
                            "error_type" to e::class.simpleName,
                            "source_class" to "ConversationDetailViewModel",
                            "error_message" to e.message.orEmpty()
                        )
                    )
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
