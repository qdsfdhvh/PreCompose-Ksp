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
    route = WalletRoute.Collectible,
    deepLinks = DeepLinks.WalletCollectible,
)
@Composable
fun WalletCollectibleScene(
    @Back onBack: () -> Unit,
    @Navigate onNavigate: (String) -> Unit,
) {
    Scaffold(
        topBar = {
            TopBackBar(
                onBack = onBack,
                title = {
                    Text("Wallet")
                },
            )
        },
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Button(onClick = { onNavigate(WalletRoute.Connect) }) {
                Text("Connect")
            }
            Button(onClick = { onNavigate(DeepLinks.SocialProfile) }) {
                Text("Social")
            }
        }
    }
}

@NavGraphDestination(
    route = WalletRoute.Connect,
    packageName = "io.github.seiko.precompose.demo",
    functionName = "customScene",
)
@Composable
fun WalletConnectScene(
    @Back onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopBackBar(
                onBack = onBack,
                title = {
                    Text("Wallet")
                },
            )
        },
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text("This is wallet connect.")
        }
    }
}
