package org.zwylair.pisskaland_launcher

object Config {
    const val JRE_DOWNLOAD_LINK = "https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.12%2B7/OpenJDK17U-jre_x86-32_windows_hotspot_17.0.12_7.msi"
    const val JRE_RUNTIME_PATH = "jre_runtime/"
    const val JRE_SHA256_DOWNLOAD_LINK = "$JRE_DOWNLOAD_LINK.sha256.txt"
    val JRE_INSTALLER_KEYS = listOf("/quiet", "/norestart", "INSTALLDIR=\"$JRE_RUNTIME_PATH\"")
}
