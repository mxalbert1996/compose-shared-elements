package com.mxalbert.sharedelements.demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.zIndex

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent(null) {
            MaterialTheme(colors = if (isSystemInDarkTheme()) darkColors() else lightColors()) {
                Surface(color = MaterialTheme.colors.background) {
                    Demo()
                }
            }
        }
    }

}

@Composable
private fun Demo() {
    var useCards by rememberSaveable { mutableStateOf(true) }
    Column {
        TopAppBar(
            title = { Text(text = stringResource(R.string.app_name)) },
            actions = {
                IconButton(onClick = { useCards = !useCards }) {
                    Text(text = "SWITCH", textAlign = TextAlign.Center)
                }
            },
            modifier = Modifier.zIndex(1f)
        )
        Crossfade(useCards) {
            if (it) {
                UserCardsRoot()
            } else {
                UserListRoot()
            }
        }
    }
}
