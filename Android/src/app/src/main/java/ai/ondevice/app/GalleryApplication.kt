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

package ai.ondevice.app

import android.app.Application
import ai.ondevice.app.data.DataStoreRepository
import ai.ondevice.app.ui.theme.ThemeSettings
import ai.ondevice.app.util.CrashlyticsLogger
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class GalleryApplication : Application() {

  @Inject lateinit var dataStoreRepository: DataStoreRepository

  override fun onCreate() {
    super.onCreate()

    // Load saved theme.
    ThemeSettings.setTheme(dataStoreRepository.readTheme())

    // Initialize Firebase
    FirebaseApp.initializeApp(this)

    // Initialize Firebase Crashlytics
    // Note: Requires google-services.json in app/ directory and plugins applied in build.gradle.kts
    // See docs/firebase-setup.md for setup instructions
    initializeCrashlytics()
  }

  private fun initializeCrashlytics() {
    try {
      FirebaseCrashlytics.getInstance().apply {
        // Enable crash reporting (requires google-services.json)
        setCrashlyticsCollectionEnabled(true)

        // Set app version for crash correlation
        setCustomKey("app_version", BuildConfig.VERSION_NAME)
        setCustomKey("build_number", BuildConfig.VERSION_CODE)

        // Log initialization
        log("Crashlytics initialized successfully")
      }
    } catch (e: IllegalStateException) {
      // Crashlytics not configured (google-services.json missing or plugins not applied)
      // This is expected during development before Firebase setup
      android.util.Log.w("GalleryApplication", "Crashlytics initialization skipped (Firebase not configured)")
    }
  }
}
