package org.zwylair.pisskaland_launcher

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.rememberScrollbarAdapter
import java.io.File

@Composable
@Preview
fun app() {
    MaterialTheme {
        val scrollState = rememberScrollState()
        var showSettingsDialog by remember { mutableStateOf(false) }
        var isDownloading by remember { mutableStateOf(false) }
        var downloadProgress by remember { mutableStateOf(0f) }
        var downloadObject by remember { mutableStateOf(Downloader("", File(""))) }

        Box(Modifier.fillMaxSize()) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState) // Make the column scrollable
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("PisskaLand Launcher", style = MaterialTheme.typography.h4)
                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(onClick = { /* Run Minecraft */ }) { Text("Run Minecraft") }

                    // Test Feature button
                    Button(onClick = {
                        if (!isDownloading) {
                            downloadObject = Downloader(
                                url = "https://file-examples.com/storage/fe36b23e6a66fc0679c1f86/2017/02/file-sample_1MB.doc",
                                outputFile = File("example_file.doc")
                            )

                            downloadObject.startDownload(
                                onProgress = { progress -> downloadProgress = progress },
                                onComplete = { isDownloading = false },
                                onError = { isDownloading = false }
                            )
                            isDownloading = true
                        }
                    }) {
                        if (isDownloading) Text("Downloading") else Text("Start downloading")
                    }
                }

                Button(onClick = { showSettingsDialog = true }) { Text("Edit Settings") }
                if (showSettingsDialog) {
                    settingsDialog(onDismiss = { showSettingsDialog = false } )
                }

                AnimatedVisibility(visible = isDownloading) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                Modifier.fillMaxWidth(0.7f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Downloading: ${downloadObject.outputFile.name}")
                                Spacer(modifier = Modifier.width(20.dp))
                                LinearProgressIndicator(progress = downloadProgress, modifier = Modifier.fillMaxWidth())
                            }
                        }
                    }
                }
            }

            VerticalScrollbar(
                adapter = rememberScrollbarAdapter(scrollState),
                modifier = Modifier.align(Alignment.CenterEnd).padding(end = 4.dp)
            )
        }
    }
}

@Composable
fun settingsDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Settings") },
        text = {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                var selectedMemory by remember { mutableStateOf(1) }
                Text("Max Memory Allocation:")
                Slider(
                    value = selectedMemory.toFloat(),
                    onValueChange = { selectedMemory = it.toInt() },
                    valueRange = 1f..8f,
                    steps = 7,
                    modifier = Modifier.fillMaxWidth(0.7f)
                )
                Text("${selectedMemory}GB")

                Spacer(modifier = Modifier.height(20.dp))

                var isDarkTheme by remember { mutableStateOf(true) }
                Text("Theme:")
                Row(
                    Modifier.fillMaxWidth(0.7f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = isDarkTheme,
                        onClick = { isDarkTheme = true }
                    )
                    Text("Dark")

                    Spacer(modifier = Modifier.width(20.dp))

                    RadioButton(
                        selected = !isDarkTheme,
                        onClick = { isDarkTheme = false }
                    )
                    Text("Light")
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Custom Downloader picker
                var jrePath by remember { mutableStateOf("") }
                Text("Custom Downloader Path:")
                OutlinedTextField(
                    value = jrePath,
                    onValueChange = { jrePath = it },
                    placeholder = { Text("Select Downloader Path") },
                    modifier = Modifier.fillMaxWidth(0.7f)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Additional Minecraft options with reset button in a row
                var additionalOptions by remember { mutableStateOf("") }
                Text("Additional Minecraft Options:")
                Row(
                    Modifier.fillMaxWidth(0.7f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedTextField(
                        value = additionalOptions,
                        onValueChange = { additionalOptions = it },
                        placeholder = { Text("Enter JVM arguments") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(onClick = { additionalOptions = "" }) {
                        Text("Reset to Defaults")
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}
