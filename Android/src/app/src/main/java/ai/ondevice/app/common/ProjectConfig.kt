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

package ai.ondevice.app.common

import ai.ondevice.app.BuildConfig
import androidx.core.net.toUri
import net.openid.appauth.AuthorizationServiceConfiguration

object ProjectConfig {
  // Hugging Face Client ID — injected via BuildConfig from local.properties or CI secret.
  // Empty string disables OAuth flow gracefully (non-gated models still work).
  val clientId: String
    get() = BuildConfig.HF_CLIENT_ID

  // Registered redirect URI.
  //
  // The scheme needs to match the
  // "android.defaultConfig.manifestPlaceholders["appAuthRedirectScheme"]" field in
  // "build.gradle.kts".
  const val redirectUri = "ai.ondevice.app:/oauth2redirect"

  // OAuth 2.0 Endpoints (Authorization + Token Exchange)
  private const val authEndpoint = "https://huggingface.co/oauth/authorize"
  private const val tokenEndpoint = "https://huggingface.co/oauth/token"

  // OAuth service configuration (AppAuth library requires this)
  val authServiceConfig =
    AuthorizationServiceConfiguration(
      authEndpoint.toUri(), // Authorization endpoint
      tokenEndpoint.toUri(), // Token exchange endpoint
    )
}
