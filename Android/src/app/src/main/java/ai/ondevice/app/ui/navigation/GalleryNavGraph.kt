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

package ai.ondevice.app.ui.navigation

import androidx.hilt.navigation.compose.hiltViewModel

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.EaseOutExpo
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.zIndex
import androidx.core.os.bundleOf
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import ai.ondevice.app.ui.conversationlist.ConversationListScreen
import ai.ondevice.app.ui.conversationdetail.ConversationDetailScreen
import androidx.navigation.navArgument
import androidx.navigation.navArgument
import ai.ondevice.app.customtasks.common.CustomTaskData
import ai.ondevice.app.customtasks.common.CustomTaskDataForBuiltinTask
import ai.ondevice.app.data.BuiltInTaskId
import ai.ondevice.app.data.ModelDownloadStatusType
import ai.ondevice.app.data.Task
import ai.ondevice.app.data.isBuiltInTask
import ai.ondevice.app.firebaseAnalytics
import ai.ondevice.app.ui.common.ErrorDialog
import ai.ondevice.app.ui.common.ModelPageAppBar
import ai.ondevice.app.ui.common.chat.ModelDownloadStatusInfoPanel
import ai.ondevice.app.ui.common.tos.TosDialog
import ai.ondevice.app.ui.common.tos.TosViewModel
import ai.ondevice.app.ui.modelmanager.ModelInitializationStatusType
import ai.ondevice.app.ui.modelmanager.ModelManager
import ai.ondevice.app.ui.modelmanager.ModelManagerViewModel
import ai.ondevice.app.ui.settings.SettingsScreen
import ai.ondevice.app.ui.settings.CustomInstructionsScreen
import ai.ondevice.app.ui.settings.PrivacyCenterScreen
import ai.ondevice.app.ui.settings.StorageManagementScreen
import ai.ondevice.app.ui.settings.ModelParametersScreen
import ai.ondevice.app.data.DataStoreRepository
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val TAG = "AGGalleryNavGraph"


private const val ROUTE_PLACEHOLDER = "placeholder"
private const val ROUTE_MODEL = "route_model"
private const val ROUTE_CONVERSATION_LIST = "conversation_list"
private const val ROUTE_CONVERSATION_DETAIL = "conversation_detail/{threadId}"
private const val ROUTE_SETTINGS = "settings"  // Epic 5: Settings screen
private const val ROUTE_CUSTOM_INSTRUCTIONS = "custom_instructions"  // Story 8: Custom Instructions
private const val ROUTE_PRIVACY_CENTER = "privacy_center"  // Story 10: Privacy Center
private const val ROUTE_STORAGE_MANAGEMENT = "storage_management"  // Story 9: Storage Management
private const val ROUTE_MODEL_PARAMETERS = "model_parameters"  // Story 7: Model Parameters
private const val ROUTE_MODEL_SELECTION = "model_selection"  // First-launch model selection
private const val ENTER_ANIMATION_DURATION_MS = 500
private val ENTER_ANIMATION_EASING = EaseOutExpo
private const val ENTER_ANIMATION_DELAY_MS = 100

private const val EXIT_ANIMATION_DURATION_MS = 500
private val EXIT_ANIMATION_EASING = EaseOutExpo

private fun enterTween(): FiniteAnimationSpec<IntOffset> {
  return tween(
    ENTER_ANIMATION_DURATION_MS,
    easing = ENTER_ANIMATION_EASING,
    delayMillis = ENTER_ANIMATION_DELAY_MS,
  )
}

private fun exitTween(): FiniteAnimationSpec<IntOffset> {
  return tween(EXIT_ANIMATION_DURATION_MS, easing = EXIT_ANIMATION_EASING)
}

private fun AnimatedContentTransitionScope<*>.slideEnter(): EnterTransition {
  return slideIntoContainer(
    animationSpec = enterTween(),
    towards = AnimatedContentTransitionScope.SlideDirection.Left,
  )
}

private fun AnimatedContentTransitionScope<*>.slideExit(): ExitTransition {
  return slideOutOfContainer(
    animationSpec = exitTween(),
    towards = AnimatedContentTransitionScope.SlideDirection.Right,
  )
}

/** Navigation routes. */
@Composable
fun GalleryNavHost(
  navController: NavHostController,
  modifier: Modifier = Modifier,
  modelManagerViewModel: ModelManagerViewModel,
) {
  val lifecycleOwner = LocalLifecycleOwner.current
  val modelManagerUiState by modelManagerViewModel.uiState.collectAsState()
  val tosViewModel: TosViewModel = hiltViewModel()

  // Option A Flow: TOS → Model Selection → Chat
  var showTosDialog by remember { mutableStateOf(!tosViewModel.getIsTosAccepted()) }
  var hasAutoNavigated by remember { mutableStateOf(false) }

  LaunchedEffect(modelManagerUiState.modelDownloadStatus, showTosDialog) {
    if (!hasAutoNavigated && !modelManagerUiState.loadingModelAllowlist && !showTosDialog) {
      val llmTask = modelManagerViewModel.getTaskById(BuiltInTaskId.LLM_CHAT)

      // Check if any model is downloaded
      val downloadedModel = llmTask?.models?.firstOrNull { model ->
        modelManagerUiState.modelDownloadStatus[model.name]?.status == ModelDownloadStatusType.SUCCEEDED
      }

      if (downloadedModel != null && llmTask != null) {
        // Model downloaded - navigate directly to chat
        hasAutoNavigated = true
        navController.navigate("$ROUTE_MODEL/${llmTask.id}/${downloadedModel.name}")
      } else if (llmTask != null && llmTask.models.isNotEmpty()) {
        // No model downloaded - navigate to model selection (first launch)
        hasAutoNavigated = true
        navController.navigate(ROUTE_MODEL_SELECTION)
      }
    }
  }

  // Track whether app is in foreground.
  DisposableEffect(lifecycleOwner) {
    val observer = LifecycleEventObserver { _, event ->
      when (event) {
        Lifecycle.Event.ON_START,
        Lifecycle.Event.ON_RESUME -> {
          modelManagerViewModel.setAppInForeground(foreground = true)
        }
        Lifecycle.Event.ON_STOP,
        Lifecycle.Event.ON_PAUSE -> {
          modelManagerViewModel.setAppInForeground(foreground = false)
        }
        else -> {
          /* Do nothing for other events */
        }
      }
    }

    lifecycleOwner.lifecycle.addObserver(observer)

    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
  }

  // TOS Dialog (Option A: Show before first launch)
  if (showTosDialog) {
    TosDialog(
      onDismiss = {}, // No dismiss - must accept to continue
      onAccept = {
        tosViewModel.acceptTos()
        showTosDialog = false
      }
    )
  }

  NavHost(
    navController = navController,
    // Start with placeholder - auto-navigation will handle routing
    startDestination = ROUTE_PLACEHOLDER,
    enterTransition = { EnterTransition.None },
    exitTransition = { ExitTransition.None },
    modifier = modifier.zIndex(1f),
  ) {
    // Placeholder root screen
    composable(route = ROUTE_PLACEHOLDER) { Text("") }

    // Epic 5: Settings Screen
    composable(
      route = ROUTE_SETTINGS,
      enterTransition = { slideEnter() },
      exitTransition = { slideExit() },
    ) {
      SettingsScreen(
        onNavigateBack = { navController.navigateUp() },
        modelManagerViewModel = modelManagerViewModel,
        onNavigateToModelSelection = {
          navController.navigate(ROUTE_MODEL_SELECTION)
        },
        onNavigateToCustomInstructions = {
          navController.navigate(ROUTE_CUSTOM_INSTRUCTIONS)
        },
        onNavigateToPrivacyCenter = {
          navController.navigate(ROUTE_PRIVACY_CENTER)
        },
        onNavigateToStorageManagement = {
          navController.navigate(ROUTE_STORAGE_MANAGEMENT)
        },
        onNavigateToModelParameters = {
          navController.navigate(ROUTE_MODEL_PARAMETERS)
        }
      )
    }

    // Story 8: Custom Instructions Screen
    composable(
      route = ROUTE_CUSTOM_INSTRUCTIONS,
      enterTransition = { slideEnter() },
      exitTransition = { slideExit() },
    ) {
      CustomInstructionsScreen(
        onNavigateBack = { navController.navigateUp() }
      )
    }

    // Story 10: Privacy Center Screen
    composable(
      route = ROUTE_PRIVACY_CENTER,
      enterTransition = { slideEnter() },
      exitTransition = { slideExit() },
    ) {
      PrivacyCenterScreen(
        onNavigateBack = { navController.navigateUp() },
        modelManagerViewModel = modelManagerViewModel
      )
    }

    // Story 9: Storage Management Screen
    composable(
      route = ROUTE_STORAGE_MANAGEMENT,
      enterTransition = { slideEnter() },
      exitTransition = { slideExit() },
    ) {
      StorageManagementScreen(
        onNavigateBack = { navController.navigateUp() },
        modelManagerViewModel = modelManagerViewModel
      )
    }

    // Story 7: Model Parameters Screen
    composable(
      route = ROUTE_MODEL_PARAMETERS,
      enterTransition = { slideEnter() },
      exitTransition = { slideExit() },
    ) {
      val selectedModel = modelManagerViewModel.uiState.collectAsState().value.selectedModel
      ModelParametersScreen(
        model = selectedModel,
        onNavigateBack = { navController.navigateUp() },
        onParametersChanged = { temperature, topK, topP, maxTokens ->
          // TODO: Persist model parameters to DataStore or Model config
          // For now, parameters are managed within ModelParametersScreen state
        }
      )
    }

    // Model Selection Screen (First launch onboarding)
    composable(
      route = ROUTE_MODEL_SELECTION,
      enterTransition = { slideEnter() },
      exitTransition = { slideExit() },
    ) {
      ai.ondevice.app.ui.modelselection.ModelSelectionScreen(
        modelManagerViewModel = modelManagerViewModel,
        onModelSelected = { model ->
          // Start download and navigate to chat when complete
          val llmTask = modelManagerViewModel.getTaskById(BuiltInTaskId.LLM_CHAT)
          if (llmTask != null) {
            navController.navigate("$ROUTE_MODEL/${llmTask.id}/${model.name}") {
              // Clear back stack so user can't go back to model selection
              popUpTo(ROUTE_PLACEHOLDER) { inclusive = true }
            }
          }
        }
      )
    }

    // PHASE 3: Conversation List (Update #8: One-step navigation to active chat)
    composable(route = ROUTE_CONVERSATION_LIST) {
      ConversationListScreen(
        onNavigateToDetail = { threadId ->
          // One-step navigation: go back to chat and load the conversation
          // Pass threadId via saved state handle so chat can load it
          navController.previousBackStackEntry?.savedStateHandle?.set("loadThreadId", threadId)
          navController.navigateUp()
        },
        onNavigateToNewChat = {
          navController.navigateUp()
        }
      )
    }

    // PHASE 3: Conversation Detail
    composable(
      route = ROUTE_CONVERSATION_DETAIL,
      arguments = listOf(navArgument("threadId") { type = NavType.LongType })
    ) { backStackEntry ->
      val threadId = backStackEntry.arguments?.getLong("threadId") ?: 0L
      ConversationDetailScreen(
        threadId = threadId,
        onNavigateBack = { navController.navigateUp() },
        onContinueChat = { loadThreadId, modelId, taskId ->
          // Navigate to the chat screen and pass threadId to load conversation
          navController.navigate("$ROUTE_MODEL/$taskId/$modelId") {
            popUpTo(ROUTE_CONVERSATION_LIST) { inclusive = false }
          }
          // Set loadThreadId in savedStateHandle so chat screen can load the conversation
          navController.currentBackStackEntry?.savedStateHandle?.set("loadThreadId", loadThreadId)
        }
      )
    }

    composable(
      route = "$ROUTE_MODEL/{taskId}/{modelName}",
      arguments =
        listOf(
          navArgument("taskId") { type = NavType.StringType },
          navArgument("modelName") { type = NavType.StringType },
        ),
      enterTransition = { slideEnter() },
      exitTransition = { slideExit() },
    ) { backStackEntry ->
      val modelName = backStackEntry.arguments?.getString("modelName") ?: ""
      val taskId = backStackEntry.arguments?.getString("taskId") ?: ""

      // Update #8: Observe loadThreadId from conversation history navigation
      val loadThreadId by backStackEntry.savedStateHandle.getStateFlow<Long?>("loadThreadId", null).collectAsState()

      modelManagerViewModel.getModelByName(name = modelName)?.let { model ->
        modelManagerViewModel.selectModel(model)

        val customTask = modelManagerViewModel.getCustomTaskByTaskId(id = taskId)
        if (customTask != null) {
          if (isBuiltInTask(customTask.task.id)) {
            customTask.MainScreen(
              data =
                CustomTaskDataForBuiltinTask(
                  modelManagerViewModel = modelManagerViewModel,
                  onNavUp = { navController.navigateUp() },
                  onNavigateToConversationHistory = {
                    Log.d(TAG, "onNavigateToConversationHistory callback triggered, navigating to $ROUTE_CONVERSATION_LIST")
                    navController.navigate(ROUTE_CONVERSATION_LIST)
                  },
                  onNavigateToSettings = {
                    Log.d(TAG, "onNavigateToSettings callback triggered, navigating to $ROUTE_SETTINGS")
                    navController.navigate(ROUTE_SETTINGS)
                  },
                  loadConversationId = loadThreadId,
                  onConversationLoaded = {
                    backStackEntry.savedStateHandle["loadThreadId"] = null
                  },
                )
            )
          } else {
            var disableAppBarControls by remember { mutableStateOf(false) }
            CustomTaskScreen(
              task = customTask.task,
              modelManagerViewModel = modelManagerViewModel,
              onNavigateUp = { navController.navigateUp() },
              disableAppBarControls = disableAppBarControls,
            ) { bottomPadding ->
              customTask.MainScreen(
                data =
                  CustomTaskData(
                    modelManagerViewModel = modelManagerViewModel,
                    bottomPadding = bottomPadding,
                    setAppBarControlsDisabled = { disableAppBarControls = it },
                  )
              )
            }
          }
        }
      }
    }
  }

  // Handle back button at root level to cleanly exit app
  val activity = androidx.activity.compose.LocalActivity.current
  val currentRoute = navController.currentBackStackEntry?.destination?.route
  BackHandler(
    enabled = currentRoute == ROUTE_PLACEHOLDER
  ) {
    activity?.finish()
  }

  // Handle incoming intents for deep links
  val intent = androidx.activity.compose.LocalActivity.current?.intent
  val data = intent?.data
  if (data != null) {
    intent.data = null
    Log.d(TAG, "navigation link clicked: $data")
    if (data.toString().startsWith("ai.ondevice.app://model/")) {
      if (data.pathSegments.size >= 2) {
        val taskId = data.pathSegments.get(data.pathSegments.size - 2)
        val modelName = data.pathSegments.last()
        modelManagerViewModel.getModelByName(name = modelName)?.let { model ->
          navController.navigate("$ROUTE_MODEL/${taskId}/${model.name}")
        }
      } else {
        Log.e(TAG, "Malformed deep link URI received: $data")
      }
    }
  }
}

@Composable
private fun CustomTaskScreen(
  task: Task,
  modelManagerViewModel: ModelManagerViewModel,
  disableAppBarControls: Boolean,
  onNavigateUp: () -> Unit,
  content: @Composable (bottomPadding: Dp) -> Unit,
) {
  val modelManagerUiState by modelManagerViewModel.uiState.collectAsState()
  val selectedModel = modelManagerUiState.selectedModel
  val scope = rememberCoroutineScope()
  val context = LocalContext.current
  var navigatingUp by remember { mutableStateOf(false) }
  var showErrorDialog by remember { mutableStateOf(false) }

  val handleNavigateUp = {
    navigatingUp = true
    onNavigateUp()

    // clean up all models.
    scope.launch(Dispatchers.Default) {
      for (model in task.models) {
        modelManagerViewModel.cleanupModel(context = context, task = task, model = model)
      }
    }
  }

  // Handle system's edge swipe.
  BackHandler { handleNavigateUp() }

  // Initialize model when model/download state changes.
  val curDownloadStatus = modelManagerUiState.modelDownloadStatus[selectedModel.name]
  LaunchedEffect(curDownloadStatus, selectedModel.name) {
    if (!navigatingUp) {
      if (curDownloadStatus?.status == ModelDownloadStatusType.SUCCEEDED) {
        Log.d(
          TAG,
          "Initializing model '${selectedModel.name}' from CustomTaskScreen launched effect",
        )
        modelManagerViewModel.initializeModel(context, task = task, model = selectedModel)
      }
    }
  }

  val modelInitializationStatus = modelManagerUiState.modelInitializationStatus[selectedModel.name]
  LaunchedEffect(modelInitializationStatus) {
    showErrorDialog = modelInitializationStatus?.status == ModelInitializationStatusType.ERROR
  }

  Scaffold(
    topBar = {
      ModelPageAppBar(
        task = task,
        model = selectedModel,
        modelManagerViewModel = modelManagerViewModel,
        inProgress = disableAppBarControls,
        modelPreparing = disableAppBarControls,
        canShowResetSessionButton = false,
        onConfigChanged = { _, _ -> },
        onMenuClicked = { handleNavigateUp() },
        onModelSelected = { prevModel, newSelectedModel ->
          scope.launch(Dispatchers.Default) {
            // Clean up prev model.
            if (prevModel.name != newSelectedModel.name) {
              modelManagerViewModel.cleanupModel(context = context, task = task, model = prevModel)
            }

            // Update selected model.
            modelManagerViewModel.selectModel(model = newSelectedModel)
          }
        },
      )
    }
  ) { innerPadding ->
    Box(
      modifier =
        Modifier.padding(
          top = innerPadding.calculateTopPadding(),
          start = innerPadding.calculateStartPadding(LocalLayoutDirection.current),
          end = innerPadding.calculateStartPadding(LocalLayoutDirection.current),
        )
    ) {
      val curModelDownloadStatus = modelManagerUiState.modelDownloadStatus[selectedModel.name]
      AnimatedContent(
        targetState = curModelDownloadStatus?.status == ModelDownloadStatusType.SUCCEEDED
      ) { targetState ->
        when (targetState) {
          // Main UI when model is downloaded.
          true -> content(innerPadding.calculateBottomPadding())
          // Model download
          false ->
            ModelDownloadStatusInfoPanel(
              model = selectedModel,
              task = task,
              modelManagerViewModel = modelManagerViewModel,
            )
        }
      }
    }
  }

  if (showErrorDialog) {
    ErrorDialog(
      error = modelInitializationStatus?.error ?: "",
      onDismiss = { showErrorDialog = false },
    )
  }
}
