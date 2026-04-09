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

import ai.ondevice.app.BuildConfig
import ai.ondevice.app.data.ModelRuntimeStateManager
import android.content.Intent
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import ai.ondevice.app.R
import ai.ondevice.app.data.Model
import ai.ondevice.app.data.ModelDownloadStatus
import ai.ondevice.app.data.ModelDownloadStatusType
import ai.ondevice.app.data.Task
import ai.ondevice.app.ui.modelmanager.ModelManagerViewModel
import ai.ondevice.app.ui.modelmanager.TokenRequestResultType
import ai.ondevice.app.ui.modelmanager.TokenStatus
import ai.ondevice.app.ui.common.tos.GemmaTermsDialog
import ai.ondevice.app.helper.SecureModelDownloader
import ai.ondevice.app.config.FeatureFlags
import java.net.HttpURLConnection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "AGDownloadAndTryButton"
private const val SYSTEM_RESERVED_MEMORY_IN_BYTES = 3 * (1L shl 30)

/**
 * Handles the "Download & Try it" button click, managing the model download process based on
 * various conditions.
 *
 * If the button is enabled and not currently checking the token, it initiates a coroutine to handle
 * the download logic.
 *
 * For models requiring download first, it specifically addresses HuggingFace URLs by first checking
 * if authentication is necessary. If no authentication is needed, the download starts directly.
 * Otherwise, it checks the current token status; if the token is invalid or expired, a token
 * exchange flow is initiated. If a valid token exists, it attempts to access the download URL. If
 * access is granted, the download begins; if not, a new token is requested.
 *
 * For non-HuggingFace URLs that need downloading, the download starts directly.
 *
 * If the model doesn't need to be downloaded first, the provided `onClicked` callback is executed.
 *
 * Additionally, for gated HuggingFace models, if accessing the model after token exchange results
 * in a forbidden error, a modal bottom sheet is displayed, prompting the user to acknowledge the
 * user agreement by opening it in a custom tab. Upon closing the tab, the download process is
 * retried.
 *
 * The composable also manages UI states for indicating token checking and displaying the agreement
 * acknowledgement sheet, and it handles requesting notification permissions before initiating the
 * actual download.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadAndTryButton(
  task: Task,
  model: Model,
  enabled: Boolean,
  downloadStatus: ModelDownloadStatus?,
  modelManagerViewModel: ModelManagerViewModel,
  onClicked: () -> Unit,
  modifier: Modifier = Modifier,
  compact: Boolean = false,
  canShowTryIt: Boolean = true,
) {
  val scope = rememberCoroutineScope()
  val context = LocalContext.current
  var checkingToken by remember { mutableStateOf(false) }
  var showAgreementAckSheet by remember { mutableStateOf(false) }
  var showErrorDialog by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf("") }
  var errorTitle by remember { mutableStateOf("Error") }
  var retryAction by remember { mutableStateOf<(() -> Unit)?>(null) }
  var showMemoryWarning by remember { mutableStateOf(false) }
  var downloadStarted by remember { mutableStateOf(false) }
  var showPreOAuthDialog by remember { mutableStateOf(false) }
  var showLicenseSummaryDialog by remember { mutableStateOf(false) }
  var showGemmaTermsDialog by remember { mutableStateOf(false) }
  var showStorageWarningDialog by remember { mutableStateOf(false) }
  var storageWarningMessage by remember { mutableStateOf("") }
  val sheetState = rememberModalBottomSheetState()
  val secureDownloader = remember { SecureModelDownloader(context) }

  val needToDownloadFirst =
    (downloadStatus?.status == ModelDownloadStatusType.NOT_DOWNLOADED ||
      downloadStatus?.status == ModelDownloadStatusType.FAILED) &&
      model.localFileRelativeDirPathOverride.isEmpty()
  val inProgress = downloadStatus?.status == ModelDownloadStatusType.IN_PROGRESS
  val downloadSucceeded = downloadStatus?.status == ModelDownloadStatusType.SUCCEEDED
  val isPartiallyDownloaded = downloadStatus?.status == ModelDownloadStatusType.PARTIALLY_DOWNLOADED
  val showDownloadProgress =
    !downloadSucceeded && (downloadStarted || checkingToken || inProgress || isPartiallyDownloaded)
  var curDownloadProgress: Float

  // A launcher for requesting notification permission.
  val permissionLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
      modelManagerViewModel.downloadModel(task = task, model = model)
    }

  // Function to kick off download.
  val startDownload: (accessToken: String?) -> Unit = { accessToken ->
    ModelRuntimeStateManager.update(model.name) { it.copy(accessToken = accessToken) }
    checkNotificationPermissionAndStartDownload(
      context = context,
      launcher = permissionLauncher,
      modelManagerViewModel = modelManagerViewModel,
      task = task,
      model = model,
    )
    checkingToken = false
  }

  // A launcher for opening the custom tabs intent for requesting user agreement ack.
  // Once the tab is closed, try starting the download process.
  val agreementAckLauncher: ActivityResultLauncher<Intent> =
    rememberLauncherForActivityResult(
      contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
      Log.d(TAG, "User closes the browser tab. Try to start downloading.")
      startDownload(modelManagerViewModel.curAccessToken)
    }

  // A launcher for handling the authentication flow.
  // It processes the result of the authentication activity and then checks if a user agreement
  // acknowledgement is needed before proceeding with the model download.
  val authResultLauncher =
    rememberLauncherForActivityResult(
      contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
      modelManagerViewModel.handleAuthResult(
        result,
        onTokenRequested = { tokenRequestResult ->
          when (tokenRequestResult.status) {
            TokenRequestResultType.SUCCEEDED -> {
              Log.d(TAG, "Token request succeeded. Checking if we need user to ack user agreement")
              scope.launch(Dispatchers.IO) {
                // Check if we can use the current token to access model. If not, we might need to
                // acknowledge the user agreement.
                if (
                  modelManagerViewModel.getModelUrlResponse(
                    model = model,
                    accessToken = modelManagerViewModel.curAccessToken,
                  ) == HttpURLConnection.HTTP_FORBIDDEN
                ) {
                  Log.d(TAG, "Model '${model.name}' needs user agreement ack.")
                  withContext(Dispatchers.Main) {
                    showLicenseSummaryDialog = true
                  }
                } else {
                  Log.d(
                    TAG,
                    "Model '${model.name}' does NOT need user agreement ack. Start downloading...",
                  )
                  withContext(Dispatchers.Main) {
                    startDownload(modelManagerViewModel.curAccessToken)
                  }
                }
              }
            }

            TokenRequestResultType.FAILED -> {
              Log.e(
                TAG,
                "OAuth failed. Error: ${tokenRequestResult.errorMessage ?: "Unknown error"}",
              )
              checkingToken = false
              downloadStarted = false
              errorTitle = "Authentication Failed"
              errorMessage = tokenRequestResult.errorMessage
                ?: "Failed to authenticate with HuggingFace. Please try again."
              retryAction = {
                checkingToken = true
                showPreOAuthDialog = true
              }
              showErrorDialog = true
            }

            TokenRequestResultType.USER_CANCELLED -> {
              Log.d(TAG, "User cancelled. Do nothing")
              checkingToken = false
              downloadStarted = false
            }
          }
        },
      )
    }

  // Function to kick off the authentication and token exchange flow.
  val startTokenExchange = {
    val authRequest = modelManagerViewModel.getAuthorizationRequest()
    val authIntent = modelManagerViewModel.authService.getAuthorizationRequestIntent(authRequest)
    authResultLauncher.launch(authIntent)
  }

  // Launches a coroutine to handle the initial check and potential authentication flow
  // before downloading the model. It checks if the model needs to be downloaded first,
  // handles HuggingFace URLs by verifying the need for authentication, and initiates
  // the token exchange process if required or proceeds with the download if no auth is needed
  // or a valid token is available.
  fun handleClickButton() {
    scope.launch(Dispatchers.IO) {
      if (needToDownloadFirst) {
        // STEP 1: Check Gemma Terms FIRST (before any OAuth or download logic)
        if (model.requiresGemmaTerms && !modelManagerViewModel.isGemmaTermsAccepted()) {
          withContext(Dispatchers.Main) {
            showGemmaTermsDialog = true
          }
          return@launch
        }

        // STEP 2: Validate download conditions (WiFi, storage, network)
        val validationError = secureDownloader.validateDownloadConditions(model)
        if (validationError != null) {
          withContext(Dispatchers.Main) {
            storageWarningMessage = validationError
            showStorageWarningDialog = true
          }
          return@launch
        }

        downloadStarted = true

        // STEP 3: Determine which URL to use (self-hosted vs OAuth)
        val useOAuthFlow = model.url.startsWith("https://huggingface.co") &&
                           (model.downloadUrl.isEmpty() ||
                            !FeatureFlags.isUserInSelfHostedCohort(null))

        // For HuggingFace urls (OAuth flow)
        if (useOAuthFlow) {
          checkingToken = true

          // Check if the url needs auth.
          Log.d(
            TAG,
            "Model '${model.name}' is from HuggingFace. Checking if the url needs auth to download",
          )
          val firstResponseCode = modelManagerViewModel.getModelUrlResponse(model = model)
          if (firstResponseCode == HttpURLConnection.HTTP_OK) {
            Log.d(TAG, "Model '${model.name}' doesn't need auth. Start downloading the model...")
            withContext(Dispatchers.Main) { startDownload(null) }
            return@launch
          } else if (firstResponseCode < 0) {
            checkingToken = false
            downloadStarted = false
            Log.e(TAG, "Network error checking model access")
            errorTitle = "Network Error"
            errorMessage = "Failed to connect to HuggingFace. Please check your internet connection and try again."
            retryAction = {
              handleClickButton()
            }
            showErrorDialog = true
            return@launch
          }
          Log.d(TAG, "Model '${model.name}' needs auth. Start token exchange process...")

          // Get current token status
          val tokenStatusAndData = modelManagerViewModel.getTokenStatusAndData()

          when (tokenStatusAndData.status) {
            // If token is not stored or expired, log in and request a new token.
            TokenStatus.NOT_STORED,
            TokenStatus.EXPIRED -> {
              withContext(Dispatchers.Main) {
                showPreOAuthDialog = true
              }
            }

            // If token is still valid...
            TokenStatus.NOT_EXPIRED -> {
              val tokenData = tokenStatusAndData.data
              if (tokenData == null) {
                Log.e(TAG, "Token status is NOT_EXPIRED but token data is null. Requesting new token.")
                withContext(Dispatchers.Main) {
                  showPreOAuthDialog = true
                }
              } else {
              // Use the current token to check the download url.
              Log.d(TAG, "Checking the download url '${model.url}' with the current token...")
              val responseCode =
                modelManagerViewModel.getModelUrlResponse(
                  model = model,
                  accessToken = tokenData.accessToken,
                )
              if (responseCode == HttpURLConnection.HTTP_OK) {
                // Download url is accessible. Download the model.
                Log.d(TAG, "Download url is accessible with the current token.")

                withContext(Dispatchers.Main) {
                  startDownload(tokenData.accessToken)
                }
              }
              // Download url is NOT accessible. Request a new token.
              else {
                Log.d(
                  TAG,
                  "Download url is NOT accessible. Response code: ${responseCode}. Trying to request a new token.",
                )

                withContext(Dispatchers.Main) {
                  showPreOAuthDialog = true
                }
              }
              }
            }
          }
        }
        // For other urls, just download the model.
        else {
          Log.d(
            TAG,
            "Model '${model.name}' is not from huggingface. Start downloading the model...",
          )
          withContext(Dispatchers.Main) { startDownload(null) }
        }
      }
      // No need to download. Directly open the model.
      else {
        withContext(Dispatchers.Main) { onClicked() }
      }
    }
  }

  if (!showDownloadProgress) {
    var buttonModifier: Modifier = modifier.height(42.dp)
    if (!compact) {
      buttonModifier = buttonModifier.fillMaxWidth()
    }
    Button(
      modifier = buttonModifier,
      colors =
        ButtonDefaults.buttonColors(
          containerColor =
            if (
              (!downloadSucceeded || !canShowTryIt) &&
                model.localFileRelativeDirPathOverride.isEmpty()
            )
              MaterialTheme.colorScheme.surfaceContainer
            else getTaskBgGradientColors(task = task)[1]
        ),
      contentPadding = PaddingValues(horizontal = 12.dp),
      onClick = {
        if (!enabled || checkingToken) {
          return@Button
        }

        if (isMemoryLow(context = context, model = model)) {
          showMemoryWarning = true
        } else {
          handleClickButton()
        }
      },
    ) {
      val textColor =
        if (!downloadSucceeded && model.localFileRelativeDirPathOverride.isEmpty())
          MaterialTheme.colorScheme.onSurface
        else Color.White
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Icon(
          if (needToDownloadFirst) Icons.Outlined.FileDownload
          else Icons.AutoMirrored.Rounded.ArrowForward,
          contentDescription = "",
          tint = textColor,
        )

        if (!compact) {
          if (needToDownloadFirst) {
            Text(
              stringResource(R.string.download),
              color = textColor,
              style = MaterialTheme.typography.titleMedium,
            )
          } else if (canShowTryIt) {
            Text(
              stringResource(R.string.try_it),
              color = textColor,
              style = MaterialTheme.typography.titleMedium,
            )
          }
        }
      }
    }
  }
  // Download progress.
  else {
    curDownloadProgress = if (downloadStatus != null) {
      downloadStatus.receivedBytes.toFloat() / downloadStatus.totalBytes.toFloat()
    } else {
      0f
    }
    if (curDownloadProgress.isNaN()) {
      curDownloadProgress = 0f
    }
    val animatedProgress = remember { Animatable(0f) }

    var downloadProgressModifier: Modifier = modifier
    if (!compact) {
      downloadProgressModifier = downloadProgressModifier.fillMaxWidth()
    }
    downloadProgressModifier =
      downloadProgressModifier
        .clip(CircleShape)
        .background(MaterialTheme.colorScheme.surfaceContainer)
        .padding(horizontal = 8.dp)
        .height(42.dp)
    Row(modifier = downloadProgressModifier, verticalAlignment = Alignment.CenterVertically) {
      if (checkingToken) {
        Text(
          stringResource(R.string.checking_access),
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurface,
          textAlign = TextAlign.Center,
          modifier = if (!compact) Modifier.fillMaxWidth() else Modifier.padding(horizontal = 4.dp),
        )
      } else {
        Text(
          "${(curDownloadProgress * 100).toInt()}%",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurface,
          modifier = Modifier.padding(start = 12.dp).width(if (compact) 32.dp else 44.dp),
        )
        if (!compact) {
          LinearProgressIndicator(
            modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
            progress = { animatedProgress.value },
            color = getTaskBgGradientColors(task = task)[1],
            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
          )
        }
        IconButton(
          onClick = {
            downloadStarted = false
            modelManagerViewModel.cancelDownloadModel(task = task, model = model)
          },
          colors =
            IconButtonDefaults.iconButtonColors(
              containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
        ) {
          Icon(
            Icons.Outlined.Close,
            contentDescription = "",
            tint = MaterialTheme.colorScheme.onSurface,
          )
        }
      }
    }
    LaunchedEffect(curDownloadProgress) {
      animatedProgress.animateTo(curDownloadProgress, animationSpec = tween(150))
    }
  }

  // A ModalBottomSheet composable that displays information about the user agreement
  // for a gated model and provides a button to open the agreement in a custom tab.
  // Upon clicking the button, it constructs the agreement URL, launches it using a
  // custom tab, and then dismisses the bottom sheet.
  // WebView-based license acceptance with auto-scroll
  if (showAgreementAckSheet) {
    AlertDialog(
      onDismissRequest = {
        showAgreementAckSheet = false
        checkingToken = false
      },
      title = { Text("Accept Gemma License") },
      text = {
        Column(modifier = Modifier.fillMaxWidth()) {
          Text(
            "Please accept Google's Gemma license to download this model. The 'Agree' button will be highlighted for you.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 16.dp),
          )

          // Get agreement url from model url
          val index = model.url.indexOf("/resolve/")
          val agreementUrl = if (index >= 0) model.url.substring(0, index) else ""

          // Security: Validate the agreement URL is a trusted HuggingFace origin
          val isValidAgreementUrl = agreementUrl.isNotEmpty() &&
            try {
              val parsed = java.net.URL(agreementUrl)
              parsed.protocol == "https" && (parsed.host == "huggingface.co" || parsed.host.endsWith(".huggingface.co"))
            } catch (e: Exception) { false }

          if (isValidAgreementUrl) {
            AndroidView(
              factory = { context ->
                WebView(context).apply {
                  settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    // Security hardening
                    allowFileAccess = false
                    setGeolocationEnabled(false)
                  }

                  webViewClient = object : WebViewClient() {
                    // Security: Block navigation away from HuggingFace
                    override fun shouldOverrideUrlLoading(
                      view: WebView?,
                      request: android.webkit.WebResourceRequest
                    ): Boolean {
                      val host = request.url.host ?: return true
                      if (host == "huggingface.co" || host.endsWith(".huggingface.co")) {
                        return false // Allow HuggingFace navigation
                      }
                      return true // Block all other hosts
                    }

                    override fun shouldInterceptRequest(
                      view: WebView?,
                      request: android.webkit.WebResourceRequest
                    ): android.webkit.WebResourceResponse? {
                      // Security: Only inject Authorization header for HuggingFace requests
                      val host = request.url.host
                      if (host != null && (host == "huggingface.co" || host.endsWith(".huggingface.co"))) {
                        var connection: java.net.HttpURLConnection? = null
                        return try {
                          val accessToken = modelManagerViewModel.curAccessToken
                          connection = java.net.URL(request.url.toString()).openConnection() as java.net.HttpURLConnection
                          connection.setRequestProperty("Authorization", "Bearer $accessToken")
                          connection.setRequestProperty("User-Agent", "OnDeviceAI-Android/1.1.9")

                          android.webkit.WebResourceResponse(
                            connection.contentType ?: "text/html",
                            connection.contentEncoding ?: "UTF-8",
                            connection.inputStream
                          )
                        } catch (e: Exception) {
                          Log.e(TAG, "Failed to load with auth header", e)
                          connection?.disconnect()
                          null
                        }
                      }
                      return super.shouldInterceptRequest(view, request)
                    }

                    override fun onReceivedError(
                      view: WebView?,
                      request: android.webkit.WebResourceRequest?,
                      error: android.webkit.WebResourceError?
                    ) {
                      super.onReceivedError(view, request, error)
                      if (request?.isForMainFrame == true) {
                        Log.e(TAG, "WebView error loading license page: ${error?.description}")
                        showAgreementAckSheet = false
                        errorTitle = "License Page Error"
                        errorMessage = "Failed to load the license page. Please check your internet connection and try again."
                        retryAction = {
                          showLicenseSummaryDialog = true
                        }
                        showErrorDialog = true
                      }
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                      // Auto-scroll to Agree button and highlight it
                      view?.evaluateJavascript("""
                        (function() {
                          // Wait a bit for page to fully render
                          setTimeout(function() {
                            // Find the agree button (HuggingFace uses button[type="submit"])
                            const agreeBtn = document.querySelector('button[type="submit"]');
                            if (agreeBtn) {
                              // Scroll into view
                              agreeBtn.scrollIntoView({ behavior: 'smooth', block: 'center' });
                              // Highlight with green border
                              agreeBtn.style.border = '3px solid #4CAF50';
                              agreeBtn.style.boxShadow = '0 0 10px #4CAF50';
                            }
                          }, 500);
                        })();
                      """, null)

                      // Check if user clicked Agree (URL changes after acceptance)
                      if (url != null && url != agreementUrl && !url.contains("/login")) {
                        Log.d(TAG, "License accepted, closing dialog")
                        showAgreementAckSheet = false
                        startDownload(modelManagerViewModel.curAccessToken)
                      }
                    }
                  }

                  loadUrl(agreementUrl)
                }
              },
              modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
            )
          }
        }
      },
      confirmButton = {
        TextButton(onClick = {
          showAgreementAckSheet = false
          startDownload(modelManagerViewModel.curAccessToken)
        }) {
          Text("I've Accepted")
        }
      },
      dismissButton = {
        TextButton(onClick = {
          showAgreementAckSheet = false
          checkingToken = false
        }) {
          Text("Cancel")
        }
      }
    )
  }

  if (showErrorDialog) {
    AlertDialog(
      icon = {
        Icon(Icons.Rounded.Error, contentDescription = "", tint = MaterialTheme.colorScheme.error)
      },
      title = { Text(errorTitle) },
      text = { Text(errorMessage) },
      onDismissRequest = {
        showErrorDialog = false
        retryAction = null
      },
      confirmButton = {
        if (retryAction != null) {
          TextButton(onClick = {
            showErrorDialog = false
            retryAction?.invoke()
            retryAction = null
          }) {
            Text("Retry")
          }
        }
      },
      dismissButton = {
        TextButton(onClick = {
          showErrorDialog = false
          retryAction = null
        }) {
          Text("Close")
        }
      },
    )
  }

  if (showMemoryWarning) {
    MemoryWarningAlert(
      onProceeded = {
        handleClickButton()
        showMemoryWarning = false
      },
      onDismissed = { showMemoryWarning = false },
    )
  }

  // Pre-OAuth Warning Dialog - Sets user expectations before browser opens
  if (showPreOAuthDialog) {
    AlertDialog(
      onDismissRequest = {
        showPreOAuthDialog = false
        checkingToken = false
      },
      title = {
        Column(modifier = Modifier.fillMaxWidth()) {
          // Step indicator
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
          ) {
            // Step 1 - Active
            androidx.compose.foundation.layout.Box(
              modifier = Modifier
                .width(32.dp)
                .height(32.dp)
                .background(
                  color = MaterialTheme.colorScheme.primary,
                  shape = CircleShape
                ),
              contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
              Text("1", color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.labelLarge)
            }

            // Connector line
            androidx.compose.foundation.layout.Spacer(
              modifier = Modifier
                .width(40.dp)
                .height(2.dp)
                .background(MaterialTheme.colorScheme.outline)
            )

            // Step 2 - Inactive
            androidx.compose.foundation.layout.Box(
              modifier = Modifier
                .width(32.dp)
                .height(32.dp)
                .background(
                  color = MaterialTheme.colorScheme.surfaceVariant,
                  shape = CircleShape
                ),
              contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
              Text("2", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelLarge)
            }
          }

          androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(16.dp))
          Text("Sign In", style = MaterialTheme.typography.headlineSmall)
        }
      },
      text = {
        Column(modifier = Modifier.fillMaxWidth()) {
          Text(
            "To download Gemma, you need a HuggingFace account.\n\n" +
            "Here's what happens next:\n\n" +
            "1. Your browser will open\n" +
            "2. Sign in or create account\n" +
            "3. You'll see \"Authorize?\"\n" +
            "   Tap [Authorize] - it's safe\n" +
            "4. You'll return to this app\n\n" +
            "This takes about 60 seconds.",
            style = MaterialTheme.typography.bodyMedium
          )
        }
      },
      confirmButton = {
        TextButton(onClick = {
          showPreOAuthDialog = false
          startTokenExchange()
        }) {
          Text("Continue")
        }
      },
      dismissButton = {
        TextButton(onClick = {
          showPreOAuthDialog = false
          checkingToken = false
        }) {
          Text("Cancel")
        }
      }
    )
  }

  // License Summary Dialog - Simplifies legal terms before full license
  if (showLicenseSummaryDialog) {
    AlertDialog(
      onDismissRequest = {
        showLicenseSummaryDialog = false
        checkingToken = false
      },
      title = {
        Column(modifier = Modifier.fillMaxWidth()) {
          // Step indicator
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
          ) {
            // Step 1 - Completed
            androidx.compose.foundation.layout.Box(
              modifier = Modifier
                .width(32.dp)
                .height(32.dp)
                .background(
                  color = MaterialTheme.colorScheme.tertiary,
                  shape = CircleShape
                ),
              contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
              Text("✓", color = MaterialTheme.colorScheme.onTertiary, style = MaterialTheme.typography.labelLarge)
            }

            // Connector line
            androidx.compose.foundation.layout.Spacer(
              modifier = Modifier
                .width(40.dp)
                .height(2.dp)
                .background(MaterialTheme.colorScheme.primary)
            )

            // Step 2 - Active
            androidx.compose.foundation.layout.Box(
              modifier = Modifier
                .width(32.dp)
                .height(32.dp)
                .background(
                  color = MaterialTheme.colorScheme.primary,
                  shape = CircleShape
                ),
              contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
              Text("2", color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.labelLarge)
            }
          }

          androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(16.dp))
          Text("Accept License", style = MaterialTheme.typography.headlineSmall)
        }
      },
      text = {
        Column(modifier = Modifier.fillMaxWidth()) {
          Text(
            "Gemma requires you to agree to:\n\n" +
            "✓ Non-commercial use only\n" +
            "  (Learning & testing)\n\n" +
            "✓ No redistribution\n" +
            "  (Can't share the model)\n\n" +
            "✓ Credit Google if you publish\n\n" +
            "Tap [I Agree] to see full license.",
            style = MaterialTheme.typography.bodyMedium
          )
        }
      },
      confirmButton = {
        TextButton(onClick = {
          showLicenseSummaryDialog = false
          showAgreementAckSheet = true
        }) {
          Text("I Agree")
        }
      },
      dismissButton = {
        TextButton(onClick = {
          showLicenseSummaryDialog = false
          checkingToken = false
        }) {
          Text("Cancel")
        }
      }
    )
  }

  // Gemma Terms Dialog - Required before downloading any Gemma model
  if (showGemmaTermsDialog) {
    GemmaTermsDialog(
      modelName = model.name,
      onAccept = {
        modelManagerViewModel.acceptGemmaTerms()
        modelManagerViewModel.logGemmaTermsAccepted(model.name)
        showGemmaTermsDialog = false
        // Retry download after accepting terms
        handleClickButton()
      },
      onDecline = {
        showGemmaTermsDialog = false
        checkingToken = false
        downloadStarted = false
      }
    )
  }

  // Storage Warning Dialog - Shows WiFi/storage/network issues
  if (showStorageWarningDialog) {
    AlertDialog(
      onDismissRequest = {
        showStorageWarningDialog = false
        checkingToken = false
        downloadStarted = false
      },
      icon = {
        Icon(
          Icons.Rounded.Error,
          contentDescription = "",
          tint = MaterialTheme.colorScheme.error
        )
      },
      title = {
        Text("Cannot Download Model")
      },
      text = {
        Text(storageWarningMessage)
      },
      confirmButton = {
        TextButton(onClick = {
          showStorageWarningDialog = false
          checkingToken = false
          downloadStarted = false
        }) {
          Text("OK")
        }
      }
    )
  }
}
