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

package ai.ondevice.app.ui.common

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.AllInclusive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.delay

// Color constants for privacy mode indication
private val OnDeviceGreen = Color(0xFF4CAF50)  // Pure on-device mode
private val HybridAmber = Color(0xFFFF9800)    // Web search enabled

/**
 * Persistent on-device indicator for all inference screens.
 * Reinforces privacy positioning at every interaction.
 */
@Composable
fun OnDeviceIndicator(
    modifier: Modifier = Modifier,
    variant: IndicatorVariant = IndicatorVariant.COMPACT
) {
    when (variant) {
        IndicatorVariant.ICON_ONLY -> IconOnlyIndicator(modifier)
        IndicatorVariant.COMPACT -> CompactOnDeviceIndicator(modifier)
        IndicatorVariant.PROMINENT -> ProminentOnDeviceIndicator(modifier)
        IndicatorVariant.SUBTLE -> SubtleOnDeviceIndicator(modifier)
    }
}

enum class IndicatorVariant {
    ICON_ONLY,  // Icon-only, 32dp bubble, dynamic color (green/amber)
    COMPACT,    // Small badge for top bars
    PROMINENT,  // Larger card for feature screens
    SUBTLE      // Minimal text-only for inline use
}

@Composable
private fun IconOnlyIndicator(
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.size(32.dp),
        color = OnDeviceGreen,
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Lock,
                contentDescription = "On-device processing",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun CompactOnDeviceIndicator(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.Lock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = "On-device",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ProminentOnDeviceIndicator(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.Lock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Column {
                Text(
                    text = "100% On-Device Processing",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Your data never leaves your phone",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun SubtleOnDeviceIndicator(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Rounded.Lock,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(12.dp)
        )
        Text(
            text = "On-device",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Offline capability badge.
 * Shows when network is unavailable to highlight competitive advantage.
 */
@Composable
fun OfflineCapabilityBadge(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var isOffline by remember { mutableStateOf(false) }

    // Check network status periodically
    LaunchedEffect(Unit) {
        while (true) {
            isOffline = !isNetworkAvailable(context)
            delay(5000) // Check every 5 seconds
        }
    }

    AnimatedVisibility(
        visible = isOffline,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Surface(
            modifier = modifier,
            color = MaterialTheme.colorScheme.secondaryContainer,
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.WifiOff,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "Works offline",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * Premium feature highlights for first-launch/onboarding.
 * Emphasizes competitive advantages vs cloud AI.
 */
@Composable
fun PrivacyAdvantagesCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Why Choose On-Device AI?",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )

            PrivacyAdvantageItem(
                icon = Icons.Rounded.Lock,
                title = "Complete Privacy",
                description = "Your conversations never leave your device"
            )

            PrivacyAdvantageItem(
                icon = Icons.Rounded.AllInclusive,
                title = "Unlimited Conversations",
                description = "No rate limits, no message caps, no subscriptions"
            )

            PrivacyAdvantageItem(
                icon = Icons.Rounded.Speed,
                title = "Instant Responses",
                description = "Zero network latency - AI responds immediately"
            )

            PrivacyAdvantageItem(
                icon = Icons.Rounded.WifiOff,
                title = "Works Offline",
                description = "AI that works on airplanes, in subways, anywhere"
            )
        }
    }
}

@Composable
private fun PrivacyAdvantageItem(
    icon: ImageVector,
    title: String,
    description: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.size(24.dp)
        )
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }
    }
}

/**
 * Comparison callout for competitive differentiation.
 * Can be used in settings or about screens.
 */
@Composable
fun CompetitiveComparisonCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "vs. Cloud AI Services",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            ComparisonRow(
                feature = "Privacy",
                onDevice = "✓ Complete",
                cloud = "✗ Uploaded to servers"
            )

            ComparisonRow(
                feature = "Cost",
                onDevice = "✓ Unlimited free",
                cloud = "✗ Pay per message"
            )

            ComparisonRow(
                feature = "Speed",
                onDevice = "✓ Instant",
                cloud = "✗ Network latency"
            )

            ComparisonRow(
                feature = "Offline",
                onDevice = "✓ Works anywhere",
                cloud = "✗ Requires internet"
            )
        }
    }
}

@Composable
private fun ComparisonRow(
    feature: String,
    onDevice: String,
    cloud: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = feature,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(0.3f)
        )
        Text(
            text = onDevice,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(0.35f)
        )
        Text(
            text = cloud,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.35f)
        )
    }
}

// Helper function
private fun isNetworkAvailable(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}
