package org.zwylair.pisskaland_launcher

//import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.ExperimentalResourceApi
import pisskalandlauncher.composeapp.generated.resources.Res
import java.io.File

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

    private suspend fun checkDependencies(
        taskCreator: (String, String) -> Int,
        taskProgressUpdater: (Int, Float) -> Unit
    ) {
//        delay(1000)
    }

    @OptIn(ExperimentalResourceApi::class)
    private suspend fun checkLibraries(
        taskCreator: (String, String) -> Int,
        taskProgressUpdater: (Int, Float) -> Unit
    ) {
        // Assuming Res.readBytes works correctly, and jsonData is loaded properly
        val jsonData = Res.readBytes(Config.USED_MINECRAFT_VERSION_CONFIG).toString(Charsets.UTF_8)
        val versionManifest = Json.decodeFromString<VersionManifest>(jsonData)

        File("lib/").mkdirs()
        File("versions/").mkdirs()

        Downloader(url = "", outputFile = File(""))
        println(versionManifest)
        // TODO
//        delay(1000)
    }

    private suspend fun checkModBuild(
        taskCreator: (String, String) -> Int,
        taskProgressUpdater: (Int, Float) -> Unit
    ) {
//        delay(1000)
    }

    private suspend fun cleanMinecraftFolder(
        taskCreator: (String, String) -> Int,
        taskProgressUpdater: (Int, Float) -> Unit
    ) {
//        delay(1000)
    }

    suspend fun launch(
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

        preLaunchTasks.forEach {
            it(taskCreator, taskProgressUpdater)
            tasksDone++
            taskProgressUpdater(mainTask, oneProgressTaskWeight * tasksDone)
        }

        preLaunchHandlers.forEach { it(taskCreator, taskProgressUpdater) }
        postLaunchHandlers.forEach { it(taskCreator, taskProgressUpdater) }
    }
}
