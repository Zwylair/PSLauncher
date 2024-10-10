package org.zwylair.pisskaland_launcher

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.zwylair.pisskaland_launcher.storage.BuildManifest
import org.zwylair.pisskaland_launcher.storage.Config
import pisskalandlauncher.composeapp.generated.resources.Res
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.security.MessageDigest
import kotlin.io.path.Path
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name
import kotlinx.coroutines.*
import java.io.ByteArrayInputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

fun readZipFile(zipBytes: ByteArray) {
    // Create a ByteArrayInputStream from the zip file bytes
    val inputStream = ByteArrayInputStream(zipBytes)
    val zipInputStream = ZipInputStream(inputStream)

    var entry: ZipEntry? = zipInputStream.nextEntry
    while (entry != null) {
        println("Reading file: ${entry.name}")

        // You can now read the file content as needed from zipInputStream
        if (!entry.isDirectory) {
            val fileContent = zipInputStream.readBytes()
            // Do something with the file content (e.g., process it)
            println("File content size: ${fileContent.size} bytes")
        }

        // Move to the next entry in the zip
        entry = zipInputStream.nextEntry
    }

    // Close the zip stream
    zipInputStream.close()
}

fun openZipFile(file: File): ZipInputStream {
    return ZipInputStream(ByteArrayInputStream(file.readBytes()))
}

fun getFilename(path: String): String { return File(path).name }

fun fileExists(path: String): Boolean { return File(path).exists() }

fun calculateSha512(input: ByteArray): String {
    val digest = MessageDigest.getInstance("SHA-512")
    val hashBytes = digest.digest(input)
    return hashBytes.joinToString("") { "%02x".format(it) }
}

fun checkSha512(filePath: String, shaCompareTo: String?): Boolean {
    if (!fileExists(filePath)) { return false }
    return if (shaCompareTo != null) calculateSha512(File(filePath).readBytes()) == shaCompareTo else true
}

fun getProcessOutput(process: Process): String {
    return BufferedReader(InputStreamReader(process.inputStream)).use { it.readText() }
}

fun String.isDigit(): Boolean { return this.toFloatOrNull() != null }
fun String.isAlpha(allowSpace: Boolean = false): Boolean  {
    return !(this.toCharArray().map {
        if (allowSpace) { it.isLetter() || it == " ".toCharArray()[0] }
        else { it.isLetter() }
    }.contains(false))
}

@OptIn(ExperimentalResourceApi::class)
private suspend fun getBuildManifest(): BuildManifest {
    val jsonData = Res.readBytes(Config.MINECRAFT_BUILD_CONFIG).toString(Charsets.UTF_8)
    return Json.decodeFromString<BuildManifest>(jsonData)
}

object MinecraftRunner {
    private val preLaunchHandlers = mutableListOf<(
        taskCreator: (String, String) -> Int,
        taskProgressUpdater: (Int, Float, String?) -> Unit
    ) -> Unit>()

    private val postLaunchHandlers = mutableListOf<(
        taskCreator: (String, String) -> Int,
        taskProgressUpdater: (Int, Float, String?) -> Unit
    ) -> Unit>()

    fun addPreLaunchHandler(
        handler: (
            taskCreator: (String, String) -> Int,
            taskProgressUpdater: (Int, Float, String?) -> Unit
        ) -> Unit
    ) { preLaunchHandlers.add(handler) }

    fun addPostLaunchHandler(
        handler: (
            taskCreator: (String, String) -> Int,
            taskProgressUpdater: (Int, Float, String?) -> Unit
        ) -> Unit
    ) { postLaunchHandlers.add(handler) }

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun checkDependencies(
        taskCreator: (String, String) -> Int,
        taskProgressUpdater: (Int, Float, String?) -> Unit
    ) {
        if (!fileExists(Config.PYTHON_SDK_PATH)) {
            println("Python SDK was not found. Downloading")

            if (!fileExists(Config.PYTHON_INSTALLER_PATH)) {
                val downloadTask = taskCreator("Downloading", "Python SDK")
                val outputFile = File(Config.PYTHON_INSTALLER_PATH)

                suspendCancellableCoroutine<Unit> { continuation ->
                    Downloader(url = Config.PYTHON_INSTALLER_URL, outputFile = outputFile)
                        .startDownload(
                            onProgress = { progress -> taskProgressUpdater(downloadTask, progress, null) },
                            onComplete = { continuation.resume(Unit) { } },
                            onError = { e ->
                                taskProgressUpdater(downloadTask, 1f, null)
                                continuation.resume(Unit) { }
                            }
                        )
                }
            }

            val command = mutableListOf(Config.PYTHON_INSTALLER_PATH)
            command.addAll(listOf("-o\"${File("").absolutePath}\"", "-y"))

            try {
                val processBuilder = ProcessBuilder(command)
                processBuilder.directory(File("."))
                processBuilder.redirectErrorStream(true)

                val process = processBuilder.start()
                val result = process.waitFor()
                println("python sdk installer exited with code: $result")
            }
            catch (e: IOException) { e.printStackTrace() }
            catch (e: InterruptedException) { e.printStackTrace(); Thread.currentThread().interrupt() }
        }

        if (!fileExists("${Config.PYTHON_SDK_PATH}/Lib/site-packages/minecraft_launcher_lib")) {
            println("minecraft-launcher-lib not found. Installing")
            val installLibsTask = taskCreator("Installing libs", "Processing")
            val command = mutableListOf(
                "${Config.PYTHON_SDK_PATH}/python.exe",
                "-m", "pip", "install", "minecraft-launcher-lib"
            )

            try {
                val processBuilder = ProcessBuilder(command)
                processBuilder.directory(File("."))
                processBuilder.redirectErrorStream(true)

                val process = processBuilder.start()
                val resultString = BufferedReader(InputStreamReader(process.inputStream)).use { it.readText() }
                println(resultString)
                taskProgressUpdater(installLibsTask, 1f, null)
            }
            catch (e: IOException) { e.printStackTrace() }
            catch (e: InterruptedException) { e.printStackTrace(); Thread.currentThread().interrupt() }
        }

        if (
            !fileExists("${Config.MINECRAFT_PARENT_PATH}/runtime/java-runtime-gamma/windows-x64/java-runtime-gamma/bin/java.exe") ||
            !fileExists("${Config.MINECRAFT_PARENT_PATH}/libraries") ||
            !fileExists("${Config.MINECRAFT_PARENT_PATH}/assets") ||
            !fileExists("${Config.MINECRAFT_PARENT_PATH}/versions")
        ) {
            println("One or some of minecraft requirements was not found. Repairing")

            val command = mutableListOf(
                "${Config.PYTHON_SDK_PATH}/python.exe",
                "scripts/setup_dependencies.py"
            )

            try {
                val processBuilder = ProcessBuilder(command)
                processBuilder.directory(File("."))
                processBuilder.redirectErrorStream(true)

                val process = processBuilder.start()

                val outputJob = CoroutineScope(Dispatchers.IO).launch {
                    val reader = BufferedReader(InputStreamReader(process.inputStream))
                    var line: String?

                    var task = taskCreator("Downloading Minecraft", "Processing")

                    while (process.isAlive) {
                        line = reader.readLine()

                        if (line == null) { taskProgressUpdater(task, 1f, line); break }
                        if (line.replace("/", "").isDigit()) {
                            val (currentProgress, totalProgress) = line.split("/").map { it.toFloat() }
                            val progressInPercent = currentProgress / totalProgress

                            taskProgressUpdater(task, progressInPercent, null)
                            if (progressInPercent == 1f) { task = taskCreator("Downloading Minecraft", "Processing") }
                        }
                        else if (line.isAlpha(allowSpace = true)) { taskProgressUpdater(task, 0f, line) }
                    }
                }

                outputJob.join()
                while (outputJob.isActive) { delay(0) }
            }
            catch (e: IOException) { e.printStackTrace() }
            catch (e: InterruptedException) { e.printStackTrace(); Thread.currentThread().interrupt() }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun checkModBuild(
        taskCreator: (String, String) -> Int,
        taskProgressUpdater: (Int, Float, String?) -> Unit
    ) {
        val buildManifest = getBuildManifest()

        for (mod in buildManifest.files) {
            val outputFile = File("${Config.MINECRAFT_PARENT_PATH}/${mod.path}")
            if (fileExists(outputFile.path) && checkSha512(outputFile.path, mod.hashes.sha512)) { continue }
            val downloadTask = taskCreator("Download mod", getFilename(mod.path))

            suspendCancellableCoroutine<Unit> { continuation ->
                Downloader(url = mod.downloads[0], outputFile = outputFile)
                    .startDownload(
                        onProgress = { progress -> taskProgressUpdater(downloadTask, progress, null) },
                        onComplete = { continuation.resume(Unit) { } },
                        onError = { e ->
                            taskProgressUpdater(downloadTask, 1f, null)
                            continuation.resume(Unit) { }
                        }
                    )
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun cleanMinecraftFolder(
        taskCreator: (String, String) -> Int,
        taskProgressUpdater: (Int, Float, String?) -> Unit
    ) {
        val cleanMinecraftFolderTask = taskCreator("Cleaning", "Mod folder")
        println("Searching minecraft folder for trash")

        val buildManifest = getBuildManifest()
        val verifiedModList = mutableListOf<String>()
        val verifiedResourcePacksList = mutableListOf<String>()
        val modsPathList = Path("${Config.MINECRAFT_PARENT_PATH}/mods").listDirectoryEntries()
        val resourcePacksPathList = Path("${Config.MINECRAFT_PARENT_PATH}/resourcepacks").listDirectoryEntries()
        val pointsPerItem = 1f / modsPathList.size + resourcePacksPathList.size
        var itemCounter = 0

        buildManifest.files.forEach {
            when (File(it.path).parent) {
                "mods" -> { verifiedModList.add(getFilename(it.path)) }
                "resourcepacks" -> { verifiedResourcePacksList.add(getFilename(it.path)) }
            }
        }

        modsPathList.forEach {
            if (!verifiedModList.contains(it.name)) { it.toFile().delete() }
            itemCounter++
            taskProgressUpdater(cleanMinecraftFolderTask, pointsPerItem * itemCounter, "Cleaning mods folder")
        }

        resourcePacksPathList.forEach {
            if (!verifiedResourcePacksList.contains(it.name)) { it.toFile().delete() }
            itemCounter++
            taskProgressUpdater(cleanMinecraftFolderTask, pointsPerItem * itemCounter, "Cleaning RP folder")
        }
    }

    suspend fun launchGame(
        taskCreator: (String, String) -> Int,
        taskProgressUpdater: (Int, Float, String?) -> Unit
    ) {
        val preLaunchTasks = mapOf(
            ::checkDependencies to "Checking dependencies",
            ::checkModBuild to "Checking mod build",
            ::cleanMinecraftFolder to "Cleaning minecraft"
        )
        val oneProgressTaskWeight = 1f / (preLaunchTasks.size + preLaunchHandlers.size + postLaunchHandlers.size)
        var tasksDone = 0
        val mainTask = taskCreator("Launching Minecraft", "Processing")
        println("launchGame task ran")

        for ((task, taskDesc) in preLaunchTasks) {
            taskProgressUpdater(mainTask, oneProgressTaskWeight * tasksDone, taskDesc)
            println(taskDesc)
            task(taskCreator, taskProgressUpdater)
            tasksDone++
            taskProgressUpdater(mainTask, oneProgressTaskWeight * tasksDone, null)
        }

        taskProgressUpdater(mainTask, oneProgressTaskWeight * tasksDone, "executing preLaunchHooks")
        println("executing preLaunchHooks")

        for (handler in preLaunchHandlers) {
            handler(taskCreator, taskProgressUpdater)
            tasksDone++
            taskProgressUpdater(mainTask, oneProgressTaskWeight * tasksDone, null)
        }

        val command = mutableListOf(
            "${Config.PYTHON_SDK_PATH}/python.exe",
            "scripts/run_minecraft.py"
        )

        try {
            val processBuilder = ProcessBuilder(command)
            processBuilder.directory(File("."))
            processBuilder.redirectErrorStream(true)

            val process = processBuilder.start()
            val outputJob = CoroutineScope(Dispatchers.IO).launch {
                val reader = BufferedReader(InputStreamReader(process.inputStream))
                var line: String?

                while (process.isAlive) {
                    line = reader.readLine()
                    println(line)
                }
            }

            outputJob.join()
            while (outputJob.isActive) { delay(0) }
        }
        catch (e: IOException) { e.printStackTrace() }
        catch (e: InterruptedException) { e.printStackTrace(); Thread.currentThread().interrupt() }

        taskProgressUpdater(mainTask, oneProgressTaskWeight * tasksDone, "executing postLaunchHooks")
        println("executing postLaunchHooks")

        for (handler in postLaunchHandlers) {
            handler(taskCreator, taskProgressUpdater) // Execute post-launch handler sequentially
            tasksDone++
            taskProgressUpdater(mainTask, oneProgressTaskWeight * tasksDone, null)
        }

        println("launchGame task finished")
    }
}
