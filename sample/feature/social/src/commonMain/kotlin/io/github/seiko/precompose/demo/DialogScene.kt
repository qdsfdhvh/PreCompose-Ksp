package io.github.seiko.precompose.demo

import androidx.compose.runtime.Composable
import io.github.seiko.precompose.annotation.NavGraphDestination

@NavGraphDestination(
    route = "Dialog",
    functionName = "dialog",
)
@Composable
fun DialogScene() {
}
