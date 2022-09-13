package io.github.seiko.precompose.demo

import io.github.seiko.precompose.annotation.Back
import io.github.seiko.precompose.annotation.GeneratedRoute
import io.github.seiko.precompose.annotation.Navigate
import moe.tlaster.precompose.navigation.Navigator
import moe.tlaster.precompose.navigation.RouteBuilder

@Suppress("NO_ACTUAL_FOR_EXPECT")
@GeneratedRoute
expect fun RouteBuilder.walletRoute(
    navigator: Navigator,
    @Back onBack: () -> Unit,
    @Navigate onNavigate: (String) -> Unit,
)

object WalletRoute {
    const val Collectible = "/wallet/collectible"
    const val Connect = "/wallet/connect"
}
