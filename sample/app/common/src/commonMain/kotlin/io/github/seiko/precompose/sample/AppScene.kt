package io.github.seiko.precompose.sample

import androidx.compose.runtime.Composable
import io.github.seiko.precompose.annotation.NavGraphDestination
import moe.tlaster.precompose.navigation.Navigator

@NavGraphDestination(
    route = "app/scene",
)
@Composable
fun AppScene(
    navigator: Navigator,
) {
}
