/*
 * Copyright 2025 OnDevice Inc.
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
 
import java.util.Properties

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use { load(it) }
    }
}

plugins {
    id("com.google.devtools.ksp")
  alias(libs.plugins.android.application)
  // Note: set apply to true once Firebase project is configured (google-services.json required)
  alias(libs.plugins.google.services) apply false
  alias(libs.plugins.firebase.crashlytics) apply false
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.protobuf)
  alias(libs.plugins.hilt.application)
  alias(libs.plugins.oss.licenses)
  kotlin("kapt")
}

android {
  namespace = "ai.ondevice.app"
  compileSdk = 35
  defaultConfig {
    applicationId = "ai.ondevice.app"
    minSdk = 31
    targetSdk = 35
    versionCode = 35
    versionName = "1.1.9"
    
    // Needed for HuggingFace auth workflows.
    // Use the scheme of the "Redirect URLs" in HuggingFace app.
    manifestPlaceholders["appAuthRedirectScheme"] = "ai.ondevice.app"
    manifestPlaceholders["applicationName"] = "ai.ondevice.app.GalleryApplication"
    
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    
    // BuildConfig fields for OAuth credentials
    buildConfigField(
        "String",
        "HF_CLIENT_ID",
        "\"${localProperties.getProperty("hf.client.id", "")}\""
    )
    buildConfigField(
        "String",
        "HF_REDIRECT_URI",
        "\"${localProperties.getProperty("hf.redirect.uri", "ai.ondevice.app:/oauth2redirect")}\""
    )

    // Brave Search API key for web search feature
    // Priority: Environment variable > local.properties > empty string
    val braveApiKey = System.getenv("BRAVE_API_KEY")
        ?: localProperties.getProperty("brave.api.key", "")
    buildConfigField(
        "String",
        "BRAVE_API_KEY",
        "\"$braveApiKey\""
    )
  }

  signingConfigs {
    create("release") {
      storeFile = file("../ondevice-ai-release.keystore")
      storePassword = System.getenv("SIGNING_STORE_PASSWORD")
      keyAlias = System.getenv("SIGNING_KEY_ALIAS") ?: "ondevice-key"
      keyPassword = System.getenv("SIGNING_KEY_PASSWORD")
    }
  }

  buildTypes {
    release {
      // TODO: Enable ProGuard after adding proper rules for auto-value library
      // See: https://github.com/google/auto/issues/982
      isMinifyEnabled = false
      isShrinkResources = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      // Only use release signing if credentials are available
      val hasSigningCredentials = System.getenv("SIGNING_STORE_PASSWORD")?.isNotEmpty() == true
      if (hasSigningCredentials) {
        signingConfig = signingConfigs.getByName("release")
      }

      // Firebase Crashlytics - Upload ProGuard mapping files for deobfuscated stack traces
      configure<com.google.firebase.crashlytics.buildtools.gradle.CrashlyticsExtension> {
        mappingFileUploadEnabled = true
      }
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  kotlinOptions {
    jvmTarget = "11"
    freeCompilerArgs += "-Xcontext-receivers"
  }
  buildFeatures {
    compose = true
    buildConfig = true
  }
}

dependencies {
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.activity.compose)
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.ui)
  implementation(libs.androidx.ui.graphics)
  implementation(libs.androidx.ui.tooling.preview)
  implementation(libs.androidx.material3)
  implementation(libs.androidx.compose.navigation)
  implementation(libs.kotlinx.serialization.json)
  implementation(libs.material.icon.extended)
  implementation(libs.androidx.work.runtime)
  implementation(libs.androidx.datastore)
  implementation(libs.androidx.datastore.preferences)
  implementation(libs.com.google.code.gson)
  implementation(libs.retrofit)
  implementation(libs.retrofit.converter.gson)
  implementation(libs.androidx.lifecycle.process)
  implementation(libs.androidx.security.crypto)
  implementation(libs.mediapipe.tasks.text)
  implementation(libs.mediapipe.tasks.genai)
  implementation(libs.mediapipe.tasks.imagegen)
  implementation(libs.litertlm)
  implementation(libs.commonmark)
  implementation(libs.richtext)
  implementation(libs.tflite)
  implementation(libs.tflite.gpu)
  implementation(libs.tflite.support)
  implementation(libs.camerax.core)
  implementation(libs.camerax.camera2)
  implementation(libs.camerax.lifecycle)
  implementation(libs.camerax.view)
  implementation(libs.openid.appauth)
  implementation(libs.androidx.splashscreen)
  implementation(libs.protobuf.javalite)
  implementation(libs.hilt.android)
  implementation(libs.hilt.navigation.compose)
  implementation(libs.play.services.oss.licenses)
  implementation(platform(libs.firebase.bom))
  implementation(libs.firebase.analytics)
  implementation(libs.firebase.crashlytics)
  implementation(libs.androidx.exifinterface)
  kapt(libs.hilt.android.compiler)
  testImplementation(libs.junit)
  testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.ui.test.junit4)
  androidTestImplementation(libs.hilt.android.testing)
  debugImplementation(libs.androidx.ui.tooling)
  debugImplementation(libs.androidx.ui.test.manifest)

    // Room Database
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // SnakeYAML for GQA dataset loading
    implementation("org.yaml:snakeyaml:2.2")
}

protobuf {
  protoc { artifact = "com.google.protobuf:protoc:4.26.1" }
  generateProtoTasks { all().forEach { it.plugins { create("java") { option("lite") } } } }
}
