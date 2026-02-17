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

package ai.ondevice.app.ui.common.permissions

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

/**
 * Permission types supported by the app with context-aware messaging
 */
enum class AppPermission(
    val permission: String,
    val icon: ImageVector,
    val title: String,
    val benefit: String,
    val privacyNote: String,
    val alternativeAction: String? = null
) {
    MICROPHONE(
        permission = Manifest.permission.RECORD_AUDIO,
        icon = Icons.Rounded.Mic,
        title = "Microphone Access",
        benefit = "Use voice input to talk to the AI instead of typing",
        privacyNote = "Your voice is processed entirely on your device. No audio is uploaded.",
        alternativeAction = "You can still use text input"
    ),

    PHOTOS(
        permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        },
        icon = Icons.Rounded.Image,
        title = "Photo Access",
        benefit = "Select images to ask the AI questions about them",
        privacyNote = "Images are analyzed on-device. No photos are uploaded.",
        alternativeAction = "You can still use text-only conversations"
    ),

    NOTIFICATIONS(
        permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.POST_NOTIFICATIONS
        } else {
            "" // Not needed on older Android versions
        },
        icon = Icons.Rounded.Notifications,
        title = "Notification Access",
        benefit = "Get notified when model downloads complete",
        privacyNote = "Notifications are shown locally. No data is sent to servers.",
        alternativeAction = "You can check download progress in the app"
    );

    fun isGranted(context: Context): Boolean {
        if (permission.isEmpty()) return true // Not needed on this Android version
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }
}

/**
 * Permission request state machine
 */
sealed class PermissionState {
    object NotRequested : PermissionState()
    object ShowingPreScreen : PermissionState()
    object RequestingPermission : PermissionState()
    object Granted : PermissionState()
    object DeniedOnce : PermissionState()
    object ShowingRationale : PermissionState()
    object PermanentlyDenied : PermissionState()
}

/**
 * Composable permission request flow with pre-screen
 */
@Composable
fun rememberPermissionState(
    permission: AppPermission,
    onPermissionResult: (Boolean) -> Unit
): PermissionFlowState {
    val context = LocalContext.current
    var state by remember { mutableStateOf<PermissionState>(PermissionState.NotRequested) }
    var showPreScreen by remember { mutableStateOf(false) }
    var showRationale by remember { mutableStateOf(false) }
    var showPermanentDenialDialog by remember { mutableStateOf(false) }

    // Check if already granted
    val isGranted = permission.isGranted(context)

    LaunchedEffect(isGranted) {
        if (isGranted) {
            state = PermissionState.Granted
        }
    }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            state = PermissionState.Granted
            onPermissionResult(true)
        } else {
            // Check if we should show rationale (user denied once)
            val activity = context as? androidx.activity.ComponentActivity
            if (activity?.shouldShowRequestPermissionRationale(permission.permission) == true) {
                state = PermissionState.DeniedOnce
                showRationale = true
            } else {
                // Permanently denied or first denial without rationale
                state = PermissionState.PermanentlyDenied
                showPermanentDenialDialog = true
            }
            onPermissionResult(false)
        }
    }

    return PermissionFlowState(
        permission = permission,
        isGranted = isGranted,
        showPreScreen = showPreScreen,
        showRationale = showRationale,
        showPermanentDenialDialog = showPermanentDenialDialog,
        onRequestPermission = {
            if (isGranted) {
                onPermissionResult(true)
            } else {
                showPreScreen = true
                state = PermissionState.ShowingPreScreen
            }
        },
        onPreScreenContinue = {
            showPreScreen = false
            state = PermissionState.RequestingPermission
            permissionLauncher.launch(permission.permission)
        },
        onPreScreenDismiss = {
            showPreScreen = false
            state = PermissionState.NotRequested
            onPermissionResult(false)
        },
        onRationaleDismiss = {
            showRationale = false
        },
        onPermanentDenialDismiss = {
            showPermanentDenialDialog = false
        },
        onOpenSettings = {
            openAppSettings(context)
            showPermanentDenialDialog = false
        }
    )
}

data class PermissionFlowState(
    val permission: AppPermission,
    val isGranted: Boolean,
    val showPreScreen: Boolean,
    val showRationale: Boolean,
    val showPermanentDenialDialog: Boolean,
    val onRequestPermission: () -> Unit,
    val onPreScreenContinue: () -> Unit,
    val onPreScreenDismiss: () -> Unit,
    val onRationaleDismiss: () -> Unit,
    val onPermanentDenialDismiss: () -> Unit,
    val onOpenSettings: () -> Unit
)

/**
 * Open app settings for manual permission grant
 */
private fun openAppSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", context.packageName, null)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    context.startActivity(intent)
}
