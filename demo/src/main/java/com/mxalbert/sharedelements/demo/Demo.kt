package com.mxalbert.sharedelements.demo

import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.mxalbert.sharedelements.*

private var selectedUser: Int by mutableStateOf(-1)
private var previousSelectedUser: Int = -1

@Composable
fun UserCardsRoot() {
    SharedElementsRoot {
        val user = selectedUser
        val listState = rememberLazyListState()

        BackHandler(enabled = user >= 0) {
            changeUser(-1)
        }

        DelayExit(visible = user < 0) {
            UserCardsScreen(listState)
        }

        DelayExit(visible = user >= 0) {
            val currentUser = remember { users[user] }
            UserCardDetailsScreen(currentUser)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun UserCardsScreen(listState: LazyListState) {
    LaunchedEffect(listState) {
        val previousIndex = (previousSelectedUser / 2).coerceAtLeast(0)
        if (!listState.layoutInfo.visibleItemsInfo.any { it.index == previousIndex }) {
            listState.scrollToItem(previousIndex)
        }
    }

    val scope = LocalSharedElementsRootScope.current!!
    LazyVerticalGrid(
        cells = GridCells.Fixed(2),
        state = listState,
        contentPadding = PaddingValues(4.dp)
    ) {
        itemsIndexed(users) { i, user ->
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
                            scope.changeUser(i)
                        }
                    ) {
                        Image(
                            painterResource(id = user.avatar),
                            contentDescription = user.name,
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
    val (fraction, setFraction) = remember { mutableStateOf(1f) }
    // Scrim color
    Surface(color = Color.Black.copy(alpha = 0.32f * (1 - fraction))) {
        SharedMaterialContainer(
            key = user.name,
            screenKey = DetailsScreen,
            isFullscreen = true,
            transitionSpec = MaterialFadeOutTransitionSpec,
            onFractionChanged = setFraction
        ) {
            Surface {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val scope = LocalSharedElementsRootScope.current!!
                    Image(
                        painterResource(id = user.avatar),
                        contentDescription = user.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !scope.isRunningTransition) {
                                scope.changeUser(-1)
                            },
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
fun UserListRoot() {
    SharedElementsRoot {
        BackHandler(enabled = selectedUser >= 0) {
            changeUser(-1)
        }

        val listState = rememberLazyListState()
        Crossfade(
            targetState = selectedUser,
            animationSpec = tween(durationMillis = TransitionDurationMillis)
        ) { user ->
            when {
                user < 0 -> UserListScreen(listState)
                else -> UserDetailsScreen(users[user])
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun UserListScreen(listState: LazyListState) {
    LaunchedEffect(listState) {
        val previousIndex = previousSelectedUser.coerceAtLeast(0)
        if (!listState.layoutInfo.visibleItemsInfo.any { it.index == previousIndex }) {
            listState.scrollToItem(previousIndex)
        }
    }

    val scope = LocalSharedElementsRootScope.current!!
    LazyColumn(state = listState) {
        itemsIndexed(users) { i, user ->
            ListItem(
                Modifier.clickable(enabled = !scope.isRunningTransition) {
                    scope.changeUser(i)
                },
                icon = {
                    SharedMaterialContainer(
                        key = user.avatar,
                        screenKey = ListScreen,
                        shape = CircleShape,
                        color = Color.Transparent,
                        transitionSpec = FadeOutTransitionSpec
                    ) {
                        Image(
                            painterResource(id = user.avatar),
                            contentDescription = user.name,
                            modifier = Modifier.size(48.dp),
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
            val scope = LocalSharedElementsRootScope.current!!
            Image(
                painterResource(id = user.avatar),
                contentDescription = user.name,
                modifier = Modifier
                    .size(200.dp)
                    .clickable(enabled = !scope.isRunningTransition) { scope.changeUser(-1) },
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

private fun SharedElementsRootScope.changeUser(user: Int) {
    val currentUser = selectedUser
    if (currentUser != user) {
        val targetUser = if (user >= 0) user else currentUser
        if (targetUser >= 0) {
            users[targetUser].let {
                prepareTransition(it.avatar, it.name)
            }
        }
        previousSelectedUser = selectedUser
        selectedUser = user
    }
}

private data class User(@DrawableRes val avatar: Int, val name: String)

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

private const val ListScreen = "list"
private const val DetailsScreen = "details"

private const val TransitionDurationMillis = 1000

private val FadeOutTransitionSpec = MaterialContainerTransformSpec(
    durationMillis = TransitionDurationMillis,
    fadeMode = FadeMode.Out
)
private val CrossFadeTransitionSpec = SharedElementsTransitionSpec(
    durationMillis = TransitionDurationMillis,
    fadeMode = FadeMode.Cross,
    fadeProgressThresholds = ProgressThresholds(0.10f, 0.40f)
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
