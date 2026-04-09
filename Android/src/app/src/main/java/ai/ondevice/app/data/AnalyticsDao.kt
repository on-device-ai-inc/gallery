/*
 * Copyright 2025 On Device AI Inc.
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

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface AnalyticsDao {
    @Insert
    suspend fun insert(event: AnalyticsEvent)

    @Query("SELECT * FROM analytics_events ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecent(limit: Int = 100): List<AnalyticsEvent>

    @Query("DELETE FROM analytics_events")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM analytics_events WHERE event = :eventType")
    suspend fun countByType(eventType: String): Int
}
