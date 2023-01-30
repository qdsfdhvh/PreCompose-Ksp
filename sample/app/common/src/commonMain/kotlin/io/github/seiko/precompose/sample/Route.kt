package io.github.seiko.precompose.sample

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.seiko.precompose.annotation.RootRouteGraph
import io.github.seiko.precompose.demo.SocialRoute
import io.github.seiko.precompose.demo.socialRoute
import io.github.seiko.precompose.demo.walletRoute
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
        )
    }
}

private fun RouteBuilder.generateRoute(navigator: Navigator, onBack: () -> Unit, onNavigate: (String) -> Unit) {
    socialRoute(navigator, onNavigate = onNavigate)
    walletRoute(navigator, onBack = onBack, onNavigate = onNavigate)
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
@RootRouteGraph
expect fun RouteBuilder.generateRoute2(navigator: Navigator, onBack: () -> Unit, onNavigate: (String) -> Unit)