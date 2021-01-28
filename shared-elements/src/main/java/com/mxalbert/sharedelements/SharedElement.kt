package com.mxalbert.sharedelements

import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.AmbientDensity
import androidx.compose.ui.unit.round
import androidx.compose.ui.zIndex

@Composable
fun SharedElement(
    key: Any,
    screenKey: Any,
    transitionSpec: SharedElementsTransitionSpec = DefaultSharedElementsTransitionSpec,
    placeholder: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val elementInfo = remember(key, screenKey) { SharedElementInfo(key, screenKey, transitionSpec) }
    val realPlaceholder = placeholder ?: content
    BaseSharedElement(
        elementInfo,
        realPlaceholder,
        { Placeholder(it) },
        { ElementContainer(modifier = it, content = content) }
    )
}

@Composable
private fun Placeholder(state: SharedElementsTransitionState) {
    with(AmbientDensity.current) {
        var fraction = state.fraction
        val startBounds = state.startBounds
        val endBounds = state.endBounds
        val (startScaleX, startScaleY) = calculateScale(startBounds, endBounds, fraction)
        val offset = calculateOffset(
            startBounds, endBounds,
            fraction, state.pathMotion,
            startBounds.width * startScaleX
        ).round()

        @Composable
        fun Container(
            ambientValues: AmbientValues,
            bounds: Rect,
            scaleX: Float,
            scaleY: Float,
            isStart: Boolean,
            fraction: Float,
            content: @Composable () -> Unit,
            zIndex: Float = 0f,
        ) {
            val alpha = calculateAlpha(state.direction, state.spec?.fadeMode, isStart, fraction)
            if (alpha > 0) ambientValues.provided {
                ElementContainer(
                    modifier = Modifier.size(
                        bounds.width.toDp(),
                        bounds.height.toDp()
                    ).offset { offset }.graphicsLayer {
                        this.transformOrigin = TopLeft
                        this.scaleX = scaleX
                        this.scaleY = scaleY
                        this.alpha = alpha
                    }.run {
                        if (zIndex == 0f) this else zIndex(zIndex)
                    },
                    content = content
                )
            }
        }

        Container(
            state.startAmbientValues,
            startBounds,
            startScaleX, startScaleY,
            true, fraction,
            state.startPlaceholder
        )

        if (endBounds != null) {
            fraction = 1 - fraction
            val (endScaleX, endScaleY) = calculateScale(endBounds, startBounds, fraction)
            Container(
                state.endAmbientValues!!,
                endBounds,
                endScaleX, endScaleY,
                false, fraction,
                state.endPlaceholder!!,
                if (state.spec?.fadeMode == FadeMode.Out) -1f else 0f
            )
        }
    }
}
