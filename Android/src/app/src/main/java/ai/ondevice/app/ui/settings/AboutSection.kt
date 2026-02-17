package ai.ondevice.app.ui.settings

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
                    "Version 1.0.7",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    "OnDevice AI is built on open-source technology (Apache 2.0). " +
                    "Your purchase supports professional packaging, testing, updates, and customer support.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Legal buttons
                OutlinedButton(
                    onClick = { onOpenLicense("LICENSE") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("View Open Source License")
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedButton(
                    onClick = { onOpenLicense("NOTICE") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("View Legal Notices")
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedButton(
                    onClick = { onOpenLicense("ATTRIBUTIONS") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("View Third-Party Attributions")
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Divider()
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // External links
                TextButton(
                    onClick = { onOpenWebsite("https://github.com/on-device-ai-inc/on-device-ai") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("View Original Source Code")
                }
                
                TextButton(
                    onClick = { onOpenWebsite("https://ondevice.ai/legal/terms.html") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Terms of Service")
                }
                
                TextButton(
                    onClick = { onOpenWebsite("https://ondevice.ai/legal/privacy.html") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Privacy Policy")
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    "Support: support@ondevice.ai",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
