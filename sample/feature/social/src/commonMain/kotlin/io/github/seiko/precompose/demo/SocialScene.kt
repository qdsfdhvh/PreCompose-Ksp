package io.github.seiko.precompose.demo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.seiko.precompose.annotation.Back
import io.github.seiko.precompose.annotation.NavGraphDestination
import io.github.seiko.precompose.annotation.Navigate

@NavGraphDestination(
    route = SocialRoute.Profile,
    deepLinks = [DeepLinks.SOCIAL_PROFILE],
)
@Composable
fun SocialProfileScene(
    @Back onBack: () -> Unit,
    @Navigate onNavigate: (String) -> Unit,
) {
    Scaffold(
        topBar = {
            TopBackBar(
                onBack = onBack,
                title = {
                    Text("Social")
                },
            )
        },
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Button(onClick = { onNavigate(SocialRoute.Relation) }) {
                Text("Relation")
            }
            Button(onClick = { onNavigate(DeepLinks.WALLET_COLLECTIBLE) }) {
                Text("Wallet")
            }
        }
    }
}

@NavGraphDestination(
    route = SocialRoute.Relation,
)
@Composable
fun SocialRelationScene(
    @Back onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopBackBar(
                onBack = onBack,
                title = {
                    Text("Social")
                },
            )
        },
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text("This is social relation.")
        }
    }
}
