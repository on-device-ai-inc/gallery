/*
 * Copyright 2025-2026 On Device AI Inc. All rights reserved.
 * Modifications are proprietary and confidential.
 *
 * Originally Copyright 2025 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ai.ondevice.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Stores running summary for compacted conversations.
 * Used to maintain context beyond token limits via summarization.
 */
@Entity(tableName = "conversation_state")
data class ConversationState(
    @PrimaryKey val threadId: Long,
    val runningSummary: String,
    val turnsSummarized: Int,
    val lastCompactionTime: Long
)
