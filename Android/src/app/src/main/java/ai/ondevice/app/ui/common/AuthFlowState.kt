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

/**
 * State machine for OAuth + License acceptance flow.
 *
 * Represents all possible states in the authentication and license
 * acceptance workflow for gated models.
 */
sealed class AuthFlowState {
  /** Initial state - no action in progress */
  data object Idle : AuthFlowState()

  /** Validating existing OAuth token */
  data object CheckingToken : AuthFlowState()

  /** Pre-OAuth warning dialog displayed (Step 1/2) */
  data object ShowingPreOAuthDialog : AuthFlowState()

  /** OAuth in progress - Custom Tab opened */
  data object OAuthInProgress : AuthFlowState()

  /** OAuth completed successfully */
  data object OAuthCompleted : AuthFlowState()

  /** Checking if model requires license acceptance */
  data object CheckingLicense : AuthFlowState()

  /** License summary dialog displayed (Step 2/2) */
  data object ShowingLicenseSummary : AuthFlowState()

  /** Full license WebView displayed */
  data object ShowingLicenseWebView : AuthFlowState()

  /** License accepted, ready to download */
  data object LicenseAccepted : AuthFlowState()

  /** Download in progress */
  data object Downloading : AuthFlowState()

  /** Error state with error message */
  data class Error(val message: String) : AuthFlowState()
}

/**
 * Events that trigger state transitions in the auth flow.
 */
sealed class AuthFlowEvent {
  /** User clicked download button */
  data object DownloadClicked : AuthFlowEvent()

  /** Token check completed */
  data class TokenCheckCompleted(val isValid: Boolean) : AuthFlowEvent()

  /** User confirmed Pre-OAuth dialog */
  data object PreOAuthConfirmed : AuthFlowEvent()

  /** User cancelled Pre-OAuth dialog */
  data object PreOAuthCancelled : AuthFlowEvent()

  /** OAuth completed successfully */
  data object OAuthSucceeded : AuthFlowEvent()

  /** OAuth failed */
  data class OAuthFailed(val error: String) : AuthFlowEvent()

  /** User cancelled OAuth */
  data object OAuthCancelled : AuthFlowEvent()

  /** License check completed */
  data class LicenseCheckCompleted(val requiresLicense: Boolean) : AuthFlowEvent()

  /** User confirmed license summary */
  data object LicenseSummaryConfirmed : AuthFlowEvent()

  /** User cancelled license summary */
  data object LicenseSummaryCancelled : AuthFlowEvent()

  /** User accepted license in WebView */
  data object LicenseAccepted : AuthFlowEvent()

  /** Download started */
  data object DownloadStarted : AuthFlowEvent()

  /** Error occurred */
  data class ErrorOccurred(val message: String) : AuthFlowEvent()

  /** User dismissed error */
  data object ErrorDismissed : AuthFlowEvent()
}
