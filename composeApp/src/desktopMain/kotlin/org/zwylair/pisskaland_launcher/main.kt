package org.zwylair.pisskaland_launcher

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "PisskaLandLauncher",
    ) {
        app()
    }
}
