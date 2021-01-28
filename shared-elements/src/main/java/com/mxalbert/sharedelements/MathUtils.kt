package com.mxalbert.sharedelements

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.ScaleFactor
import androidx.compose.ui.layout.lerp

internal val Rect.area: Float
    get() = width * height

internal operator fun Size.div(operand: Size): ScaleFactor =
    ScaleFactor(width / operand.width, height / operand.height)

internal fun calculateDirection(start: Rect, end: Rect): TransitionDirection =
    if (end.area > start.area) TransitionDirection.Enter else TransitionDirection.Return

internal fun calculateAlpha(
    direction: TransitionDirection?,
    fadeMode: FadeMode?,
    isStart: Boolean,
    fraction: Float
) = when (fadeMode) {
    FadeMode.In -> if (isStart) 1f else 1 - fraction
    FadeMode.Out -> if (isStart) 1 - fraction else 1f
    FadeMode.Cross, null -> 1 - fraction
    FadeMode.Through -> {
        val actualFraction = if (isStart) fraction else 1 - fraction
        val threshold = if (direction == TransitionDirection.Enter)
            FadeThroughProgressThreshold else 1 - FadeThroughProgressThreshold
        if (actualFraction < threshold) {
            if (isStart) 1 - actualFraction / threshold else 0f
        } else {
            if (isStart) 0f else 1 - fraction / (1 - threshold)
        }
    }
}

internal fun calculateOffset(
    start: Rect,
    end: Rect?,
    fraction: Float,
    pathMotion: PathMotion?,
    width: Float
): Offset = if (end == null) start.topLeft else {
    val topCenter = pathMotion!!.invoke(
        start.topCenter,
        end.topCenter,
        fraction
    )
    Offset(topCenter.x - width / 2, topCenter.y)
}

private val Identity = ScaleFactor(1f, 1f)

internal fun calculateScale(
    start: Rect,
    end: Rect?,
    fraction: Float
): ScaleFactor =
    if (end == null) Identity else lerp(Identity, end.size / start.size, fraction)
