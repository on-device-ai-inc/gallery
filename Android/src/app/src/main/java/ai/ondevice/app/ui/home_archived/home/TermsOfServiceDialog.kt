package ai.ondevice.app.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun TermsOfServiceDialog(
    onDismiss: () -> Unit,
    onAccept: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        title = {
            Text(
                "OnDevice AI App Terms of Service",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    "By using OnDevice AI, you accept our Terms of Service and Privacy Policy.",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    "OnDevice AI is built on open-source technology (Apache 2.0). " +
                    "Your purchase supports professional packaging, testing, updates, and customer support.",
                    style = MaterialTheme.typography.bodySmall
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    "AI Model Terms",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    "Some AI models have specific usage terms:\n\n" +
                    "• Gemma models: Subject to Google's Gemma Terms of Use and Prohibited Use Policy\n" +
                    "• Other models: Subject to their respective licenses\n\n" +
                    "By using these models, you agree to their terms.",
                    style = MaterialTheme.typography.bodySmall
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                TextButton(
                    onClick = { /* Open terms URL */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("View Full Terms of Service")
                }
                
                TextButton(
                    onClick = { /* Open privacy URL */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("View Privacy Policy")
                }
                
                TextButton(
                    onClick = { /* Open Gemma terms */ },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("View Gemma Terms of Use")
                }
            }
        },
        confirmButton = {
            Button(onClick = onAccept) {
                Text("Accept and Continue")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
