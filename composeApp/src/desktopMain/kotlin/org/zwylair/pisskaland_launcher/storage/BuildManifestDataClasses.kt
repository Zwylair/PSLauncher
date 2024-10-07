package org.zwylair.pisskaland_launcher.storage

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Hashes(
    val sha1: String,
    val sha512: String
)

@Serializable
data class Environment(
    val client: String,
    val server: String
)

@Serializable
data class File(
    val path: String,
    val hashes: Hashes,
    val env: Environment,
    val downloads: List<String>,
    val fileSize: Int
)

@Serializable
data class Dependencies(
    @SerialName("fabric-loader")
    val fabricLoader: String,
    val minecraft: String
)

@Serializable
data class BuildManifest(
    val game: String,
    val formatVersion: Int,
    val versionId: String,
    val name: String,
    val summary: String,
    val files: List<File>,
    val dependencies: Dependencies
)
