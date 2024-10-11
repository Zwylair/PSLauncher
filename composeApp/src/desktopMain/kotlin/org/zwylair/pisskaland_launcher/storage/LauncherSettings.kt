package org.zwylair.pisskaland_launcher.storage

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.MissingFieldException
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

val CONFIG_FILE = File("config.json")
val launcherSettings = LauncherSettings()

@Serializable
private data class Settings(
    val version: String = "1.0.0",
    var buildVersion: String,
    var nickName: String
)

class LauncherSettings() {
    var version by mutableStateOf("")
    var buildVersion by mutableStateOf("")
    var nickName by mutableStateOf("")

    init { readConfig() }

    private fun initEmpty(): Settings {
        return Settings(
            nickName = nickName,
            buildVersion = buildVersion
        )
    }

    fun saveConfig() { CONFIG_FILE.writeText(Json.encodeToString(initEmpty())) }

    @OptIn(ExperimentalSerializationApi::class)
    fun readConfig() {
        if (!CONFIG_FILE.exists()) { saveConfig() }

        var settings: Settings = initEmpty()

        try {
            settings = Json.decodeFromString<Settings>(CONFIG_FILE.readText())
        } catch (_: MissingFieldException) { }

        nickName = settings.nickName
        version = settings.version
        buildVersion = settings.buildVersion
    }
}
