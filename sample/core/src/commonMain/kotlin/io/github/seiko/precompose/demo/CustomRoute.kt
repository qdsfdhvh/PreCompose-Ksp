package io.github.seiko.precompose.demo

import androidx.compose.runtime.Composable
import moe.tlaster.precompose.navigation.BackStackEntry
import moe.tlaster.precompose.navigation.RouteBuilder
import moe.tlaster.precompose.navigation.transition.NavTransition

fun RouteBuilder.customScene(
    route: String,
    deepLinks: List<String> = emptyList(),
    content: @Composable (BackStackEntry) -> Unit,
) {
    scene(
        route = route,
        deepLinks = deepLinks,
        navTransition = NavTransition(),
        content = content,
    )
}