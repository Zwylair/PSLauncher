package org.zwylair.pisskaland_launcher.storage

import java.io.File

object Config {
    const val LATEST_LAUNCHER_VERSION_FILE_URL = "https://github.com/Zwylair/PSLauncher/releases/latest/download/version.txt"
    const val LAUNCHER_DOWNLOAD_PAGE_URL = "https://github.com/Zwylair/PSLauncher/releases/latest/"

    const val PYTHON_INSTALLER_URL = "https://github.com/winpython/winpython/releases/download/10.1.20240824/Winpython64-3.12.5.0dotb5.exe"
    const val PYTHON_INSTALLER_PATH = "Winpython64-3.12.5.0dotb5.exe"
    const val PYTHON_SDK_PATH = "WPy64-31250b5/python-3.12.5.amd64"

    const val MINECRAFT_BUILD_CONFIG = "files/build.json"
    val MINECRAFT_PARENT_PATH = File("minecraft").absolutePath
}
