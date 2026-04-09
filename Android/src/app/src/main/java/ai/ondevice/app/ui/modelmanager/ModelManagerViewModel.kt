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

package ai.ondevice.app.ui.modelmanager

import android.content.Context
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.compose.runtime.Stable
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ai.ondevice.app.AppLifecycleProvider
import ai.ondevice.app.BuildConfig
import ai.ondevice.app.R
import ai.ondevice.app.common.ProjectConfig
import ai.ondevice.app.common.getJsonResponse
import ai.ondevice.app.customtasks.common.CustomTask
import ai.ondevice.app.data.Accelerator
import ai.ondevice.app.data.BuiltInTaskId
import ai.ondevice.app.data.Config
import ai.ondevice.app.data.DataStoreRepository
import ai.ondevice.app.data.DownloadRepository
import ai.ondevice.app.data.EMPTY_MODEL
import ai.ondevice.app.data.IMPORTS_DIR
import ai.ondevice.app.data.Model
import ai.ondevice.app.data.ModelAllowlist
import ai.ondevice.app.data.ModelDownloadStatus
import ai.ondevice.app.data.ModelDownloadStatusType
import ai.ondevice.app.data.ModelRuntimeStateManager
import ai.ondevice.app.data.TMP_FILE_EXT
import ai.ondevice.app.data.Task
import ai.ondevice.app.data.createLlmChatConfigs
import ai.ondevice.app.proto.AccessTokenData
import ai.ondevice.app.proto.ImportedModel
import ai.ondevice.app.proto.Theme
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.ResponseTypeValues

private const val TAG = "AGModelManagerViewModel"
private const val TEXT_INPUT_HISTORY_MAX_SIZE = 50
private const val MODEL_ALLOWLIST_FILENAME = "model_allowlist.json"
private const val MODEL_ALLOWLIST_TEST_FILENAME = "model_allowlist_test.json"

data class ModelInitializationStatus(
  val status: ModelInitializationStatusType,
  var error: String = "",
)

enum class ModelInitializationStatusType {
  NOT_INITIALIZED,
  INITIALIZING,
  INITIALIZED,
  ERROR,
}

enum class TokenStatus {
  NOT_STORED,
  EXPIRED,
  NOT_EXPIRED,
}

enum class TokenRequestResultType {
  FAILED,
  SUCCEEDED,
  USER_CANCELLED,
}

data class TokenStatusAndData(val status: TokenStatus, val data: AccessTokenData?)

data class TokenRequestResult(val status: TokenRequestResultType, val errorMessage: String? = null)

data class ModelManagerUiState(
  /** A list of tasks available in the application. */
  val tasks: List<Task>,

  /** A map that tracks the download status of each model, indexed by model name. */
  val modelDownloadStatus: Map<String, ModelDownloadStatus>,

  /** A map that tracks the initialization status of each model, indexed by model name. */
  val modelInitializationStatus: Map<String, ModelInitializationStatus>,

  /** Whether the app is loading and processing the model allowlist. */
  val loadingModelAllowlist: Boolean = true,

  /** The error message when loading the model allowlist. */
  val loadingModelAllowlistError: String = "",

  /** The currently selected model. */
  val selectedModel: Model = EMPTY_MODEL,

  /** The history of text inputs entered by the user. */
  val textInputHistory: List<String> = listOf(),
  val configValuesUpdateTrigger: Long = 0L,
) {
  fun isModelInitialized(model: Model): Boolean {
    return modelInitializationStatus[model.name]?.status ==
      ModelInitializationStatusType.INITIALIZED
  }

  fun isModelInitializing(model: Model): Boolean {
    return modelInitializationStatus[model.name]?.status ==
      ModelInitializationStatusType.INITIALIZING
  }
}

/**
 * ViewModel responsible for managing models, their download status, and initialization.
 *
 * This ViewModel handles model-related operations such as downloading, deleting, initializing, and
 * cleaning up models. It also manages the UI state for model management, including the list of
 * tasks, models, download statuses, and initialization statuses.
 */
@Stable
@HiltViewModel
open class ModelManagerViewModel
@Inject
constructor(
  private val downloadRepository: DownloadRepository,
  private val dataStoreRepository: DataStoreRepository,
  private val lifecycleProvider: AppLifecycleProvider,
  private val customTasks: Set<@JvmSuppressWildcards CustomTask>,
  @ApplicationContext private val context: Context,
) : ViewModel() {
  private val externalFilesDir = context.getExternalFilesDir(null)
  protected val _uiState = MutableStateFlow(createEmptyUiState())
  val uiState = _uiState.asStateFlow()

  val authService = AuthorizationService(context)
  @Volatile var curAccessToken: String = ""

  override fun onCleared() {
    super.onCleared()
    authService.dispose()
  }

  fun getTaskById(id: String): Task? {
    return uiState.value.tasks.find { it.id == id }
  }

  fun getTasksByIds(ids: Set<String>): List<Task> {
    return uiState.value.tasks.filter { ids.contains(it.id) }
  }

  fun getCustomTaskByTaskId(id: String): CustomTask? {
    return customTasks.find { it.task.id == id }
  }

  fun getModelByName(name: String): Model? {
    for (task in uiState.value.tasks) {
      for (model in task.models) {
        if (model.name == name) {
          return model
        }
      }
    }
    return null
  }

  fun processTasks() {
    val curTasks = customTasks.map { it.task }
    for (task in curTasks) {
      for (model in task.models) {
        model.preProcess()
      }
      // Move the model that is best for this task to the front.
      val bestModel = task.models.find { it.bestForTaskIds.contains(task.id) }
      if (bestModel != null) {
        task.models.remove(bestModel)
        task.models.add(0, bestModel)
      }
    }
  }

  fun updateConfigValuesUpdateTrigger() {
    _uiState.update { it.copy(configValuesUpdateTrigger = System.currentTimeMillis()) }
  }

  fun selectModel(model: Model) {
    _uiState.update { it.copy(selectedModel = model) }
  }

  fun downloadModel(task: Task, model: Model) {
    // Update status.
    setDownloadStatus(
      curModel = model,
      status = ModelDownloadStatus(status = ModelDownloadStatusType.IN_PROGRESS),
    )

    // Delete the model files first.
    deleteModel(task = task, model = model)

    // Start to send download request.
    downloadRepository.downloadModel(
      task = task,
      model = model,
      onStatusUpdated = this::setDownloadStatus,
    )
  }

  fun cancelDownloadModel(task: Task, model: Model) {
    downloadRepository.cancelDownloadModel(model)
    deleteModel(task = task, model = model)
  }

  fun deleteModel(task: Task, model: Model) {
    if (model.imported) {
      deleteFileFromExternalFilesDir(model.downloadFileName)
    } else {
      deleteDirFromExternalFilesDir(model.normalizedName)
    }

    // Delete model from the list if model is imported as a local model.
    if (model.imported) {
      for (curTask in uiState.value.tasks) {
        val index = curTask.models.indexOf(model)
        if (index >= 0) {
          curTask.models.removeAt(index)
        }
        curTask.updateTrigger.value = System.currentTimeMillis()
      }

      // Update data store.
      val importedModels = dataStoreRepository.readImportedModels().toMutableList()
      val importedModelIndex = importedModels.indexOfFirst { it.fileName == model.name }
      if (importedModelIndex >= 0) {
        importedModels.removeAt(importedModelIndex)
      }
      dataStoreRepository.saveImportedModels(importedModels = importedModels)
    }

    // Update model download status to NotDownloaded.
    _uiState.update { current ->
      val curModelDownloadStatus = current.modelDownloadStatus.toMutableMap()
      curModelDownloadStatus[model.name] =
        ModelDownloadStatus(status = ModelDownloadStatusType.NOT_DOWNLOADED)
      if (model.imported) {
        curModelDownloadStatus.remove(model.name)
      }
      current.copy(
        modelDownloadStatus = curModelDownloadStatus,
        tasks = current.tasks.toList(),
      )
    }
  }

  fun initializeModel(context: Context, task: Task, model: Model, force: Boolean = false) {
    viewModelScope.launch(Dispatchers.Default) {
      // Skip if initialized already.
      if (
        !force &&
          uiState.value.modelInitializationStatus[model.name]?.status ==
            ModelInitializationStatusType.INITIALIZED
      ) {
        Log.d(TAG, "Model '${model.name}' has been initialized. Skipping.")
        return@launch
      }

      // Atomic check-and-set to prevent TOCTOU race between coroutines.
      var shouldSkip = false
      ModelRuntimeStateManager.update(model.name) { state ->
        if (state.initializing) {
          shouldSkip = true
          state.copy(cleanUpAfterInit = false)
        } else {
          state.copy(initializing = true)
        }
      }
      if (shouldSkip) {
        Log.d(TAG, "Model '${model.name}' is being initialized. Skipping.")
        return@launch
      }

      // Clean up any existing instance before initializing.
      // Only call cleanupModel if an instance actually exists. If instance is null and we call
      // cleanupModel now, it will see initializing=true (set above) and incorrectly schedule a
      // cleanup of the model we're about to create (cleanUpAfterInit=true), which causes the model
      // to destroy itself the moment it finishes loading.
      if (ModelRuntimeStateManager.getValue(model.name).instance != null) {
        cleanupModel(context = context, task = task, model = model)
      }

      Log.d(TAG, "Initializing model '${model.name}'...")

      // Show initializing status after a delay. When the delay expires, check if the model has
      // been initialized or not. If so, skip.
      launch {
        delay(500)
        val state = ModelRuntimeStateManager.getValue(model.name)
        if (state.instance == null && state.initializing) {
          updateModelInitializationStatus(
            model = model,
            status = ModelInitializationStatusType.INITIALIZING,
          )
        }
      }

      val onDone: (error: String) -> Unit = { error ->
        ModelRuntimeStateManager.update(model.name) { it.copy(initializing = false) }
        val stateAfterInit = ModelRuntimeStateManager.getValue(model.name)
        if (stateAfterInit.instance != null) {
          Log.d(TAG, "Model '${model.name}' initialized successfully")
          updateModelInitializationStatus(
            model = model,
            status = ModelInitializationStatusType.INITIALIZED,
          )
          if (stateAfterInit.cleanUpAfterInit) {
            Log.d(TAG, "Model '${model.name}' needs cleaning up after init.")
            cleanupModel(context = context, task = task, model = model)
          }
        } else if (error.isNotEmpty()) {
          Log.d(TAG, "Model '${model.name}' failed to initialize")
          updateModelInitializationStatus(
            model = model,
            status = ModelInitializationStatusType.ERROR,
            error = error,
          )
        }
      }

      // Call the model initialization function.
      getCustomTaskByTaskId(id = task.id)
        ?.initializeModelFn(
          context = context,
          coroutineScope = viewModelScope,
          model = model,
          onDone = onDone,
        )
    }
  }

  fun cleanupModel(context: Context, task: Task, model: Model) {
    val runtimeState = ModelRuntimeStateManager.getValue(model.name)
    if (runtimeState.instance != null) {
      ModelRuntimeStateManager.update(model.name) { it.copy(cleanUpAfterInit = false) }
      Log.d(TAG, "Cleaning up model '${model.name}'...")
      val onDone: () -> Unit = {
        ModelRuntimeStateManager.update(model.name) {
          it.copy(instance = null, initializing = false)
        }
        updateModelInitializationStatus(
          model = model,
          status = ModelInitializationStatusType.NOT_INITIALIZED,
        )
        Log.d(TAG, "Clean up model '${model.name}' done")
      }
      getCustomTaskByTaskId(id = task.id)
        ?.cleanUpModelFn(
          context = context,
          coroutineScope = viewModelScope,
          model = model,
          onDone = onDone,
        )
    } else {
      // When model is being initialized and we are trying to clean it up at same time, we mark it
      // to clean up and it will be cleaned up after initialization is done.
      if (runtimeState.initializing) {
        Log.d(
          TAG,
          "Model '${model.name}' is still initializing.. Will clean up after it is done initializing",
        )
        ModelRuntimeStateManager.update(model.name) { it.copy(cleanUpAfterInit = true) }
      }
    }
  }

  fun setDownloadStatus(curModel: Model, status: ModelDownloadStatus) {
    // Delete downloaded file if status is failed or not_downloaded.
    if (
      status.status == ModelDownloadStatusType.FAILED ||
        status.status == ModelDownloadStatusType.NOT_DOWNLOADED
    ) {
      deleteFileFromExternalFilesDir(curModel.downloadFileName)
    }

    // Update model download progress.
    _uiState.update { current ->
      val curModelDownloadStatus = current.modelDownloadStatus.toMutableMap()
      curModelDownloadStatus[curModel.name] = status
      current.copy(modelDownloadStatus = curModelDownloadStatus)
    }
  }

  fun addTextInputHistory(text: String) {
    _uiState.update { current ->
      if (current.textInputHistory.indexOf(text) < 0) {
        val newHistory = current.textInputHistory.toMutableList()
        newHistory.add(0, text)
        if (newHistory.size > TEXT_INPUT_HISTORY_MAX_SIZE) {
          newHistory.removeAt(newHistory.size - 1)
        }
        current.copy(textInputHistory = newHistory)
      } else {
        // Promote: move existing item to front
        val index = current.textInputHistory.indexOf(text)
        val newHistory = current.textInputHistory.toMutableList()
        newHistory.removeAt(index)
        newHistory.add(0, text)
        current.copy(textInputHistory = newHistory)
      }
    }
    dataStoreRepository.saveTextInputHistory(_uiState.value.textInputHistory)
  }

  fun promoteTextInputHistoryItem(text: String) {
    _uiState.update { current ->
      val index = current.textInputHistory.indexOf(text)
      if (index >= 0) {
        val newHistory = current.textInputHistory.toMutableList()
        newHistory.removeAt(index)
        newHistory.add(0, text)
        current.copy(textInputHistory = newHistory)
      } else {
        current
      }
    }
    dataStoreRepository.saveTextInputHistory(_uiState.value.textInputHistory)
  }

  fun deleteTextInputHistory(text: String) {
    _uiState.update { current ->
      val index = current.textInputHistory.indexOf(text)
      if (index >= 0) {
        val newHistory = current.textInputHistory.toMutableList()
        newHistory.removeAt(index)
        current.copy(textInputHistory = newHistory)
      } else {
        current
      }
    }
    dataStoreRepository.saveTextInputHistory(_uiState.value.textInputHistory)
  }

  fun clearTextInputHistory() {
    _uiState.update { it.copy(textInputHistory = mutableListOf()) }
    dataStoreRepository.saveTextInputHistory(_uiState.value.textInputHistory)
  }

  fun readThemeOverride(): Theme {
    return dataStoreRepository.readTheme()
  }

  fun saveThemeOverride(theme: Theme) {
    dataStoreRepository.saveTheme(theme = theme)
  }

  fun getModelUrlResponse(model: Model, accessToken: String? = null): Int {
    var connection: HttpURLConnection? = null
    try {
      val url = URL(model.url)
      // Security: Validate URL scheme and host before connecting
      if (url.protocol != "https") {
        Log.e(TAG, "Insecure protocol rejected: ${url.protocol}")
        return -1
      }
      val trustedHosts = setOf("huggingface.co", "cdn-lfs.huggingface.co", "cdn-lfs-us-1.huggingface.co")
      if (trustedHosts.none { url.host == it || url.host.endsWith(".$it") }) {
        Log.e(TAG, "Untrusted host rejected")
        return -1
      }
      connection = url.openConnection() as HttpURLConnection
      if (accessToken != null) {
        connection.setRequestProperty("Authorization", "Bearer $accessToken")
      }
      connection.connect()

      // Report the result.
      return connection.responseCode
    } catch (e: Exception) {
      if (e is CancellationException) throw e
      Log.e(TAG, "Error checking model URL: $e")
      return -1
    } finally {
      connection?.disconnect()
    }
  }

  fun addImportedLlmModel(info: ImportedModel) {
    Log.d(TAG, "adding imported llm model: $info")

    // Create model.
    val model = createModelFromImportedModelInfo(info = info)

    for (task in
      getTasksByIds(
        ids =
          setOf(
            BuiltInTaskId.LLM_CHAT,
            BuiltInTaskId.LLM_ASK_IMAGE,
            BuiltInTaskId.LLM_ASK_AUDIO,
            BuiltInTaskId.LLM_PROMPT_LAB,
          )
      )) {
      // Remove duplicated imported model if existed.
      val modelIndex = task.models.indexOfFirst { info.fileName == it.name && it.imported }
      if (modelIndex >= 0) {
        Log.d(TAG, "duplicated imported model found in task. Removing it first")
        task.models.removeAt(modelIndex)
      }
      if (
        (task.id == BuiltInTaskId.LLM_ASK_IMAGE && model.llmSupportImage) ||
          (task.id == BuiltInTaskId.LLM_ASK_AUDIO && model.llmSupportAudio) ||
          (task.id != BuiltInTaskId.LLM_ASK_IMAGE && task.id != BuiltInTaskId.LLM_ASK_AUDIO)
      ) {
        task.models.add(model)
      }
      task.updateTrigger.value = System.currentTimeMillis()
    }

    // Update ui state with initial status and states.
    _uiState.update { current ->
      val modelDownloadStatus = current.modelDownloadStatus.toMutableMap()
      val modelInstances = current.modelInitializationStatus.toMutableMap()
      modelDownloadStatus[model.name] =
        ModelDownloadStatus(
          status = ModelDownloadStatusType.SUCCEEDED,
          receivedBytes = info.fileSize,
          totalBytes = info.fileSize,
        )
      modelInstances[model.name] =
        ModelInitializationStatus(status = ModelInitializationStatusType.NOT_INITIALIZED)
      current.copy(
        tasks = current.tasks.toList(),
        modelDownloadStatus = modelDownloadStatus,
        modelInitializationStatus = modelInstances,
      )
    }

    // Add to data store.
    val importedModels = dataStoreRepository.readImportedModels().toMutableList()
    val importedModelIndex = importedModels.indexOfFirst { info.fileName == it.fileName }
    if (importedModelIndex >= 0) {
      Log.d(TAG, "duplicated imported model found in data store. Removing it first")
      importedModels.removeAt(importedModelIndex)
    }
    importedModels.add(info)
    dataStoreRepository.saveImportedModels(importedModels = importedModels)
  }

  fun getTokenStatusAndData(): TokenStatusAndData {
    // Try to load token data from DataStore.
    var tokenStatus = TokenStatus.NOT_STORED
    Log.d(TAG, "Reading token data from data store...")
    val tokenData = dataStoreRepository.readAccessTokenData()

    // Token exists.
    if (tokenData != null && tokenData.accessToken.isNotEmpty()) {
      Log.d(TAG, "Token exists and loaded.")

      // Check expiration (with 5-minute buffer).
      val curTs = System.currentTimeMillis()
      val expirationTs = tokenData.expiresAtMs - 5 * 60 * 1000L
      Log.d(
        TAG,
        "Checking whether token has expired or not. Current ts: $curTs, expires at: $expirationTs",
      )
      if (curTs >= expirationTs) {
        Log.d(TAG, "Token expired!")
        tokenStatus = TokenStatus.EXPIRED
      } else {
        Log.d(TAG, "Token not expired.")
        tokenStatus = TokenStatus.NOT_EXPIRED
        curAccessToken = tokenData.accessToken
      }
    } else {
      Log.d(TAG, "Token doesn't exists.")
    }

    return TokenStatusAndData(status = tokenStatus, data = tokenData)
  }

  fun getAuthorizationRequest(): AuthorizationRequest {
    return AuthorizationRequest.Builder(
        ProjectConfig.authServiceConfig,
        ProjectConfig.clientId,
        ResponseTypeValues.CODE,
        ProjectConfig.redirectUri.toUri(),
      )
      .setScope("read-repos")
      .setCodeVerifier(null) // Security: AppAuth generates PKCE code_verifier automatically when null
      .build()
  }

  fun handleAuthResult(result: ActivityResult, onTokenRequested: (TokenRequestResult) -> Unit) {
    val dataIntent = result.data
    if (dataIntent == null) {
      onTokenRequested(
        TokenRequestResult(
          status = TokenRequestResultType.FAILED,
          errorMessage = "Empty auth result",
        )
      )
      return
    }

    val response = AuthorizationResponse.fromIntent(dataIntent)
    val exception = AuthorizationException.fromIntent(dataIntent)

    when {
      response?.authorizationCode != null -> {
        // Authorization successful, exchange the code for tokens
        var errorMessage: String? = null
        authService.performTokenRequest(response.createTokenExchangeRequest()) {
          tokenResponse,
          tokenEx ->
          if (tokenResponse != null) {
            if (tokenResponse.accessToken == null) {
              errorMessage = "Empty access token"
            } else if (tokenResponse.refreshToken == null) {
              errorMessage = "Empty refresh token"
            } else if (tokenResponse.accessTokenExpirationTime == null) {
              errorMessage = "Empty expiration time"
            } else {
              // Token exchange successful. Store the tokens securely
              Log.d(TAG, "Token exchange successful. Storing tokens...")
              saveAccessToken(
                accessToken = tokenResponse.accessToken!!,
                refreshToken = tokenResponse.refreshToken!!,
                expiresAt = tokenResponse.accessTokenExpirationTime!!,
              )
              curAccessToken = tokenResponse.accessToken!!
              Log.d(TAG, "Token successfully saved.")
            }
          } else if (tokenEx != null) {
            errorMessage = "Token exchange failed: ${tokenEx.message}"
          } else {
            errorMessage = "Token exchange failed"
          }
          if (errorMessage == null) {
            onTokenRequested(TokenRequestResult(status = TokenRequestResultType.SUCCEEDED))
          } else {
            onTokenRequested(
              TokenRequestResult(
                status = TokenRequestResultType.FAILED,
                errorMessage = errorMessage,
              )
            )
          }
        }
      }

      exception != null -> {
        onTokenRequested(
          TokenRequestResult(
            status =
              if (exception.message == "User cancelled flow") TokenRequestResultType.USER_CANCELLED
              else TokenRequestResultType.FAILED,
            errorMessage = exception.message,
          )
        )
      }

      else -> {
        onTokenRequested(TokenRequestResult(status = TokenRequestResultType.USER_CANCELLED))
      }
    }
  }

  fun saveAccessToken(accessToken: String, refreshToken: String, expiresAt: Long) {
    dataStoreRepository.saveAccessTokenData(
      accessToken = accessToken,
      refreshToken = refreshToken,
      expiresAt = expiresAt,
    )
  }

  fun clearAccessToken() {
    dataStoreRepository.clearAccessTokenData()
  }

  private fun processPendingDownloads() {
    // Cancel all pending downloads for the retrieved models.
    downloadRepository.cancelAll {
      Log.d(TAG, "All workers are cancelled.")

      viewModelScope.launch(Dispatchers.Main) {
        val checkedModelNames = mutableSetOf<String>()
        val tokenStatusAndData = getTokenStatusAndData()
        for (task in uiState.value.tasks) {
          for (model in task.models) {
            if (checkedModelNames.contains(model.name)) {
              continue
            }

            // Start download for partially downloaded models.
            val downloadStatus = uiState.value.modelDownloadStatus[model.name]?.status
            if (downloadStatus == ModelDownloadStatusType.PARTIALLY_DOWNLOADED) {
              if (
                tokenStatusAndData.status == TokenStatus.NOT_EXPIRED &&
                  tokenStatusAndData.data != null
              ) {
                ModelRuntimeStateManager.update(model.name) {
                  it.copy(accessToken = tokenStatusAndData.data.accessToken)
                }
              }
              Log.d(TAG, "Sending a new download request for '${model.name}'")
              downloadRepository.downloadModel(
                task = task,
                model = model,
                onStatusUpdated = this@ModelManagerViewModel::setDownloadStatus,
              )
            }

            checkedModelNames.add(model.name)
          }
        }
      }
    }
  }

  fun loadModelAllowlist() {
    _uiState.update {
      it.copy(loadingModelAllowlist = true, loadingModelAllowlistError = "")
    }

    viewModelScope.launch(Dispatchers.IO) {
      try {
        // Load model allowlist json.
        var modelAllowlist: ModelAllowlist? = null

        // Try to read the test allowlist first (debug builds only).
        if (BuildConfig.DEBUG) {
          Log.d(TAG, "Loading test model allowlist.")
          modelAllowlist = readModelAllowlistFromDisk(fileName = MODEL_ALLOWLIST_TEST_FILENAME)
        }
        if (modelAllowlist == null) {
          // Load from bundled raw resource (model_allowlist.json with DeepSeek/Qwen/Phi models).
          Log.d(TAG, "Loading model allowlist from bundled resource.")
          try {
            val inputStream = context.resources.openRawResource(R.raw.model_allowlist)
            val content = inputStream.bufferedReader().use { it.readText() }
            val gson = Gson()
            modelAllowlist = gson.fromJson(content, ModelAllowlist::class.java)

            if (modelAllowlist != null) {
              Log.d(TAG, "Done: loading model allowlist from bundled resource")
              saveModelAllowlistToDisk(modelAllowlistContent = content)
            }
          } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(TAG, "Failed to load model allowlist from bundled resource", e)
          }

          if (modelAllowlist == null) {
            Log.w(TAG, "Failed to load bundled model allowlist. Trying to load it from disk")
            modelAllowlist = readModelAllowlistFromDisk()
          }
        }

        if (modelAllowlist == null) {
          _uiState.update {
            it.copy(
              loadingModelAllowlist = false,
              loadingModelAllowlistError = context.getString(R.string.error_model_allowlist_failed)
            )
          }
          return@launch
        }

        Log.d(TAG, "Allowlist loaded with ${modelAllowlist.models.size} models")

        // Convert models in the allowlist.
        val curTasks = customTasks.map { it.task }
        for (allowedModel in modelAllowlist.models) {
          if (allowedModel.disabled == true) {
            continue
          }

          val model = allowedModel.toModel()
          for (taskType in allowedModel.taskTypes) {
            val task = curTasks.find { it.id == taskType }
            task?.models?.add(model)
          }
        }

        // Process all tasks.
        processTasks()

        // Update UI state.
        _uiState.update { createUiState().copy(loadingModelAllowlist = false, tasks = curTasks) }

        // Process pending downloads.
        processPendingDownloads()
      } catch (e: Exception) {
        if (e is CancellationException) throw e
        e.printStackTrace()
      }
    }
  }

  fun clearLoadModelAllowlistError() {
    val curTasks = customTasks.map { it.task }
    processTasks()
    _uiState.update {
      createUiState()
        .copy(loadingModelAllowlist = false, tasks = curTasks, loadingModelAllowlistError = "")
    }
  }

  fun setAppInForeground(foreground: Boolean) {
    lifecycleProvider.isAppInForeground = foreground
  }

  private fun saveModelAllowlistToDisk(modelAllowlistContent: String) {
    try {
      Log.d(TAG, "Saving model allowlist to disk...")
      val file = File(externalFilesDir, MODEL_ALLOWLIST_FILENAME)
      file.writeText(modelAllowlistContent)
      Log.d(TAG, "Done: saving model allowlist to disk.")
    } catch (e: Exception) {
      if (e is CancellationException) throw e
      Log.e(TAG, "failed to write model allowlist to disk", e)
    }
  }

  private fun readModelAllowlistFromDisk(
    fileName: String = MODEL_ALLOWLIST_FILENAME
  ): ModelAllowlist? {
    try {
      Log.d(TAG, "Reading model allowlist from disk: $fileName")
      val file = File(externalFilesDir, fileName)
      if (file.exists()) {
        val content = file.readText()
        Log.d(TAG, "Model allowlist loaded from local file (${content.length} bytes)")

        val gson = Gson()
        return gson.fromJson(content, ModelAllowlist::class.java)
      }
    } catch (e: Exception) {
      if (e is CancellationException) throw e
      Log.e(TAG, "failed to read model allowlist from disk", e)
      return null
    }

    return null
  }

  private fun isModelPartiallyDownloaded(model: Model): Boolean {
    if (model.localModelFilePathOverride.isNotEmpty()) {
      return false
    }

    // A model is partially downloaded when the tmp file exists.
    val tmpFilePath =
      model.getPath(context = context, fileName = "${model.downloadFileName}.$TMP_FILE_EXT")
    return File(tmpFilePath).exists()
  }

  private fun createEmptyUiState(): ModelManagerUiState {
    return ModelManagerUiState(
      tasks = listOf(),
      modelDownloadStatus = mapOf(),
      modelInitializationStatus = mapOf(),
    )
  }

  private fun createUiState(): ModelManagerUiState {
    val modelDownloadStatus: MutableMap<String, ModelDownloadStatus> = mutableMapOf()
    val modelInstances: MutableMap<String, ModelInitializationStatus> = mutableMapOf()
    val tasks: MutableMap<String, Task> = mutableMapOf()
    val checkedModelNames = mutableSetOf<String>()
    for (customTask in customTasks) {
      val task = customTask.task
      tasks.put(key = task.id, value = task)
      for (model in task.models) {
        if (checkedModelNames.contains(model.name)) {
          continue
        }
        modelDownloadStatus[model.name] = getModelDownloadStatus(model = model)
        modelInstances[model.name] =
          ModelInitializationStatus(status = ModelInitializationStatusType.NOT_INITIALIZED)
        checkedModelNames.add(model.name)
      }
    }

    // Load imported models.
    for (importedModel in dataStoreRepository.readImportedModels()) {
      Log.d(TAG, "stored imported model: $importedModel")

      // Create model.
      val model = createModelFromImportedModelInfo(info = importedModel)

      // Add to task.
      tasks.get(key = BuiltInTaskId.LLM_CHAT)?.models?.add(model)
      tasks.get(key = BuiltInTaskId.LLM_PROMPT_LAB)?.models?.add(model)
      if (model.llmSupportImage) {
        tasks.get(key = BuiltInTaskId.LLM_ASK_IMAGE)?.models?.add(model)
      }
      if (model.llmSupportAudio) {
        tasks.get(key = BuiltInTaskId.LLM_ASK_AUDIO)?.models?.add(model)
      }

      // Update status.
      modelDownloadStatus[model.name] =
        ModelDownloadStatus(
          status = ModelDownloadStatusType.SUCCEEDED,
          receivedBytes = importedModel.fileSize,
          totalBytes = importedModel.fileSize,
        )
    }

    val textInputHistory = dataStoreRepository.readTextInputHistory()
    Log.d(TAG, "text input history: $textInputHistory")

    Log.d(TAG, "model download status: $modelDownloadStatus")
    return ModelManagerUiState(
      tasks = customTasks.map { it.task }.toList(),
      modelDownloadStatus = modelDownloadStatus,
      modelInitializationStatus = modelInstances,
      textInputHistory = textInputHistory,
    )
  }

  private fun createModelFromImportedModelInfo(info: ImportedModel): Model {
    val accelerators: List<Accelerator> =
      info.llmConfig.compatibleAcceleratorsList.mapNotNull { acceleratorLabel ->
        when (acceleratorLabel.trim()) {
          Accelerator.GPU.label -> Accelerator.GPU
          Accelerator.CPU.label -> Accelerator.CPU
          else -> null // Ignore unknown accelerator labels
        }
      }
    val configs: List<Config> =
      createLlmChatConfigs(
        defaultMaxToken = info.llmConfig.defaultMaxTokens,
        defaultTopK = info.llmConfig.defaultTopk,
        defaultTopP = info.llmConfig.defaultTopp,
        defaultTemperature = info.llmConfig.defaultTemperature,
        accelerators = accelerators,
      )
    val llmSupportImage = info.llmConfig.supportImage
    val llmSupportAudio = info.llmConfig.supportAudio
    val model =
      Model(
        name = info.fileName,
        url = "",
        configs = configs,
        sizeInBytes = info.fileSize,
        downloadFileName = "$IMPORTS_DIR/${info.fileName}",
        showBenchmarkButton = false,
        showRunAgainButton = false,
        imported = true,
        llmSupportImage = llmSupportImage,
        llmSupportAudio = llmSupportAudio,
      )
    model.preProcess()

    return model
  }

  /**
   * Retrieves the download status of a model.
   *
   * This function determines the download status of a given model by checking if it's fully
   * downloaded, partially downloaded, or not downloaded at all. It also retrieves the received and
   * total bytes for partially downloaded models.
   */
  private fun getModelDownloadStatus(model: Model): ModelDownloadStatus {
    Log.d(TAG, "Checking model ${model.name} download status...")

    if (model.localFileRelativeDirPathOverride.isNotEmpty()) {
      Log.d(TAG, "Model has localFileRelativeDirPathOverride set. Set status to SUCCEEDED")
      return ModelDownloadStatus(
        status = ModelDownloadStatusType.SUCCEEDED,
        receivedBytes = 0,
        totalBytes = 0,
      )
    }

    var status = ModelDownloadStatusType.NOT_DOWNLOADED
    var receivedBytes = 0L
    var totalBytes = 0L

    // Partially downloaded.
    if (isModelPartiallyDownloaded(model = model)) {
      status = ModelDownloadStatusType.PARTIALLY_DOWNLOADED
      val tmpFilePath =
        model.getPath(context = context, fileName = "${model.downloadFileName}.$TMP_FILE_EXT")
      val tmpFile = File(tmpFilePath)
      receivedBytes = tmpFile.length()
      totalBytes = ModelRuntimeStateManager.getValue(model.name).totalBytes
      Log.d(TAG, "${model.name} is partially downloaded. $receivedBytes/$totalBytes")
    }
    // Fully downloaded.
    else if (isModelDownloaded(model = model)) {
      status = ModelDownloadStatusType.SUCCEEDED
      Log.d(TAG, "${model.name} has been downloaded.")
    }
    // Not downloaded.
    else {
      Log.d(TAG, "${model.name} has not been downloaded.")
    }

    return ModelDownloadStatus(
      status = status,
      receivedBytes = receivedBytes,
      totalBytes = totalBytes,
    )
  }

  private fun isFileInExternalFilesDir(fileName: String): Boolean {
    if (externalFilesDir != null) {
      val file = File(externalFilesDir, fileName)
      // Security: Verify resolved path is inside externalFilesDir (prevent path traversal)
      return file.canonicalPath.startsWith(externalFilesDir!!.canonicalPath + File.separator) &&
             file.exists()
    } else {
      return false
    }
  }

  private fun isFileInDataLocalTmpDir(fileName: String): Boolean {
    val file = File("/data/local/tmp", fileName)
    return file.exists()
  }

  private fun deleteFileFromExternalFilesDir(fileName: String) {
    if (isFileInExternalFilesDir(fileName)) {
      val file = File(externalFilesDir, fileName)
      file.delete()
    }
  }

  private fun deleteDirFromExternalFilesDir(dir: String) {
    if (isFileInExternalFilesDir(dir)) {
      val file = File(externalFilesDir, dir)
      file.deleteRecursively()
    }
  }

  private fun updateModelInitializationStatus(
    model: Model,
    status: ModelInitializationStatusType,
    error: String = "",
  ) {
    _uiState.update { current ->
      val curModelInstance = current.modelInitializationStatus.toMutableMap()
      curModelInstance[model.name] = ModelInitializationStatus(status = status, error = error)
      current.copy(modelInitializationStatus = curModelInstance)
    }
  }

  private fun isModelDownloaded(model: Model): Boolean {
    val modelRelativePath =
      listOf(model.normalizedName, model.version, model.downloadFileName)
        .joinToString(File.separator)
    val downloadedFileExists =
      model.downloadFileName.isNotEmpty() &&
        ((model.localModelFilePathOverride.isEmpty() &&
          isFileInExternalFilesDir(modelRelativePath)) ||
          (model.localModelFilePathOverride.isNotEmpty() &&
            File(model.localModelFilePathOverride).exists()))

    val unzippedDirectoryExists =
      model.isZip &&
        model.unzipDir.isNotEmpty() &&
        isFileInExternalFilesDir(
          listOf(model.normalizedName, model.version, model.unzipDir).joinToString(File.separator)
        )

    return downloadedFileExists || unzippedDirectoryExists
  }

  // Self-hosted Gemma: Terms acceptance and analytics

  fun isGemmaTermsAccepted(): Boolean {
    return dataStoreRepository.isGemmaTermsAccepted()
  }

  fun acceptGemmaTerms() {
    dataStoreRepository.acceptGemmaTerms()
  }

  fun logGemmaTermsAccepted(modelName: String) {
    Log.d("Analytics", "gemma_terms_accepted: $modelName at ${System.currentTimeMillis()}")
    // TODO: Replace with actual analytics SDK (Firebase Analytics, etc.)
    // Example: analytics.logEvent("gemma_terms_accepted", bundleOf("model_name" to modelName))
  }

  fun logDownloadSuccess(modelName: String, downloadTimeMs: Long) {
    Log.d("Analytics", "model_download_success: $modelName, time: ${downloadTimeMs}ms")
    // TODO: Replace with actual analytics SDK
    // Example: analytics.logEvent("model_download_success", bundleOf(
    //   "model_name" to modelName,
    //   "download_time_ms" to downloadTimeMs
    // ))
  }

  fun logDownloadFailure(modelName: String, error: String, usedFallback: Boolean) {
    Log.e("Analytics", "model_download_failure: $modelName, error: $error, fallback: $usedFallback")
    // TODO: Replace with actual analytics SDK
    // Example: analytics.logEvent("model_download_failure", bundleOf(
    //   "model_name" to modelName,
    //   "error" to error,
    //   "used_fallback" to usedFallback
    // ))
  }

  fun logChecksumFailure(modelName: String) {
    Log.e("Analytics", "checksum_verification_failed: $modelName")
    // TODO: Replace with actual analytics SDK
    // Example: analytics.logEvent("checksum_verification_failed", bundleOf("model_name" to modelName))
  }
}
