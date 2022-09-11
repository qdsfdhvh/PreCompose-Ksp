package io.github.seiko.precompose.sample

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.application
import moe.tlaster.precompose.PreComposeWindow

fun main(args: Array<String>) {
    application {
        PreComposeWindow(
            title = "Route Demo",
            onCloseRequest = ::exitApplication,
        ) {
            Route(Modifier.fillMaxSize())
        }
    }
}