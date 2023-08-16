package io.github.seiko.precompose.sample

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.seiko.precompose.annotation.Back
import io.github.seiko.precompose.annotation.Ignore
import io.github.seiko.precompose.annotation.NavGraphContainer
import io.github.seiko.precompose.annotation.Navigate
import io.github.seiko.precompose.demo.SocialRoute
import moe.tlaster.precompose.navigation.NavHost
import moe.tlaster.precompose.navigation.Navigator
import moe.tlaster.precompose.navigation.RouteBuilder
import moe.tlaster.precompose.navigation.rememberNavigator

@Composable
fun Route(modifier: Modifier = Modifier) {
    val navigator = rememberNavigator()
    NavHost(
        navigator = navigator,
        initialRoute = SocialRoute.Profile,
        modifier = modifier,
    ) {
        generateRoute(
            navigator = navigator,
            onBack = { navigator.goBack() },
            onNavigate = { uri -> navigator.navigate(uri) },
            onWebNavigate = {},
        )
    }
}

@NavGraphContainer
internal fun RouteBuilder.generateRoute(
    navigator: Navigator,
    @Back onBack: () -> Unit,
    @Navigate onNavigate: (String) -> Unit,
    onWebNavigate: (String) -> Unit,
    @Ignore doNotUse: DoNotUse = DoNotUse,
) {
    throw RuntimeException("Route generation failed. Check your ksp settings.")
}

internal object DoNotUse
