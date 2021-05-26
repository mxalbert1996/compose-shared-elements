package com.mxalbert.sharedelements

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.material.LocalAbsoluteElevation
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.LocalTextStyle
import androidx.compose.runtime.*

@Suppress("UNCHECKED_CAST")
private val compositionLocalList = listOf(
    LocalAbsoluteElevation,
    LocalContentColor,
    LocalContentAlpha,
    LocalIndication,
    LocalTextSelectionColors,
    LocalTextStyle
) as List<ProvidableCompositionLocal<Any>>

@JvmInline
@Immutable
internal value class CompositionLocalValues(private val values: Array<ProvidedValue<*>>) {

    @Composable
    @NonRestartableComposable
    fun Provider(content: @Composable () -> Unit) {
        CompositionLocalProvider(*values, content = content)
    }

}

internal val compositionLocalValues: CompositionLocalValues
    @Composable get() = CompositionLocalValues(
        compositionLocalList.map { it provides it.current }.toTypedArray()
    )
