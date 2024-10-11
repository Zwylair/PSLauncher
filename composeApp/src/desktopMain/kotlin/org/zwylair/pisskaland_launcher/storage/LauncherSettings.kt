package org.zwylair.pisskaland_launcher.storage

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

val CONFIG_FILE = File("config.json")
val launcherSettings = LauncherSettings()


@Serializable
private data class Settings(
    var nickName: String
)

class LauncherSettings() {
    var nickName by mutableStateOf("")

    init {
        readConfig()
    }

    fun saveConfig() {
        CONFIG_FILE.writeText(Json.encodeToString(
            Settings(
                nickName = nickName
            )
        ))
    }

    fun readConfig() {
        if (!CONFIG_FILE.exists()) { saveConfig() }
        val settings = Json.decodeFromString<Settings>(CONFIG_FILE.readText())

        nickName = settings.nickName
    }
}
