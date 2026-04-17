package ai.ondevice.app.ui.conversationlist

import ai.ondevice.app.firebaseAnalytics
import android.util.Log
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ai.ondevice.app.data.ConversationDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import ai.ondevice.app.firebaseAnalytics
import androidx.core.os.bundleOf

@HiltViewModel
class ConversationListViewModel @Inject constructor(
    private val conversationDao: ConversationDao
) : ViewModel() {

    private companion object { const val TAG = "ConversationListVM" }

    private val _uiState = MutableStateFlow<ConversationListUiState>(ConversationListUiState.Loading)
    val uiState: StateFlow<ConversationListUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _deleteConfirmation = MutableSharedFlow<Long>()
    val deleteConfirmation: SharedFlow<Long> = _deleteConfirmation.asSharedFlow()

    init { loadConversations() }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            loadConversations()
        } else {
            searchConversations(query)
        }
    }

    private fun searchConversations(query: String) = viewModelScope.launch {
        try {
            val results = conversationDao.searchThreadsWithLastMessage(query)
            if (results.isEmpty()) {
                _uiState.value = ConversationListUiState.Empty
            } else {
                val items = results.map { result ->
                    ConversationItem(
                        thread = result.toConversationThread(),
                        lastMessage = result.lastMessageContent?.take(50),
                        messageCount = result.messageCount,
                        formattedTimestamp = formatTimestamp(result.updatedAt),
                        dateGroup = getDateGroup(result.updatedAt)
                    )
                }
                _uiState.value = ConversationListUiState.Success(items)
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            firebaseAnalytics?.logEvent(
                "error_occurred",
                bundleOf(
                    "error_type" to e::class.simpleName,
                    "source_class" to "ConversationListViewModel",
                    "error_message" to e.message.orEmpty()
                )
            )
            Log.e(TAG, "Search error", e)
            _uiState.value = ConversationListUiState.Error(e.message ?: "Search failed")
        }
    }

    private fun loadConversations() = viewModelScope.launch {
        conversationDao.getAllThreadsWithLastMessageFlow()
            .map { results ->
                when {
                    results.isEmpty() -> ConversationListUiState.Empty
                    else -> {
                        // Update #7: Sort starred first, then by updatedAt
                        val items = results
                            .sortedWith(compareByDescending<ai.ondevice.app.data.ThreadWithLastMessage> { it.isStarred }
                                .thenByDescending { it.updatedAt })
                            .map { result ->
                            ConversationItem(
                                thread = result.toConversationThread(),
                                lastMessage = result.lastMessageContent?.take(50),
                                messageCount = result.messageCount,
                                formattedTimestamp = formatTimestamp(result.updatedAt),
                                dateGroup = getDateGroup(result.updatedAt)
                            )
                        }
                        ConversationListUiState.Success(items)
                    }
                }
            }
            .catch { e ->
                if (e is CancellationException) throw e
                firebaseAnalytics?.logEvent(
                    "error_occurred",
                    bundleOf(
                        "error_type" to e::class.simpleName,
                        "source_class" to "ConversationListViewModel",
                        "error_message" to e.message.orEmpty()
                    )
                )
                Log.e(TAG, "Load error", e)
                emit(ConversationListUiState.Error(e.message ?: "Unknown error"))
            }
            .collect { _uiState.value = it }
    }

    fun deleteConversation(threadId: Long) = viewModelScope.launch {
        try {
            conversationDao.deleteThread(threadId)
            Log.d(TAG, "Deleted thread $threadId")
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(TAG, "Delete error", e)
            _uiState.value = ConversationListUiState.Error("Failed to delete conversation")
        }
    }

    fun showDeleteConfirmation(threadId: Long) = viewModelScope.launch {
        _deleteConfirmation.emit(threadId)
    }

    /** Update #7: Toggle star status */
    fun toggleStar(threadId: Long) = viewModelScope.launch {
        try {
            val thread = conversationDao.getThreadById(threadId)
            if (thread != null) {
                conversationDao.updateStarred(threadId, !thread.isStarred)
                Log.d(TAG, "Toggled star for thread $threadId to ${!thread.isStarred}")
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(TAG, "Toggle star error", e)
        }
    }

    /** Update #7: Rename conversation */
    fun renameConversation(threadId: Long, newTitle: String) = viewModelScope.launch {
        try {
            conversationDao.updateTitle(threadId, newTitle)
            Log.d(TAG, "Renamed thread $threadId to $newTitle")
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(TAG, "Rename error", e)
        }
    }

    private fun formatTimestamp(epochMilli: Long): String {
        val instant = Instant.ofEpochMilli(epochMilli)
        val dateTime = instant.atZone(ZoneId.systemDefault()).toLocalDateTime()
        val now = Instant.now().atZone(ZoneId.systemDefault()).toLocalDateTime()

        return when (val days = ChronoUnit.DAYS.between(dateTime, now)) {
            0L -> {
                val mins = ChronoUnit.MINUTES.between(dateTime, now)
                when {
                    mins < 1 -> "Just now"
                    mins < 60 -> "${mins}m ago"
                    else -> "${ChronoUnit.HOURS.between(dateTime, now)}h ago"
                }
            }
            1L -> "Yesterday"
            in 2..6 -> "${days} days ago"
            else -> dateTime.toLocalDate().toString()
        }
    }

    private fun getDateGroup(epochMilli: Long): String {
        val instant = Instant.ofEpochMilli(epochMilli)
        val dateTime = instant.atZone(ZoneId.systemDefault()).toLocalDateTime()
        val now = Instant.now().atZone(ZoneId.systemDefault()).toLocalDateTime()

        return when (val days = ChronoUnit.DAYS.between(dateTime, now)) {
            0L -> "Today"
            1L -> "Yesterday"
            in 2..6 -> "This Week"
            in 7..29 -> "This Month"
            else -> dateTime.month.name.lowercase().replaceFirstChar { it.uppercase() } + " " + dateTime.year
        }
    }
}
