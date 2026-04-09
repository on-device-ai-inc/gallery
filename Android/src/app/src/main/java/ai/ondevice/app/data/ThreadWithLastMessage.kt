package ai.ondevice.app.data

/**
 * Projection class for fetching threads with their last message content
 * and message count in a single query, avoiding N+1 queries.
 */
data class ThreadWithLastMessage(
    val id: Long,
    val title: String,
    val modelId: String,
    val taskId: String,
    val createdAt: Long,
    val updatedAt: Long,
    val isStarred: Boolean,
    val lastMessageContent: String?,
    val messageCount: Int
) {
    fun toConversationThread(): ConversationThread = ConversationThread(
        id = id,
        title = title,
        modelId = modelId,
        taskId = taskId,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isStarred = isStarred
    )
}
