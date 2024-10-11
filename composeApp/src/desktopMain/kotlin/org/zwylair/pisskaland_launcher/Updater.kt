package org.zwylair.pisskaland_launcher

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.awt.Desktop
import java.net.URI
import org.zwylair.pisskaland_launcher.storage.Config
import org.zwylair.pisskaland_launcher.storage.launcherSettings

fun openUrlInBrowser(url: String) {
    if (Desktop.isDesktopSupported()) {
        val desktop = Desktop.getDesktop()
        if (desktop.isSupported(Desktop.Action.BROWSE)) { desktop.browse(URI(url)) }
        else { println("Browsing is not supported on this system.") }
    }
    else { println("Desktop operations are not supported on this system.") }
}

object Updater {
    suspend fun fetchVersionFromServer(): String {
        return suspendCancellableCoroutine { continuation ->
            MemoryDownloader(Config.LATEST_LAUNCHER_VERSION_FILE_URL)
                .startDownload(
                    onProgress = {},
                    onComplete = { continuation.resumeWith(Result.success(it.decodeToString())) },
                    onError = { continuation.resumeWith(Result.failure(it)) }
                )
        }
    }

    @Composable
    fun checkForUpdate() {
        val coroutineScope = rememberCoroutineScope()
        var isUpdateAvailable by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf<String?>(null) }

        LaunchedEffect(Unit) {
            coroutineScope.launch {
                try {
                    val serverVersion = fetchVersionFromServer()
                    if (launcherSettings.version != serverVersion) { isUpdateAvailable = true }
                } catch (e: Exception) { errorMessage = e.localizedMessage }
            }
        }

        if (isUpdateAvailable) { openUpdateAlert() }
        errorMessage?.let { Text("Error: $it") }
    }
}

@Composable
fun openUpdateAlert() {
    AlertDialog(
        onDismissRequest = { },
        title = { Text("Update is available") },
        text = {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("A new version of the launcher is available. Please update.")
            }
        },
        confirmButton = {
            Button(onClick = { openUrlInBrowser(Config.LAUNCHER_DOWNLOAD_PAGE_URL) })
            { Text("Update") }
        }
    )
}
