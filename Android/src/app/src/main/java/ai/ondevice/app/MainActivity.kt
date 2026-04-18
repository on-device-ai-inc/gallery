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

package ai.ondevice.app

import android.animation.ObjectAnimator
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.animation.doOnEnd
import androidx.core.os.bundleOf
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import ai.ondevice.app.security.LicenseManager
import ai.ondevice.app.ui.modelmanager.ModelManagerViewModel
import ai.ondevice.app.ui.theme.GalleryTheme
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.perf.performance
import com.google.firebase.perf.metrics.Trace
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

  private val modelManagerViewModel: ModelManagerViewModel by viewModels()
  private var coldStartTrace: Trace? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    // Track cold start time (first launch only)
    if (savedInstanceState == null) {
      coldStartTrace = safePerformanceTrace("cold_start")
      coldStartTrace.safeStart()
    }
    super.onCreate(savedInstanceState)

    // Handle license activation deep link (ai.ondevice.app://activate?order_id=...)
    handleActivationIntent(intent)

    modelManagerViewModel.loadModelAllowlist()

    // Install splash screen before setContent so the splash-to-app transition works.
    val splashScreen = installSplashScreen()

    enableEdgeToEdge()

    // setContent must be called unconditionally — placing it inside setOnExitAnimationListener
    // means it is never called on devices/configs where the splash animation is skipped.
    setContent {
      GalleryTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
          GalleryApp(modelManagerViewModel = modelManagerViewModel)

          // Mask that fades out to reveal app content, replacing the splash cross-fade.
          var startMaskFadeout by remember { mutableStateOf(false) }
          LaunchedEffect(Unit) { startMaskFadeout = true }
          AnimatedVisibility(
            !startMaskFadeout,
            enter = fadeIn(animationSpec = snap(0)),
            exit = fadeOut(animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)),
          ) {
            Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background))
          }
        }
      }
    }

    // Animate splash screen exit after content is set.
    splashScreen.setOnExitAnimationListener { splashScreenView ->
      val now = System.currentTimeMillis()
      val iconAnimationStartMs = splashScreenView.iconAnimationStartMillis
      val duration = splashScreenView.iconAnimationDurationMillis
      val fadeOut = ObjectAnimator.ofFloat(splashScreenView.view, View.ALPHA, 1f, 0f)
      fadeOut.interpolator = DecelerateInterpolator()
      fadeOut.duration = 300L
      fadeOut.doOnEnd { splashScreenView.remove() }
      lifecycleScope.launch {
        val animDelay = duration - (now - iconAnimationStartMs) - 300
        if (animDelay > 0) delay(animDelay)
        fadeOut.start()
      }
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      // Fix for three-button nav not properly going edge-to-edge.
      // See: https://issuetracker.google.com/issues/298296168
      window.isNavigationBarContrastEnforced = false
    }
    // Keep the screen on while the app is running for better demo experience.
    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    handleActivationIntent(intent)
  }

  private fun handleActivationIntent(intent: Intent?) {
    val data: Uri = intent?.data ?: return
    if (data.scheme != "ai.ondevice.app" || data.host != "activate") return
    val orderId = data.getQueryParameter("order_id")
    if (orderId.isNullOrBlank()) {
      Log.w(TAG, "Activation deep link missing order_id")
      return
    }
    lifecycleScope.launch {
      val result = LicenseManager.activate(applicationContext, orderId)
      result.onFailure { err ->
        Log.w(TAG, "Activation failed for order=$orderId: ${err.message}")
      }
    }
  }

  override fun onResume() {
    super.onResume()

    coldStartTrace.safeStop()
    coldStartTrace = null

    firebaseAnalytics?.logEvent(
      FirebaseAnalytics.Event.APP_OPEN,
      bundleOf(
        "app_version" to BuildConfig.VERSION_NAME,
        "os_version" to Build.VERSION.SDK_INT.toString(),
        "device_model" to Build.MODEL,
      ),
    )
  }

  companion object {
    private const val TAG = "AGMainActivity"
  }
}
