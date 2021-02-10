package com.mxalbert.sharedelements

import androidx.compose.animation.core.AnimationConstants
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.AmbientDensity
import androidx.compose.ui.unit.*
import androidx.compose.ui.util.lerp
import androidx.compose.ui.zIndex
import kotlin.math.roundToInt

@Composable
fun SharedMaterialContainer(
    key: Any,
    screenKey: Any,
    shape: Shape = RectangleShape,
    color: Color = MaterialTheme.colors.surface,
    contentColor: Color = contentColorFor(color),
    border: BorderStroke? = null,
    elevation: Dp = 0.dp,
    transitionSpec: MaterialContainerTransformSpec = DefaultMaterialContainerTransformSpec,
    onFractionChanged: ((Float) -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val elementInfo = MaterialContainerInfo(
        key, screenKey, shape, color, contentColor,
        border, elevation, transitionSpec, onFractionChanged
    )
    val realPlaceholder = placeholder ?: content
    BaseSharedElement(
        elementInfo,
        realPlaceholder,
        { Placeholder(it) },
        {
            Surface(
                modifier = it,
                shape = shape,
                color = color,
                contentColor = contentColor,
                border = border,
                elevation = elevation,
                content = content
            )
        }
    )
}

@Composable
private fun Placeholder(state: SharedElementsTransitionState) {
    with(AmbientDensity.current) {
        val startInfo = state.startInfo as MaterialContainerInfo
        val direction = state.direction
        val spec = state.spec as? MaterialContainerTransformSpec
        val start = state.startBounds
        val end = state.endBounds
        val fraction = state.fraction

        val fitMode = if (spec == null) null else remember {
            val mode = spec.fitMode
            if (mode != FitMode.Auto) mode else
                calculateFitMode(direction == TransitionDirection.Enter, start, end!!)
        }

        val thresholds = if (spec == null) DefaultEnterThresholds else remember {
            spec.progressThresholdsGroupFor(direction!!, state.pathMotion!!)
        }

        val scaleFraction = thresholds.scale.applyTo(fraction)
        val scale = calculateScale(start, end, scaleFraction)
        val contentScale = if (fitMode == FitMode.Height) scale.scaleY else scale.scaleX
        val scaleMaskFraction = thresholds.scaleMask.applyTo(fraction)
        val (containerWidth, containerHeight) = if (end == null) start.size * contentScale else {
            if (fitMode == FitMode.Height) Size(
                width = lerp(
                    start.width * contentScale,
                    start.height * contentScale / end.height * end.width,
                    scaleMaskFraction
                ),
                height = start.height * contentScale
            ) else Size(
                width = start.width * contentScale,
                height = lerp(
                    start.height * contentScale,
                    start.width * contentScale / end.width * end.height,
                    scaleMaskFraction
                )
            )
        }

        val offset = calculateOffset(start, end, fraction, state.pathMotion, containerWidth).round()

        val surfaceModifier = Modifier.size(
            containerWidth.toDp(),
            containerHeight.toDp()
        ).offset { offset }

        var shape = startInfo.shape
        var color = startInfo.color
        var contentColor = startInfo.contentColor
        var border = startInfo.border
        var elevation = startInfo.elevation

        val endInfo = state.endInfo as? MaterialContainerInfo
        var drawEnd: (@Composable () -> Unit)? = null
        val fadeFraction = thresholds.fade.applyTo(fraction)
        if (end != null && endInfo != null) {
            val endAlpha = calculateAlpha(direction, state.spec?.fadeMode, fadeFraction, false)
            if (endAlpha > 0) drawEnd = {
                val endScale = calculateScale(end, start, 1 - scaleFraction).run {
                    if (fitMode == FitMode.Height) scaleY else scaleX
                }
                val containerColor = spec?.endContainerColor ?: Color.Transparent
                val containerModifier = Modifier.fillMaxSize().run {
                    if (containerColor == Color.Transparent) this else
                        background(containerColor.copy(alpha = containerColor.alpha * endAlpha))
                }.run {
                    if (state.spec?.fadeMode == FadeMode.Out) zIndex(-1f) else this
                }
                val contentModifier = Modifier.size(
                    end.width.toDp(),
                    end.height.toDp()
                ).run {
                    if (fitMode == FitMode.Height) offset {
                        IntOffset(((containerWidth - end.width * endScale) / 2).roundToInt(), 0)
                    } else this
                }.graphicsLayer {
                    this.transformOrigin = TopLeft
                    this.scaleX = endScale
                    this.scaleY = endScale
                    this.alpha = endAlpha
                }

                ElementContainer(modifier = containerModifier, relaxMaxSize = true) {
                    ElementContainer(
                        modifier = contentModifier,
                        content = state.endPlaceholder!!
                    )
                }
            }

            val shapeFraction = thresholds.shapeMask.applyTo(fraction)
            shape = lerp(startInfo.shape, endInfo.shape, shapeFraction)
            color = lerp(startInfo.color, endInfo.color, shapeFraction)
            contentColor = lerp(startInfo.contentColor, endInfo.contentColor, shapeFraction)
            border = (startInfo.border ?: endInfo.border)?.copy(
                width = lerp(
                    startInfo.border?.width ?: 0.dp,
                    endInfo.border?.width ?: 0.dp,
                    shapeFraction
                )
            )
            elevation = lerp(startInfo.elevation, endInfo.elevation, shapeFraction)
        }

        val startAlpha = calculateAlpha(direction, state.spec?.fadeMode, fadeFraction, true)

        Surface(
            modifier = surfaceModifier,
            shape = shape,
            color = color,
            contentColor = contentColor,
            border = border,
            elevation = elevation
        ) {
            Box {
                if (startAlpha > 0) {
                    val containerColor = spec?.startContainerColor ?: Color.Transparent
                    val containerModifier = Modifier.fillMaxSize().run {
                        if (containerColor == Color.Transparent) this else
                            background(containerColor.copy(alpha = containerColor.alpha * startAlpha))
                    }
                    val contentModifier = Modifier.size(
                        start.width.toDp(),
                        start.height.toDp()
                    ).run {
                        if (fitMode == FitMode.Height) offset {
                            IntOffset(
                                ((containerWidth - start.width * contentScale) / 2).roundToInt(),
                                0
                            )
                        } else this
                    }.graphicsLayer {
                        this.transformOrigin = TopLeft
                        this.scaleX = contentScale
                        this.scaleY = contentScale
                        this.alpha = startAlpha
                    }

                    ElementContainer(modifier = containerModifier, relaxMaxSize = true) {
                        ElementContainer(
                            modifier = contentModifier,
                            content = state.startPlaceholder
                        )
                    }
                }

                drawEnd?.invoke()
            }
        }
    }
}

private fun calculateFitMode(entering: Boolean, start: Rect, end: Rect): FitMode {
    val startWidth = start.width
    val startHeight = start.height
    val endWidth = end.width
    val endHeight = end.height

    val endHeightFitToWidth = endHeight * startWidth / endWidth
    val startHeightFitToWidth = startHeight * endWidth / startWidth
    val fitWidth = if (entering)
        endHeightFitToWidth >= startHeight else startHeightFitToWidth >= endHeight
    return if (fitWidth) FitMode.Width else FitMode.Height
}

private fun lerp(start: Shape, end: Shape, fraction: Float): Shape {
    if ((start == RectangleShape && end == RectangleShape) ||
        (start != RectangleShape && start !is CornerBasedShape) ||
        (end != RectangleShape && end !is CornerBasedShape)
    ) return start
    val topLeft = lerp(
        (start as? CornerBasedShape)?.topLeft,
        (end as? CornerBasedShape)?.topLeft,
        fraction
    ) ?: ZeroCornerSize
    val topRight = lerp(
        (start as? CornerBasedShape)?.topRight,
        (end as? CornerBasedShape)?.topRight,
        fraction
    ) ?: ZeroCornerSize
    val bottomRight = lerp(
        (start as? CornerBasedShape)?.bottomRight,
        (end as? CornerBasedShape)?.bottomRight,
        fraction
    ) ?: ZeroCornerSize
    val bottomLeft = lerp(
        (start as? CornerBasedShape)?.bottomLeft,
        (end as? CornerBasedShape)?.bottomLeft,
        fraction
    ) ?: ZeroCornerSize
    return when {
        start is RoundedCornerShape || (start == RectangleShape && end is RoundedCornerShape) ->
            RoundedCornerShape(topLeft, topRight, bottomRight, bottomLeft)
        start is CutCornerShape || (start == RectangleShape && end is CutCornerShape) ->
            CutCornerShape(topLeft, topRight, bottomRight, bottomLeft)
        else -> start
    }
}

private fun lerp(start: CornerSize?, end: CornerSize?, fraction: Float): CornerSize? {
    if (start == null && end == null) return null
    return object : CornerSize {
        override fun toPx(shapeSize: Size, density: Density): Float =
            lerp(
                start?.toPx(shapeSize, density) ?: 0f,
                end?.toPx(shapeSize, density) ?: 0f,
                fraction
            )
    }
}

private class MaterialContainerInfo(
    key: Any,
    screenKey: Any,
    val shape: Shape,
    val color: Color,
    val contentColor: Color,
    val border: BorderStroke?,
    val elevation: Dp,
    spec: SharedElementsTransitionSpec,
    onFractionChanged: ((Float) -> Unit)?,
) : SharedElementInfo(key, screenKey, spec, onFractionChanged)

enum class FitMode {
    Auto, Width, Height
}

@Immutable
private class ProgressThresholdsGroup(
    val fade: ProgressThresholds,
    val scale: ProgressThresholds,
    val scaleMask: ProgressThresholds,
    val shapeMask: ProgressThresholds
)

// Default animation thresholds. Will be used by default when the default linear PathMotion is
// being used or when no other progress thresholds are appropriate (e.g., the arc thresholds for
// an arc path).
private val DefaultEnterThresholds = ProgressThresholdsGroup(
    fade = ProgressThresholds(0f, 0.25f),
    scale = ProgressThresholds(0f, 1f),
    scaleMask = ProgressThresholds(0f, 1f),
    shapeMask = ProgressThresholds(0f, 0.75f)
)
private val DefaultReturnThresholds = ProgressThresholdsGroup(
    fade = ProgressThresholds(0.60f, 0.90f),
    scale = ProgressThresholds(0f, 1f),
    scaleMask = ProgressThresholds(0f, 0.90f),
    shapeMask = ProgressThresholds(0.30f, 0.90f)
)

// Default animation thresholds for an arc path. Will be used by default when the PathMotion is
// set to MaterialArcMotion.
private val DefaultEnterThresholdsArc = ProgressThresholdsGroup(
    fade = ProgressThresholds(0.10f, 0.40f),
    scale = ProgressThresholds(0.10f, 1f),
    scaleMask = ProgressThresholds(0.10f, 1f),
    shapeMask = ProgressThresholds(0.10f, 0.90f)
)
private val DefaultReturnThresholdsArc = ProgressThresholdsGroup(
    fade = ProgressThresholds(0.60f, 0.90f),
    scale = ProgressThresholds(0f, 0.90f),
    scaleMask = ProgressThresholds(0f, 0.90f),
    shapeMask = ProgressThresholds(0.20f, 0.90f)
)

class MaterialContainerTransformSpec(
    pathMotionFactory: PathMotionFactory = LinearMotionFactory,
    durationMillis: Int = AnimationConstants.DefaultDurationMillis,
    delayMillis: Int = 0,
    easing: Easing = FastOutSlowInEasing,
    direction: TransitionDirection = TransitionDirection.Auto,
    fadeMode: FadeMode = FadeMode.In,
    val fitMode: FitMode = FitMode.Auto,
    val startContainerColor: Color = Color.Transparent,
    val endContainerColor: Color = Color.Transparent,
    fadeProgressThresholds: ProgressThresholds? = null,
    scaleProgressThresholds: ProgressThresholds? = null,
    val scaleMaskProgressThresholds: ProgressThresholds? = null,
    val shapeMaskProgressThresholds: ProgressThresholds? = null
) : SharedElementsTransitionSpec(
    pathMotionFactory,
    durationMillis,
    delayMillis,
    easing,
    direction,
    fadeMode,
    fadeProgressThresholds,
    scaleProgressThresholds
)

val DefaultMaterialContainerTransformSpec = MaterialContainerTransformSpec()

private fun MaterialContainerTransformSpec.progressThresholdsGroupFor(
    direction: TransitionDirection,
    pathMotion: PathMotion
): ProgressThresholdsGroup {
    val default = if (pathMotion is MaterialArcMotion) {
        if (direction == TransitionDirection.Enter)
            DefaultEnterThresholdsArc else DefaultReturnThresholdsArc
    } else {
        if (direction == TransitionDirection.Enter)
            DefaultEnterThresholds else DefaultReturnThresholds
    }
    return ProgressThresholdsGroup(
        fadeProgressThresholds ?: default.fade,
        scaleProgressThresholds ?: default.scale,
        scaleMaskProgressThresholds ?: default.scaleMask,
        shapeMaskProgressThresholds ?: default.shapeMask
    )
}
