package com.mxalbert.sharedelements

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.util.packFloats
import androidx.compose.ui.util.unpackFloat1
import androidx.compose.ui.util.unpackFloat2

@JvmInline
@Immutable
value class ProgressThresholds(private val packedValue: Long) {

    @Stable
    val start: Float
        get() = unpackFloat1(packedValue)

    @Stable
    val end: Float
        get() = unpackFloat2(packedValue)

    @Suppress("NOTHING_TO_INLINE")
    @Stable
    inline operator fun component1(): Float = start

    @Suppress("NOTHING_TO_INLINE")
    @Stable
    inline operator fun component2(): Float = end

}

@Stable
fun ProgressThresholds(start: Float, end: Float) = ProgressThresholds(packFloats(start, end))

@Stable
internal fun ProgressThresholds.applyTo(fraction: Float): Float = when {
    fraction < start -> 0f
    fraction in start..end -> (fraction - start) / (end - start)
    else -> 1f
}
