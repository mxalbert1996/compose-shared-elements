package com.mxalbert.sharedelements

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.material.Colors
import androidx.compose.material.LocalAbsoluteElevation
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Shapes
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

@Immutable
internal class CompositionLocalValues(
    private val values: Array<ProvidedValue<*>>,
    private val colors: Colors,
    private val typography: androidx.compose.material.Typography,
    private val shapes: Shapes
) {

    @Composable
    @NonRestartableComposable
    fun Provider(content: @Composable () -> Unit) {
        CompositionLocalProvider(*values) {
            MaterialTheme(colors, typography, shapes, content)
        }
    }
}

internal val compositionLocalValues: CompositionLocalValues
    @Composable get() = CompositionLocalValues(
        compositionLocalList.map { it provides it.current }.toTypedArray(),
        MaterialTheme.colors,
        MaterialTheme.typography,
        MaterialTheme.shapes
    )
