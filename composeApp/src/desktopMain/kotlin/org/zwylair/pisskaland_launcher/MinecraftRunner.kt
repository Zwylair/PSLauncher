package org.zwylair.pisskaland_launcher

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.zwylair.pisskaland_launcher.storage.Config
import org.zwylair.pisskaland_launcher.storage.VersionManifest
import pisskalandlauncher.composeapp.generated.resources.Res
import java.io.File
import java.io.IOException
import java.security.MessageDigest

fun getFilename(path: String): String { return File(path).name }

fun fileExists(path: String): Boolean { return File(path).exists() }

fun calculateSha1(input: ByteArray): String {
    val digest = MessageDigest.getInstance("SHA-1")
    val hashBytes = digest.digest(input)
    return hashBytes.joinToString("") { "%02x".format(it) }
}

fun calculateSha256(input: ByteArray): String {
    val digest = MessageDigest.getInstance("SHA-256")
    val hashBytes = digest.digest(input)
    return hashBytes.joinToString("") { "%02x".format(it) }
}

fun checkSha1(filePath: String, shaCompareTo: String?): Boolean {
    if (!fileExists(filePath)) { return false }
    return if (shaCompareTo != null) calculateSha1(File(filePath).readBytes()) == shaCompareTo else true
}

fun checkSha256(filePath: String, shaCompareTo: String?): Boolean {
    if (!fileExists(filePath)) { return false }
    return if (shaCompareTo != null) calculateSha256(File(filePath).readBytes()) == shaCompareTo else true
}

private fun runMsiInstaller(): Int {
    try {
        // Build the command with msiexec and the MSI file path along with the provided arguments
        val command = mutableListOf("msiexec", "/i", Config.JRE_INSTALLER_OUTPUT_FILENAME).apply {
            addAll(Config.JRE_INSTALLER_KEYS) // Add additional arguments
        }

        // Create the process builder to execute the command
        val processBuilder = ProcessBuilder(command)
        processBuilder.directory(File("."))
        processBuilder.redirectErrorStream(true)

        // Start the process
        val process = processBuilder.start()

        // Wait for the process to finish and return the exit code
        val exitCode = process.waitFor()

        // Return the exit code (0 means success, anything else usually means an error)
        return exitCode

    } catch (e: IOException) {
        e.printStackTrace()
    } catch (e: InterruptedException) {
        e.printStackTrace()
        Thread.currentThread().interrupt() // Restore interrupted status
    }

    // Return an error code if the process fails
    return -1
}

object MinecraftRunner {
    private val preLaunchHandlers = mutableListOf<(
        taskCreator: (String, String) -> Int,
        taskProgressUpdater: (Int, Float) -> Unit
    ) -> Unit>()

    private val postLaunchHandlers = mutableListOf<(
        taskCreator: (String, String) -> Int,
        taskProgressUpdater: (Int, Float) -> Unit
    ) -> Unit>()

    fun addPreLaunchHandler(
        handler: (
            taskCreator: (String, String) -> Int,
            taskProgressUpdater: (Int, Float) -> Unit
        ) -> Unit
    ) { preLaunchHandlers.add(handler) }

    fun addPostLaunchHandler(
        handler: (
            taskCreator: (String, String) -> Int,
            taskProgressUpdater: (Int, Float) -> Unit
        ) -> Unit
    ) { postLaunchHandlers.add(handler) }

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun checkDependencies(
        taskCreator: (String, String) -> Int,
        taskProgressUpdater: (Int, Float) -> Unit
    ) {
    }

    @OptIn(ExperimentalResourceApi::class, ExperimentalCoroutinesApi::class)
    private suspend fun checkLibraries(
        taskCreator: (String, String) -> Int,
        taskProgressUpdater: (Int, Float) -> Unit
    ) {
        val jsonData = Res.readBytes(Config.USED_MINECRAFT_VERSION_CONFIG).toString(Charsets.UTF_8)
        val versionManifest = Json.decodeFromString<VersionManifest>(jsonData)

        val clientJarPath = "versions/${versionManifest.id}/client.jar"
        if (
            fileExists(clientJarPath) && !checkSha1(clientJarPath, versionManifest.downloads.client.sha1) ||
            !fileExists(clientJarPath)
        ) {
            val downloadClientTask = taskCreator("Downloading client", clientJarPath)

            suspendCancellableCoroutine<Unit> { continuation ->
                Downloader(
                    url = versionManifest.downloads.client.url,
                    outputFile = File(clientJarPath)
                ).startDownload(
                    onProgress = { progress -> taskProgressUpdater(downloadClientTask, progress) },
                    onComplete = {
                        taskProgressUpdater(downloadClientTask, 1f)
                        continuation.resume(Unit) { } // Resume coroutine once the download completes
                    },
                    onError = { e ->
                        println(e)
                        taskProgressUpdater(downloadClientTask, 1f)
                        continuation.resume(Unit) { } // Resume coroutine even on error to avoid hanging
                    }
                )
            }
        }

        val classpath = mutableListOf<String>()
        for (library in versionManifest.libraries) {
            val artifact = library.downloads.artifact
            val outputFile = File("lib/${artifact.path}")

            if (library.include_in_classpath) { classpath.add(outputFile.absolutePath) }
            if (fileExists(outputFile.path) && checkSha1(outputFile.path, artifact.sha1)) { continue }

            val downloadTask = taskCreator("Download lib", getFilename(artifact.path))

            suspendCancellableCoroutine<Unit> { continuation ->
                Downloader(url = artifact.url, outputFile = outputFile)
                    .startDownload(
                        onProgress = { progress -> taskProgressUpdater(downloadTask, progress) },
                        onComplete = {
                            taskProgressUpdater(downloadTask, 1f)
                            continuation.resume(Unit) { } // Resume coroutine once the download completes
                        },
                        onError = { e ->
                            taskProgressUpdater(downloadTask, 1f)
                            continuation.resume(Unit) { } // Resume coroutine even on error to avoid hanging
                        }
                    )

            }
        }
    }

    private suspend fun checkModBuild(
        taskCreator: (String, String) -> Int,
        taskProgressUpdater: (Int, Float) -> Unit
    ) {
    }

    private suspend fun cleanMinecraftFolder(
        taskCreator: (String, String) -> Int,
        taskProgressUpdater: (Int, Float) -> Unit
    ) {
    }

    suspend fun launchGame(
        taskCreator: (String, String) -> Int,
        taskProgressUpdater: (Int, Float) -> Unit
    ) {
        val preLaunchTasks = listOf(
            ::checkDependencies,
            ::cleanMinecraftFolder,
            ::checkLibraries,
            ::checkModBuild,
        )
        val oneProgressTaskWeight = 1f / (preLaunchTasks.size + preLaunchHandlers.size + postLaunchHandlers.size)
        var tasksDone = 0
        val mainTask = taskCreator("Launching Minecraft", "Processing")

        // Execute each task sequentially
        // Sequentially execute each pre-launch task
        for (task in preLaunchTasks) {
            task(taskCreator, taskProgressUpdater) // Execute task sequentially
            tasksDone++
            taskProgressUpdater(mainTask, oneProgressTaskWeight * tasksDone)
        }

        // Execute pre-launch handlers sequentially
        for (handler in preLaunchHandlers) {
            handler(taskCreator, taskProgressUpdater) // Execute handler sequentially
            tasksDone++
            taskProgressUpdater(mainTask, oneProgressTaskWeight * tasksDone)
        }

//        val classpath = mutableListOf<String>()
//        for (library in versionManifest.libraries) {
//            if (library.include_in_classpath) {
//                classpath.add(outputFile.absolutePath)
//            }
//        }

            // Execute post-launch handlers sequentially
        for (handler in postLaunchHandlers) {
            handler(taskCreator, taskProgressUpdater) // Execute post-launch handler sequentially
            tasksDone++
            taskProgressUpdater(mainTask, oneProgressTaskWeight * tasksDone)
        }

        println("launchGame task finished")
    }
}
