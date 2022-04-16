package com.mxalbert.sharedelements.demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent(null) {
            MaterialTheme(
                colors = if (isSystemInDarkTheme()) darkColors() else lightColors()
            ) {
                Demo()
            }
        }
    }

}

@Composable
private fun Demo() {
    var useCards by rememberSaveable { mutableStateOf(true) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.app_name)) },
                actions = {
                    IconButton(onClick = { useCards = !useCards }) {
                        Text(text = "SWITCH", textAlign = TextAlign.Center)
                    }
                }
            )
        }
    ) {
        Crossfade(useCards) {
            if (it) {
                UserCardsRoot()
            } else {
                UserListRoot()
            }
        }
    }
}
