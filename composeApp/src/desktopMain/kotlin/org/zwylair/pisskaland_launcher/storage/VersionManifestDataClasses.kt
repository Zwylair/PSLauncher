package org.zwylair.pisskaland_launcher.storage

import kotlinx.serialization.Serializable

@Serializable
data class Arguments(
    val game: List<String>,  // List of game arguments (can be strings and other objects)
    val jvm: List<String>
)

@Serializable
data class Os(
    val name: String? = null,
    val arch: String? = null
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

