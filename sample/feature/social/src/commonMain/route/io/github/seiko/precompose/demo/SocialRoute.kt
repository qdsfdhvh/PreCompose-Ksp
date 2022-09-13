package io.github.seiko.precompose.demo

import io.github.seiko.precompose.annotation.GeneratedRoute
import moe.tlaster.precompose.navigation.Navigator
import moe.tlaster.precompose.navigation.RouteBuilder

@Suppress("NO_ACTUAL_FOR_EXPECT")
@GeneratedRoute
expect fun RouteBuilder.socialRoute(navigator: Navigator, onNavigate: (String) -> Unit)

object SocialRoute {
    const val Profile = "/social/profile"
    const val Relation = "/social/relation"
}
