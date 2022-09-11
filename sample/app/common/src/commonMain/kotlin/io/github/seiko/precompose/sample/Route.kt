package io.github.seiko.precompose.sample

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.seiko.precompose.demo.SocialRoute
import io.github.seiko.precompose.demo.socialRoute
import io.github.seiko.precompose.demo.walletRoute
import moe.tlaster.precompose.navigation.NavHost
import moe.tlaster.precompose.navigation.rememberNavigator

@Composable
fun Route(modifier: Modifier = Modifier) {
    val navigator = rememberNavigator()
    NavHost(
        navigator = navigator,
        initialRoute = SocialRoute.Profile,
        modifier = modifier,
    ) {
        socialRoute(navigator)
        walletRoute(navigator)
    }
}