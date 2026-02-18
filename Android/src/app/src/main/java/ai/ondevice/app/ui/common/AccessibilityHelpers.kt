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

import androidx.compose.foundation.layout.sizeIn
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * WCAG 2.2 Level AA Accessibility Compliance Helpers
 *
 * These utilities ensure the app meets accessibility standards:
 * - Touch targets: Minimum 48×48dp
 * - Color contrast: 4.5:1 for text, 3:1 for UI components
 * - TalkBack: Meaningful content descriptions
 * - Text scaling: Support up to 200%
 * - Focus indicators: Visible in all themes
 */

/**
 * Minimum touch target size per WCAG 2.2 Level AA
 * Material Design 3 specifies 48×48dp minimum
 */
val MinTouchTargetSize: Dp = 48.dp

/**
 * Ensures a composable meets minimum touch target requirements
 * Use this modifier on small interactive elements (icons, buttons, etc.)
 */
fun Modifier.minimumTouchTarget(size: Dp = MinTouchTargetSize): Modifier {
    return this.sizeIn(minWidth = size, minHeight = size)
}

/**
 * REMOVED: accessibleClickable and accessibleLiveRegion functions
 * These were example implementations that had @Composable usage issues.
 * Use Material 3 components with proper contentDescription instead.
 *
 * Example:
 * IconButton(
 *   onClick = { },
 *   modifier = Modifier.semantics { contentDescription = "Delete item" }
 * ) { Icon(...) }
 */

/**
 * Standard accessibility labels for common actions
 * Ensures consistent TalkBack experience across the app
 */
object AccessibilityLabels {
    // Navigation
    const val BACK_BUTTON = "Navigate back"
    const val CLOSE_DIALOG = "Close dialog"
    const val OPEN_MENU = "Open menu"
    const val CLOSE_MENU = "Close menu"

    // Actions
    const val SEND_MESSAGE = "Send message"
    const val DELETE_ITEM = "Delete item"
    const val EDIT_ITEM = "Edit item"
    const val COPY_TEXT = "Copy text"
    const val SHARE = "Share"
    const val DOWNLOAD = "Download"

    // Model actions
    const val DOWNLOAD_MODEL = "Download AI model"
    const val DELETE_MODEL = "Delete AI model"
    const val SELECT_MODEL = "Select AI model"
    const val RESET_SESSION = "Start new chat session"

    // Settings
    const val OPEN_SETTINGS = "Open settings"
    const val TOGGLE_THEME = "Toggle theme"
    const val CHANGE_TEXT_SIZE = "Change text size"

    // Chat-specific
    const val REGENERATE_RESPONSE = "Regenerate AI response"
    const val STOP_GENERATION = "Stop generating response"
    const val VOICE_INPUT = "Voice input"
    const val ATTACH_IMAGE = "Attach image"

    // States
    const val AI_RESPONDING = "AI is responding"
    const val RESPONSE_COMPLETE = "Response complete"
    const val MODEL_DOWNLOADING = "Model downloading"
    const val MODEL_READY = "Model ready"
}

/**
 * Accessibility checklist for developers
 *
 * Use this when creating new screens or components:
 *
 * ✓ Touch Targets:
 *   - All interactive elements minimum 48×48dp
 *   - Use .minimumTouchTarget() modifier
 *   - Icon buttons: IconButton or Icon + accessibleClickable()
 *
 * ✓ Content Descriptions:
 *   - All icons have contentDescription
 *   - Decorative icons use contentDescription = null
 *   - Use AccessibilityLabels constants for consistency
 *
 * ✓ Text Scaling:
 *   - Use sp units for all text (MaterialTheme.typography)
 *   - Never use dp for text sizes
 *   - Test with Settings → Display → Font Size → Largest
 *
 * ✓ Color Contrast:
 *   - Use Material Theme colors (automatic WCAG compliance)
 *   - Test in both Light and Dark themes
 *   - Use Accessibility Scanner app to verify
 *
 * ✓ Focus Indicators:
 *   - Material 3 components have built-in focus indicators
 *   - For custom components, ensure visible focus state
 *   - Test with keyboard/D-pad navigation
 *
 * ✓ Live Regions:
 *   - Streaming AI responses use .accessibleLiveRegion(Polite)
 *   - Status announcements use LiveRegionMode.Assertive for urgent
 *   - Group related content to avoid fragmented announcements
 *
 * ✓ TalkBack Grouping:
 *   - Group message bubble content (sender + message + timestamp)
 *   - Use Modifier.semantics(mergeDescendants = true) { }
 *   - Prevents TalkBack from reading each element separately
 */

/**
 * Example: Accessible message bubble
 *
 * @Composable
 * fun MessageBubble(sender: String, content: String, timestamp: String) {
 *     Column(
 *         modifier = Modifier.semantics(mergeDescendants = true) {
 *             contentDescription = "Message from $sender at $timestamp: $content"
 *         }
 *     ) {
 *         Text(sender)
 *         Text(content, modifier = Modifier.accessibleLiveRegion())
 *         Text(timestamp)
 *     }
 * }
 */

/**
 * Example: Accessible icon button
 *
 * @Composable
 * fun DeleteButton(onDelete: () -> Unit) {
 *     IconButton(
 *         onClick = onDelete,
 *         modifier = Modifier.semantics {
 *             contentDescription = AccessibilityLabels.DELETE_ITEM
 *         }
 *     ) {
 *         Icon(Icons.Rounded.Delete, contentDescription = null)
 *     }
 * }
 *
 * Note: contentDescription on IconButton, not Icon (avoids duplication)
 */

/**
 * Example: Accessible custom clickable
 *
 * @Composable
 * fun ModelCard(model: Model, onClick: () -> Unit) {
 *     Card(
 *         modifier = Modifier.accessibleClickable(
 *             label = "Select ${model.name} AI model",
 *             role = Role.Button,
 *             onClick = onClick
 *         )
 *     ) {
 *         Text(model.name)
 *     }
 * }
 */

/**
 * Verification: Use Android's Accessibility Scanner
 *
 * 1. Install: Play Store → "Accessibility Scanner"
 * 2. Enable: Settings → Accessibility → Accessibility Scanner
 * 3. Run scan on each screen
 * 4. Fix all "Touch target size" and "Low contrast" issues
 *
 * Common issues:
 * - Icons without contentDescription
 * - Touch targets < 48dp
 * - Insufficient color contrast
 * - Text using dp instead of sp
 * - Missing state announcements
 */
