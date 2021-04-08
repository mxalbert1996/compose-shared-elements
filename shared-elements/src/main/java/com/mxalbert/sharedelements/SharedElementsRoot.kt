package com.mxalbert.sharedelements

import android.view.Choreographer
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.toSize
import com.mxalbert.sharedelements.SharedElementTransition.InProgress
import com.mxalbert.sharedelements.SharedElementTransition.WaitingForEndElementPosition
import com.mxalbert.sharedelements.SharedElementsTracker.State.*
import kotlinx.coroutines.launch
import kotlin.properties.Delegates

@Composable
internal fun BaseSharedElement(
    elementInfo: SharedElementInfo,
    placeholder: @Composable () -> Unit,
    overlay: @Composable (SharedElementsTransitionState) -> Unit,
    content: @Composable (Modifier) -> Unit
) {
    val (savedShouldHide, setShouldHide) = remember { mutableStateOf(false) }
    val rootState = LocalSharedElementsRootState.current
    val shouldHide = rootState.onElementRegistered(elementInfo)
    setShouldHide(shouldHide)

    val compositionLocalValues = compositionLocalValues
    val modifierWithOnPositioned = Modifier.onGloballyPositioned { coordinates ->
        rootState.onElementPositioned(
            elementInfo,
            compositionLocalValues,
            placeholder,
            overlay,
            coordinates,
            setShouldHide
        )
    }.run {
        if (shouldHide || savedShouldHide) alpha(0f) else this
    }

    content(modifierWithOnPositioned)

    DisposableEffect(elementInfo) {
        onDispose {
            rootState.onElementDisposed(elementInfo)
        }
    }
}

@Composable
fun SharedElementsRoot(
    content: @Composable SharedElementsRootScope.() -> Unit
) {
    val rootState = remember { SharedElementsRootState() }

    Box(modifier = Modifier.onGloballyPositioned { layoutCoordinates ->
        rootState.rootCoordinates = layoutCoordinates
    }) {
        CompositionLocalProvider(LocalSharedElementsRootState provides rootState) {
            rootState.scope.content()
        }
        SharedElementTransitionsOverlay(rootState)
    }

    DisposableEffect(Unit) {
        onDispose {
            rootState.onDispose()
        }
    }
}

interface SharedElementsRootScope {
    val isRunningTransition: Boolean
    fun prepareTransition(vararg elements: Any)
}

@Composable
private fun SharedElementTransitionsOverlay(rootState: SharedElementsRootState) {
    rootState.recomposeScope = currentRecomposeScope
    rootState.trackers.values.forEach { tracker ->
        val transition = tracker.transition
        if (transition != null) {
            val startElement = transition.startElement
            val endElement = (transition as? InProgress)?.endElement
            val spec = startElement.info.spec
            val animated = remember { Animatable(0f) }
            val fraction = animated.value
            startElement.info.onFractionChanged?.invoke(fraction)
            endElement?.info?.onFractionChanged?.invoke(1 - fraction)

            val direction = if (endElement == null) null else remember {
                val direction = spec.direction
                if (direction != TransitionDirection.Auto) direction else
                    calculateDirection(startElement.bounds, endElement.bounds)
            }

            startElement.Placeholder(fraction, endElement, direction, spec, tracker.pathMotion)

            val scope = rememberCoroutineScope()
            if (transition is InProgress && !transition.isAnimating) {
                transition.isAnimating = true
                scope.launch {
                    repeat(spec.waitForFrames) { withFrameNanos {} }
                    animated.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(
                            durationMillis = spec.durationMillis,
                            delayMillis = spec.delayMillis,
                            easing = spec.easing
                        )
                    )
                    transition.onTransitionFinished()
                }
            }
        }
    }
}

@Composable
private fun PositionedSharedElement.Placeholder(
    fraction: Float,
    end: PositionedSharedElement? = null,
    direction: TransitionDirection? = null,
    spec: SharedElementsTransitionSpec? = null,
    pathMotion: PathMotion? = null
) {
    overlay(
        SharedElementsTransitionState(
            fraction = fraction,
            startInfo = info,
            startBounds = bounds,
            startCompositionLocalValues = compositionLocalValues,
            startPlaceholder = placeholder,
            endInfo = end?.info,
            endBounds = end?.bounds,
            endCompositionLocalValues = end?.compositionLocalValues,
            endPlaceholder = end?.placeholder,
            direction = direction,
            spec = spec,
            pathMotion = pathMotion
        )
    )
}

private val LocalSharedElementsRootState = staticCompositionLocalOf<SharedElementsRootState> {
    error("SharedElementsRoot not found. SharedElement must be hosted in SharedElementsRoot.")
}

private class SharedElementsRootState {
    private val choreographer = ChoreographerWrapper()
    val scope = Scope()
    val trackers = mutableMapOf<Any, SharedElementsTracker>()
    var recomposeScope: RecomposeScope? = null
    var rootCoordinates: LayoutCoordinates? = null

    fun onElementRegistered(elementInfo: SharedElementInfo): Boolean {
        choreographer.removeCallback(elementInfo)
        return getTracker(elementInfo).onElementRegistered(elementInfo)
    }

    fun onElementPositioned(
        elementInfo: SharedElementInfo,
        compositionLocalValues: CompositionLocalValues,
        placeholder: @Composable () -> Unit,
        overlay: @Composable (SharedElementsTransitionState) -> Unit,
        coordinates: LayoutCoordinates,
        setShouldHide: (Boolean) -> Unit
    ) {
        val element = PositionedSharedElement(
            info = elementInfo,
            compositionLocalValues = compositionLocalValues,
            placeholder = placeholder,
            overlay = overlay,
            bounds = calculateElementBoundsInRoot(coordinates)
        )
        getTracker(elementInfo).onElementPositioned(element, setShouldHide)
    }

    fun onElementDisposed(elementInfo: SharedElementInfo) {
        choreographer.postCallback(elementInfo) {
            val tracker = getTracker(elementInfo)
            tracker.onElementUnregistered(elementInfo)
            if (tracker.isEmpty) trackers.remove(elementInfo.key)
        }
    }

    fun onDispose() {
        choreographer.clear()
    }

    private fun getTracker(elementInfo: SharedElementInfo): SharedElementsTracker {
        return trackers.getOrPut(elementInfo.key) {
            SharedElementsTracker { transition ->
                recomposeScope?.invalidate()
                scope.isRunningTransition = if (transition != null) true else
                    trackers.values.any { it.transition != null }
            }
        }
    }

    private fun calculateElementBoundsInRoot(elementCoordinates: LayoutCoordinates): Rect =
        Rect(
            rootCoordinates?.localPositionOf(elementCoordinates, Offset.Zero)
                ?: elementCoordinates.positionInRoot(), elementCoordinates.size.toSize()
        )

    private inner class Scope : SharedElementsRootScope {

        override var isRunningTransition: Boolean by mutableStateOf(false)

        override fun prepareTransition(vararg elements: Any) {
            elements.forEach {
                trackers[it]?.prepareTransition()
            }
        }

    }

}

private class SharedElementsTracker(
    private val onTransitionChanged: (SharedElementTransition?) -> Unit
) {
    private var state: State = Empty

    var pathMotion: PathMotion? = null

    var transition by Delegates.observable<SharedElementTransition?>(null) { _, oldValue, newValue ->
        if (oldValue != newValue) {
            if (newValue == null) pathMotion = null
            onTransitionChanged(newValue)
        }
    }

    val isEmpty: Boolean get() = state is Empty

    private fun StartElementPositioned.prepareTransition() {
        if (transition !is WaitingForEndElementPosition) {
            transition = WaitingForEndElementPosition(startElement)
        }
    }

    fun prepareTransition() {
        (state as? StartElementPositioned)?.apply {
            prepareTransition()
        }
    }

    fun onElementRegistered(elementInfo: SharedElementInfo): Boolean {
        var shouldHide = false
        when (val state = state) {
            is StartElementPositioned -> {
                if (!state.isRegistered(elementInfo)) {
                    shouldHide = true
                    this.state = EndElementRegistered(
                        startElement = state.startElement,
                        endElementInfo = elementInfo
                    )
                    state.prepareTransition()
                }
            }
            is StartElementRegistered -> {
                if (elementInfo != state.startElementInfo) {
                    this.state = StartElementRegistered(startElementInfo = elementInfo)
                }
            }
            is Empty -> {
                this.state = StartElementRegistered(startElementInfo = elementInfo)
            }
        }
        return shouldHide || transition != null
    }

    fun onElementPositioned(element: PositionedSharedElement, setShouldHide: (Boolean) -> Unit) {
        val state = state
        if (state is StartElementPositioned && element.info == state.startElementInfo) {
            state.startElement = element
        }
        when (state) {
            is EndElementRegistered -> {
                if (element.info == state.endElementInfo) {
                    this.state = InTransition
                    val spec = element.info.spec
                    this.pathMotion = spec.pathMotionFactory()
                    transition = InProgress(
                        startElement = state.startElement,
                        endElement = element,
                        onTransitionFinished = {
                            this.state = StartElementPositioned(startElement = element)
                            transition = null
                            setShouldHide(false)
                        }
                    )
                }
            }
            is StartElementPositioned -> Unit
            is StartElementRegistered -> {
                if (element.info == state.startElementInfo) {
                    this.state = StartElementPositioned(startElement = element)
                }
            }
            else -> Unit
        }
    }

    fun onElementUnregistered(elementInfo: SharedElementInfo) {
        when (val state = state) {
            is EndElementRegistered -> {
                if (elementInfo == state.endElementInfo) {
                    this.state = StartElementPositioned(startElement = state.startElement)
                    transition = null
                } else if (elementInfo == state.startElement.info) {
                    this.state = StartElementRegistered(startElementInfo = state.endElementInfo)
                    transition = null
                }
            }
            is StartElementRegistered -> {
                if (elementInfo == state.startElementInfo) {
                    this.state = Empty
                    transition = null
                }
            }
        }
    }

    private sealed class State {
        object Empty : State()

        open class StartElementRegistered(val startElementInfo: SharedElementInfo) : State() {
            open fun isRegistered(elementInfo: SharedElementInfo): Boolean {
                return elementInfo == startElementInfo
            }
        }

        open class StartElementPositioned(var startElement: PositionedSharedElement) :
            StartElementRegistered(startElement.info)

        class EndElementRegistered(
            startElement: PositionedSharedElement,
            val endElementInfo: SharedElementInfo
        ) : StartElementPositioned(startElement) {
            override fun isRegistered(elementInfo: SharedElementInfo): Boolean {
                return super.isRegistered(elementInfo) || elementInfo == endElementInfo
            }
        }

        object InTransition : State()
    }
}

enum class TransitionDirection {
    Auto, Enter, Return
}

enum class FadeMode {
    In, Out, Cross, Through
}

const val FadeThroughProgressThreshold = 0.35f

internal class SharedElementsTransitionState(
    val fraction: Float,
    val startInfo: SharedElementInfo,
    val startBounds: Rect,
    val startCompositionLocalValues: CompositionLocalValues,
    val startPlaceholder: @Composable () -> Unit,
    val endInfo: SharedElementInfo?,
    val endBounds: Rect?,
    val endCompositionLocalValues: CompositionLocalValues?,
    val endPlaceholder: (@Composable () -> Unit)?,
    val direction: TransitionDirection?,
    val spec: SharedElementsTransitionSpec?,
    val pathMotion: PathMotion?
)

internal val TopLeft = TransformOrigin(0f, 0f)

internal open class SharedElementInfo(
    val key: Any,
    val screenKey: Any,
    val spec: SharedElementsTransitionSpec,
    val onFractionChanged: ((Float) -> Unit)?
) {

    final override fun equals(other: Any?): Boolean =
        other is SharedElementInfo && other.key == key && other.screenKey == screenKey

    final override fun hashCode(): Int = 31 * key.hashCode() + screenKey.hashCode()

}

private class PositionedSharedElement(
    val info: SharedElementInfo,
    val compositionLocalValues: CompositionLocalValues,
    val placeholder: @Composable () -> Unit,
    val overlay: @Composable (SharedElementsTransitionState) -> Unit,
    val bounds: Rect
)

private sealed class SharedElementTransition(val startElement: PositionedSharedElement) {

    class WaitingForEndElementPosition(startElement: PositionedSharedElement) :
        SharedElementTransition(startElement)

    class InProgress(
        startElement: PositionedSharedElement,
        val endElement: PositionedSharedElement,
        val onTransitionFinished: () -> Unit
    ) : SharedElementTransition(startElement) {
        var isAnimating = false
    }
}

private class ChoreographerWrapper {
    private val callbacks = mutableMapOf<SharedElementInfo, Choreographer.FrameCallback>()
    private val choreographer = Choreographer.getInstance()

    fun postCallback(elementInfo: SharedElementInfo, callback: () -> Unit) {
        if (callbacks.containsKey(elementInfo)) return

        val frameCallback = Choreographer.FrameCallback {
            callbacks.remove(elementInfo)
            callback()
        }
        callbacks[elementInfo] = frameCallback
        choreographer.postFrameCallback(frameCallback)
    }

    fun removeCallback(elementInfo: SharedElementInfo) {
        callbacks.remove(elementInfo)?.also(choreographer::removeFrameCallback)
    }

    fun clear() {
        callbacks.values.forEach(choreographer::removeFrameCallback)
        callbacks.clear()
    }
}
