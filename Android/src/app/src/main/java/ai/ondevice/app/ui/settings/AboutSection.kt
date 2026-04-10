package ai.ondevice.app.ui.settings

import ai.ondevice.app.BuildConfig
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AboutSection(
    onOpenLicense: (String) -> Unit,
    onOpenWebsite: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            "About OnDevice AI",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Version ${BuildConfig.VERSION_NAME}",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "OnDevice AI is built on Google AI Edge Gallery (Apache 2.0). " +
                    "Your purchase supports professional packaging, testing, updates, and customer support.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Legal buttons
                OutlinedButton(
                    onClick = { onOpenLicense("NOTICE") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("View Legal Notices & Attribution")
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = { onOpenLicense("LICENSE") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("View Open Source License (Apache 2.0)")
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = { onOpenLicense("ATTRIBUTIONS") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("View Third-Party Attributions")
                }

                Spacer(modifier = Modifier.height(16.dp))

                HorizontalDivider()

                Spacer(modifier = Modifier.height(16.dp))

                // External links
                TextButton(
                    onClick = { onOpenWebsite("https://on-device.org/terms") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Terms of Service")
                }

                TextButton(
                    onClick = { onOpenWebsite("https://on-device.org/privacy") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Privacy Policy")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "On Device AI Inc. — Ottawa, Canada",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Support: admin@on-device.app",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
