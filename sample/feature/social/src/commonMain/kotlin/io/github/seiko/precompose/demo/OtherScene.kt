package io.github.seiko.precompose.demo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.seiko.precompose.annotation.NavGraphContainer
import io.github.seiko.precompose.annotation.NavGraphDestination
import io.github.seiko.precompose.annotation.Path
import io.github.seiko.precompose.annotation.Query
import moe.tlaster.precompose.navigation.Navigator

@NavGraphDestination(
    route = SocialRoute.OtherOne,
)
@Composable
fun OtherOneScene(
    navigator: Navigator,
    @Path id: Int,
    @Query name: String?,
) {
    Scaffold(
        topBar = {
            TopBackBar(
                onBack = { navigator.goBack() },
                title = {
                    Text("Social_${id}_$name")
                },
            )
        },
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text("Other")
        }
    }
}
