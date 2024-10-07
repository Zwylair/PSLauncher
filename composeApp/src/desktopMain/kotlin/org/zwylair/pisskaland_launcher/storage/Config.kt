package org.zwylair.pisskaland_launcher.storage

import java.io.File

object Config {
    const val JRE_INSTALLER_DOWNLOAD_LINK = "https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.12%2B7/OpenJDK17U-jre_x86-32_windows_hotspot_17.0.12_7.msi"
    const val JRE_INSTALLER_OUTPUT_FILENAME = "OpenJDK-jre-x86-win-v17.0.12-7.msi"
    val JRE_RUNTIME_PATH = File("runtime").absolutePath
    val JRE_JAVAW_PATH = "$JRE_RUNTIME_PATH/bin/javaw.exe"
    const val JRE_SHA256_DOWNLOAD_LINK = "$JRE_INSTALLER_DOWNLOAD_LINK.sha256.txt"
    val JRE_INSTALLER_KEYS = listOf("/quiet", "/norestart", "INSTALLDIR=\"$JRE_RUNTIME_PATH\"")
    const val USED_MINECRAFT_VERSION_CONFIG = "files/1.20.1-0.16.5.json"
    const val MINECRAFT_BUILD_CONFIG = "files/build.json"
}
