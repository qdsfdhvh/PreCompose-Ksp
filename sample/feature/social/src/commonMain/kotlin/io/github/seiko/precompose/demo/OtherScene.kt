package io.github.seiko.precompose.demo

import androidx.compose.runtime.Composable
import io.github.seiko.precompose.annotation.NavGraphDestination
import io.github.seiko.precompose.annotation.Path
import io.github.seiko.precompose.annotation.Query
import moe.tlaster.precompose.navigation.Navigator

@NavGraphDestination(
    route = "/other/one",
)
@Composable
fun OtherOneScene(
    navigator: Navigator,
    @Path id: Int,
    @Query name: String?,
) {
}
