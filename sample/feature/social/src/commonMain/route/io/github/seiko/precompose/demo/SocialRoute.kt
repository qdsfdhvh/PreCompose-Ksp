package io.github.seiko.precompose.demo

import io.github.seiko.precompose.annotation.RouteGraph
import moe.tlaster.precompose.navigation.Navigator
import moe.tlaster.precompose.navigation.RouteBuilder

@Suppress("NO_ACTUAL_FOR_EXPECT")
@RouteGraph
expect fun RouteBuilder.socialRoute(navigator: Navigator, onNavigate: (String) -> Unit)

object SocialRoute {
    const val Profile = "/social/profile"
    const val Relation = "/social/relation"
}
