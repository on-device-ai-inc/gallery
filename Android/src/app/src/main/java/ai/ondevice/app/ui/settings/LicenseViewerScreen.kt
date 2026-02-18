package ai.ondevice.app.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LicenseViewerScreen(
    licenseType: String,  // "LICENSE", "NOTICE", or "ATTRIBUTIONS"
    onBackPressed: () -> Unit
) {
    val context = LocalContext.current
    var licenseText by remember { mutableStateOf("Loading...") }
    
    LaunchedEffect(licenseType) {
        try {
            val filename = when (licenseType) {
                "LICENSE" -> "legal/LICENSE.txt"
                "NOTICE" -> "legal/NOTICE.txt"
                "ATTRIBUTIONS" -> "legal/ATTRIBUTIONS.txt"
                else -> "legal/LICENSE.txt"
            }
            licenseText = context.assets.open(filename).bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            licenseText = "Error loading license: ${e.message}"
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(when (licenseType) {
                        "LICENSE" -> "Open Source License"
                        "NOTICE" -> "Legal Notices"
                        "ATTRIBUTIONS" -> "Third-Party Attributions"
                        else -> "License"
                    })
                },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                text = licenseText,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
