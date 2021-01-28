package com.mxalbert.sharedelements.demo

import android.os.Bundle
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.savedinstancestate.savedInstanceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.setContent
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.mxalbert.sharedelements.*

@OptIn(ExperimentalFoundationApi::class, ExperimentalAnimationApi::class)
class MainActivity : AppCompatActivity() {

    private data class User(@DrawableRes val avatar: Int, val name: String)

    companion object {
        private const val ListScreen = "list"
        private const val DetailsScreen = "details"

        private const val TransitionDurationMillis = 1000
        private val ZeroOffset: (IntSize) -> IntOffset = { IntOffset.Zero }

        private val FadeOutTransitionSpec = MaterialContainerTransformSpec(
            durationMillis = TransitionDurationMillis,
            fadeMode = FadeMode.In
        )
        private val CrossFadeTransitionSpec = SharedElementsTransitionSpec(
            durationMillis = TransitionDurationMillis,
            fadeMode = FadeMode.Cross
        )
        private val MaterialFadeInTransitionSpec = MaterialContainerTransformSpec(
            pathMotionFactory = MaterialArcMotionFactory,
            durationMillis = TransitionDurationMillis,
            fadeMode = FadeMode.In
        )
        private val MaterialFadeOutTransitionSpec = MaterialContainerTransformSpec(
            pathMotionFactory = MaterialArcMotionFactory,
            durationMillis = TransitionDurationMillis,
            fadeMode = FadeMode.Out
        )

        private fun hold(): ExitTransition = slideOut(
            ZeroOffset,
            tween(durationMillis = TransitionDurationMillis)
        )
    }

    private val users = listOf(
        User(R.drawable.avatar_1, "Adam"),
        User(R.drawable.avatar_2, "Andrew"),
        User(R.drawable.avatar_3, "Anna"),
        User(R.drawable.avatar_4, "Boris"),
        User(R.drawable.avatar_5, "Carl"),
        User(R.drawable.avatar_6, "Donna"),
        User(R.drawable.avatar_7, "Emily"),
        User(R.drawable.avatar_8, "Fiona"),
        User(R.drawable.avatar_9, "Grace"),
        User(R.drawable.avatar_10, "Irene"),
        User(R.drawable.avatar_11, "Jack"),
        User(R.drawable.avatar_12, "Jake"),
        User(R.drawable.avatar_13, "Mary"),
        User(R.drawable.avatar_14, "Peter"),
        User(R.drawable.avatar_15, "Rose"),
        User(R.drawable.avatar_16, "Victor")
    )

    private var selectedUser: User? by mutableStateOf(null)
    private lateinit var scope: SharedElementsRootScope

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colors = if (isSystemInDarkTheme()) darkColors() else lightColors()
            ) {
                var useCards by savedInstanceState { true }
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
        }
    }

    override fun onBackPressed() {
        if (selectedUser != null) {
            changeUser(null)
        } else {
            super.onBackPressed()
        }
    }

    private fun changeUser(user: User?) {
        val currentUser = selectedUser
        if (currentUser != user) {
            if (currentUser != null) {
                scope.prepareTransition(currentUser.avatar, currentUser.name)
            }
            selectedUser = user
        }
    }

    @Composable
    private fun UserCardsRoot() {
        SharedElementsRoot {
            scope = this
            val user = selectedUser
            val listState = rememberLazyListState()
            AnimatedVisibility(
                visible = user == null,
                enter = slideIn(ZeroOffset),  // No animation visually
                exit = hold()
            ) {
                UserCardsScreen(listState)
            }
            AnimatedVisibility(
                visible = user != null,
                enter = fadeIn(animSpec = tween(durationMillis = TransitionDurationMillis)),
                exit = fadeOut(animSpec = tween(durationMillis = TransitionDurationMillis))
            ) {
                val currentUser = remember { user!! }
                UserCardDetailsScreen(currentUser)
            }
        }
    }

    @Composable
    private fun UserCardsScreen(listState: LazyListState) {
        LazyVerticalGrid(
            cells = GridCells.Fixed(2),
            state = listState,
            contentPadding = PaddingValues(4.dp)
        ) {
            items(users) { user ->
                Box(modifier = Modifier.padding(4.dp)) {
                    SharedMaterialContainer(
                        key = user.name,
                        screenKey = ListScreen,
                        shape = MaterialTheme.shapes.medium,
                        elevation = 2.dp,
                        transitionSpec = MaterialFadeInTransitionSpec
                    ) {
                        Column(
                            modifier = Modifier.clickable(enabled = !scope.isRunningTransition) {
                                changeUser(user)
                            }
                        ) {
                            Image(
                                vectorResource(id = user.avatar),
                                modifier = Modifier.fillMaxWidth(),
                                contentScale = ContentScale.Crop
                            )
                            Text(
                                text = user.name,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun UserCardDetailsScreen(user: User) {
        // Scrim color
        Surface(color = Color.Black.copy(alpha = 0.32f)) {
            SharedMaterialContainer(
                key = user.name,
                screenKey = DetailsScreen,
                transitionSpec = MaterialFadeOutTransitionSpec
            ) {
                Surface {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            vectorResource(id = user.avatar),
                            modifier = Modifier.fillMaxWidth()
                                .clickable(enabled = !scope.isRunningTransition) { changeUser(null) },
                            contentScale = ContentScale.Crop
                        )
                        Text(
                            text = user.name,
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.h1
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun UserListRoot() {
        SharedElementsRoot {
            scope = this
            val listState = rememberLazyListState()
            Crossfade(
                current = selectedUser,
                animation = tween(durationMillis = TransitionDurationMillis)
            ) { user ->
                when (user) {
                    null -> UserListScreen(listState)
                    else -> UserDetailsScreen(user)
                }
            }
        }
    }

    @Composable
    private fun UserListScreen(listState: LazyListState) {
        LazyColumn(state = listState) {
            items(users) { user ->
                ListItem(
                    Modifier.clickable(enabled = !scope.isRunningTransition) { changeUser(user) },
                    icon = {
                        SharedMaterialContainer(
                            key = user.avatar,
                            screenKey = ListScreen,
                            shape = CircleShape,
                            color = Color.Transparent,
                            transitionSpec = FadeOutTransitionSpec
                        ) {
                            Image(
                                vectorResource(id = user.avatar),
                                modifier = Modifier.preferredSize(48.dp),
                                contentScale = ContentScale.Crop
                            )
                        }
                    },
                    text = {
                        SharedElement(
                            key = user.name,
                            screenKey = ListScreen,
                            transitionSpec = CrossFadeTransitionSpec
                        ) {
                            Text(text = user.name)
                        }
                    }
                )
            }
        }
    }

    @Composable
    private fun UserDetailsScreen(user: User) {
        Column(
            Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SharedMaterialContainer(
                key = user.avatar,
                screenKey = DetailsScreen,
                shape = MaterialTheme.shapes.medium,
                color = Color.Transparent,
                elevation = 10.dp,
                transitionSpec = FadeOutTransitionSpec
            ) {
                Image(
                    vectorResource(id = user.avatar),
                    modifier = Modifier.preferredSize(200.dp)
                        .clickable(enabled = !scope.isRunningTransition) { changeUser(null) },
                    contentScale = ContentScale.Crop
                )
            }
            SharedElement(
                key = user.name,
                screenKey = DetailsScreen,
                transitionSpec = CrossFadeTransitionSpec
            ) {
                Text(text = user.name, style = MaterialTheme.typography.h1)
            }
        }
    }

}
