package ai.ondevice.app.ui.common.tos

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Dialog that displays Google's Gemma Terms of Use and requires explicit user acceptance
 * before allowing Gemma model downloads.
 *
 * This component satisfies Section 3.1 of the Gemma Terms of Use which requires
 * downstream redistributors to:
 * 1. Provide notice of the Gemma Terms
 * 2. Require agreement to use restrictions
 * 3. Include attribution
 */
@Composable
fun GemmaTermsDialog(
    modelName: String = "Gemma",
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDecline,
        icon = {
            Icon(
                Icons.Rounded.Info,
                contentDescription = "",
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                "Gemma Terms of Use Required",
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
                    "To download $modelName, you must accept Google's Gemma Terms of Use.",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "Key Terms Summary:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Key prohibitions - make them clear
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row {
                            Icon(
                                Icons.Rounded.Error,
                                contentDescription = "",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Prohibited Uses",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "• No illegal activities\n" +
                            "• No harm to minors\n" +
                            "• No generating/promoting violence\n" +
                            "• No fraud or deception\n" +
                            "• No defamation or harassment",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Permitted uses
                Text(
                    "✓ Personal use\n" +
                    "✓ Research and education\n" +
                    "✓ Commercial applications (with restrictions)\n" +
                    "✓ Model modifications and derivatives",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "Important:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Google may restrict usage remotely if terms are violated. " +
                    "By clicking \"I Accept\" below, you agree to comply with all Gemma terms and restrictions.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Links to full terms
                TextButton(
                    onClick = {
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://ai.google.dev/gemma/terms")
                        )
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Read Full Gemma Terms of Use")
                }

                TextButton(
                    onClick = {
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://ai.google.dev/gemma/prohibited_use_policy")
                        )
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Read Prohibited Use Policy")
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Attribution (required by license)
                Text(
                    "Gemma is provided under and subject to the Gemma Terms of Use found at ai.google.dev/gemma/terms",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onAccept,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("I Accept")
            }
        },
        dismissButton = {
            TextButton(onClick = onDecline) {
                Text("Decline")
            }
        }
    )
}
