package ai.ondevice.app.ui.conversationlist

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * Update #7: Chats Screen Redesign
 * - Flat list (no cards)
 * - Title + timestamp only
 * - Delete hidden in context menu (long-press or ⋮)
 * - Context menu: Rename, Star, Delete
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationListScreen(
    viewModel: ConversationListViewModel = hiltViewModel(),
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToNewChat: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val deleteConfirmation by viewModel.deleteConfirmation.collectAsState(initial = null)

    var showDeleteDialog by remember { mutableStateOf(false) }
    var threadToDelete by remember { mutableStateOf<Long?>(null) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var threadToRename by remember { mutableStateOf<ConversationItem?>(null) }
    var renameText by remember { mutableStateOf("") }

    LaunchedEffect(deleteConfirmation) {
        deleteConfirmation?.let {
            threadToDelete = it
            showDeleteDialog = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chats") },
                navigationIcon = {
                    IconButton(onClick = onNavigateToNewChat) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToNewChat,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "New chat")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search Chats") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )

            Box(modifier = Modifier.weight(1f)) {
                when (val state = uiState) {
                    ConversationListUiState.Loading ->
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))

                    ConversationListUiState.Empty ->
                        if (searchQuery.isNotEmpty()) {
                            Text(
                                text = "No results for \"$searchQuery\"",
                                modifier = Modifier.align(Alignment.Center),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            EmptyState(Modifier.align(Alignment.Center))
                        }

                    is ConversationListUiState.Success -> {
                        // Flat list - no date grouping
                        LazyColumn(contentPadding = PaddingValues(vertical = 8.dp)) {
                            items(
                                items = state.conversations,
                                key = { it.thread.id }
                            ) { item ->
                                ConversationListItem(
                                    item = item,
                                    onClick = { onNavigateToDetail(item.thread.id) },
                                    onRename = {
                                        threadToRename = item
                                        renameText = item.thread.title
                                        showRenameDialog = true
                                    },
                                    onStar = { viewModel.toggleStar(item.thread.id) },
                                    onDelete = { viewModel.showDeleteConfirmation(item.thread.id) }
                                )
                            }
                        }
                    }

                    is ConversationListUiState.Error ->
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.align(Alignment.Center)
                        )
                }
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog && threadToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                threadToDelete = null
            },
            title = { Text("Delete Conversation") },
            text = { Text("This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        threadToDelete?.let(viewModel::deleteConversation)
                        showDeleteDialog = false
                        threadToDelete = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        threadToDelete = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Rename dialog
    if (showRenameDialog && threadToRename != null) {
        AlertDialog(
            onDismissRequest = {
                showRenameDialog = false
                threadToRename = null
            },
            title = { Text("Rename Conversation") },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        threadToRename?.let { viewModel.renameConversation(it.thread.id, renameText) }
                        showRenameDialog = false
                        threadToRename = null
                    }
                ) {
                    Text("Rename")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showRenameDialog = false
                        threadToRename = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * Flat list item with context menu (⋮ button and long-press)
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ConversationListItem(
    item: ConversationItem,
    onClick: () -> Unit,
    onRename: () -> Unit,
    onStar: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = { showMenu = true }
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Star indicator (if starred)
        if (item.thread.isStarred) {
            Icon(
                Icons.Default.Star,
                contentDescription = "Starred",
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(8.dp))
        }

        // Title and timestamp
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.thread.title,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = item.formattedTimestamp,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Menu button
        Box {
            IconButton(onClick = { showMenu = true }) {
                Icon(
                    Icons.Default.MoreVert,
                    contentDescription = "More options",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Context menu dropdown
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Rename") },
                    onClick = {
                        showMenu = false
                        onRename()
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Edit, contentDescription = null)
                    }
                )
                DropdownMenuItem(
                    text = { Text(if (item.thread.isStarred) "Unstar" else "Star") },
                    onClick = {
                        showMenu = false
                        onStar()
                    },
                    leadingIcon = {
                        Icon(
                            if (item.thread.isStarred) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = null
                        )
                    }
                )
                DropdownMenuItem(
                    text = { Text("Delete") },
                    onClick = {
                        showMenu = false
                        onDelete()
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                )
            }
        }
    }

    // Divider between items
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        color = MaterialTheme.colorScheme.outlineVariant
    )
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.History,
            contentDescription = "No conversations",
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "No chats yet",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Start a new chat to see your history here",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
