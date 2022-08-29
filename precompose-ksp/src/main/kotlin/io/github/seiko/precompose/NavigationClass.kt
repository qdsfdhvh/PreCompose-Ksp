package io.github.seiko.precompose

import com.squareup.kotlinpoet.ClassName

internal val navControllerType = ClassName("moe.tlaster.precompose.navigation", "Navigator")
internal val navBackStackEntryType = ClassName("moe.tlaster.precompose.navigation", "BackStackEntry")
internal val routeBuilderType = ClassName("moe.tlaster.precompose.navigation", "RouteBuilder")

internal val pathType = ClassName("moe.tlaster.precompose.navigation", "path")
internal val queryType = ClassName("moe.tlaster.precompose.navigation", "query")
