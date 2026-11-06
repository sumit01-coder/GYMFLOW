package com.gymflow.core.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.gymflow.core.theme.GymFlowShapesTokens
import com.gymflow.core.theme.GymFlowTheme

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GymFlowCard(
    modifier: Modifier = Modifier,
    shape: Shape = GymFlowShapesTokens.CardMain,
    backgroundColor: Color = GymFlowTheme.colors.surface,
    borderColor: Color = GymFlowTheme.colors.border,
    borderWidth: Dp = 1.dp,
    contentPadding: Dp = 16.dp,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val clickableModifier = when {
        onLongClick != null -> {
            modifier.combinedClickable(
                onClick = { onClick?.invoke() },
                onLongClick = onLongClick
            )
        }
        onClick != null -> {
            modifier.clickable(onClick = onClick)
        }
        else -> modifier
    }

    Surface(
        modifier = clickableModifier,
        shape = shape,
        color = backgroundColor,
        border = BorderStroke(borderWidth, borderColor),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            content = content
        )
    }
}
