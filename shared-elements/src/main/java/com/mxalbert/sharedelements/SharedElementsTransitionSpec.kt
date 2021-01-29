package com.mxalbert.sharedelements

import androidx.compose.animation.core.AnimationConstants
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing

open class SharedElementsTransitionSpec(
    val pathMotionFactory: PathMotionFactory = LinearMotionFactory,
    val durationMillis: Int = AnimationConstants.DefaultDurationMillis,
    val delayMillis: Int = 0,
    val easing: Easing = FastOutSlowInEasing,
    val direction: TransitionDirection = TransitionDirection.Auto,
    val fadeMode: FadeMode = FadeMode.Cross,
    val fadeProgressThresholds: ProgressThresholds? = null,
    val scaleProgressThresholds: ProgressThresholds? = null
)

val DefaultSharedElementsTransitionSpec = SharedElementsTransitionSpec()
