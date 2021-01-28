package com.mxalbert.sharedelements

import androidx.compose.foundation.AmbientIndication
import androidx.compose.material.AmbientAbsoluteElevation
import androidx.compose.material.AmbientContentAlpha
import androidx.compose.material.AmbientContentColor
import androidx.compose.material.AmbientTextStyle
import androidx.compose.runtime.*
import androidx.compose.ui.selection.AmbientTextSelectionColors

@Suppress("UNCHECKED_CAST")
private val ambientList = listOf(
    AmbientAbsoluteElevation,
    AmbientContentColor,
    AmbientContentAlpha,
    AmbientIndication,
    AmbientTextSelectionColors,
    AmbientTextStyle
) as List<ProvidableAmbient<Any>>

internal inline class AmbientValues(private val values: Array<ProvidedValue<*>>) {

    @Suppress("ComposableNaming")
    @Composable
    @ComposableContract(restartable = false)
    fun provided(block: @Composable () -> Unit) {
        Providers(*values, content = block)
    }

}

internal val ambientValues: AmbientValues
    @Composable get() = AmbientValues(ambientList.map { it provides it.current }.toTypedArray())
