package org.zwylair.pisskaland_launcher

import kotlinx.serialization.Serializable

object Config {
    const val JRE_DOWNLOAD_LINK = "https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.12%2B7/OpenJDK17U-jre_x86-32_windows_hotspot_17.0.12_7.msi"
    const val JRE_OUTPUT_FILENAME = "OpenJDK17U-jre_x86-32_windows_hotspot_17.0.12_7.msi"
    const val JRE_RUNTIME_PATH = "jre_runtime/"
    const val JRE_SHA256_DOWNLOAD_LINK = "$JRE_DOWNLOAD_LINK.sha256.txt"
    val JRE_INSTALLER_KEYS = listOf("/quiet", "/norestart", "INSTALLDIR=\"$JRE_RUNTIME_PATH\"")
    const val USED_MINECRAFT_VERSION_CONFIG = "files/1.20.1-0.16.5.json"
}

@Serializable
data class Arguments(
    val game: List<String>,  // List of game arguments (can be strings and other objects)
    val jvm: List<String>
)

@Serializable
data class Rule(
    val action: String,
    val os: Os? = null,
    val features: Features? = null
)

@Serializable
data class Os(
    val name: String? = null,
    val arch: String? = null
)

@Serializable
data class Features(
    val is_demo_user: Boolean? = null,
    val is_quick_play_realms: Boolean? = null,
    val has_custom_resolution: Boolean? = null,
    val has_quick_plays_support: Boolean? = null,
    val is_quick_play_singleplayer: Boolean? = null,
    val is_quick_play_multiplayer: Boolean? = null
)

@Serializable
data class AssetIndex(
    val id: String,
    val sha1: String,
    val size: Int,
    val totalSize: Long,
    val url: String
)

@Serializable
data class Downloads(
    val client_mappings: DownloadInfo,
    val server: DownloadInfo,
    val client: DownloadInfo,
    val server_mappings: DownloadInfo
)

@Serializable
data class DownloadInfo(
    val sha1: String,
    val size: Long,
    val url: String
)

@Serializable
data class JavaVersion(
    val component: String,
    val majorVersion: Int
)

@Serializable
data class Library(
    val downloads: LibraryDownloads,
    val include_in_classpath: Boolean
)

@Serializable
data class LibraryDownloads(
    val artifact: Artifact
)

@Serializable
data class Artifact(
    val path: String,
    val sha1: String? = null,
    val size: Long? = null,
    val url: String
)

@Serializable
data class VersionManifest(
    val arguments: Arguments,
    val assetIndex: AssetIndex,
    val assets: String,
    val downloads: Downloads,
    val id: String,
    val javaVersion: JavaVersion,
    val libraries: List<Library>,
    val mainClass: String,
    val type: String
)
