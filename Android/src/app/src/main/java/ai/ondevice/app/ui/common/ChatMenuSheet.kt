/*
 * Copyright 2025 Google LLC
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
package ai.ondevice.app.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import android.util.Log
import ai.ondevice.app.R
import ai.ondevice.app.data.ConversationThread
import ai.ondevice.app.ui.theme.customColors
import kotlinx.coroutines.launch

private const val TAG = "AGChatMenuSheet"

/**
 * Gemini-style chat menu drawer (slides from left)
 * Shows search, new chat, and recent chat history
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatMenuSheet(
    onDismiss: () -> Unit,
    onNewChat: () -> Unit,
    onViewConversationHistory: () -> Unit = {},
    onRecentChatClicked: (Long) -> Unit = {},
    onNavigateToSettings: () -> Unit = {},  // Epic 5: Settings navigation
    recentConversations: List<ConversationThread> = emptyList(),
    conversationCount: Int = 0,
    modifier: Modifier = Modifier
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Open)
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val scope = rememberCoroutineScope()


    
    LaunchedEffect(drawerState.isClosed) {
        if (drawerState.isClosed) {
            onDismiss()
        }
    }

    
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(screenWidth * 0.8f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Header - simplified to just "OnDevice"
                    Text(
                        text = "OnDevice",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // New chat row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                onNewChat()
                                scope.launch {
                                    drawerState.close()
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = "New chat",
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "New chat",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    // Chats button (renamed from "View conversation history")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                Log.d(TAG, "Chats clicked - navigating to conversation history")
                                onViewConversationHistory()
                                scope.launch {
                                    drawerState.close()
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = "Chats",
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Chats",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Recents section header
                    Text(
                        text = "Recents",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )

                    // Recent conversations list (up to 10)
                    if (recentConversations.isEmpty()) {
                        Text(
                            text = "No recent chats",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f)
                        ) {
                            items(recentConversations.take(10)) { conversation ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            onRecentChatClicked(conversation.id)
                                            scope.launch {
                                                drawerState.close()
                                            }
                                        }
                                        .padding(horizontal = 12.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = conversation.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }

                    if (recentConversations.isEmpty()) {
                        Spacer(modifier = Modifier.weight(1f))
                    }

                    // Epic 5: Settings entry at bottom of drawer
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                onNavigateToSettings()
                                scope.launch {
                                    drawerState.close()
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = "Settings",
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Settings",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        },
        content = {}
    )

}
