package com.rvbhati.purestatus.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    borderGlowColor: Color? = null,
    isDark: Boolean = isSystemInDarkTheme(),
    contentPadding: Dp = 16.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)

    val backgroundColor = if (isDark) {
        Color(0x1A202E).copy(alpha = 0.65f)
    } else {
        Color.White.copy(alpha = 0.85f)
    }

    val defaultBorderBrush = if (isDark) {
        Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.22f),
                Color.White.copy(alpha = 0.05f)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.90f),
                Color.Black.copy(alpha = 0.08f)
            )
        )
    }

    val activeBorderBrush = borderGlowColor?.let {
        Brush.verticalGradient(
            colors = listOf(
                it.copy(alpha = 0.9f),
                it.copy(alpha = 0.2f)
            )
        )
    } ?: defaultBorderBrush

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(backgroundColor)
            .border(
                BorderStroke(
                    width = if (borderGlowColor != null) 1.5.dp else 1.dp,
                    brush = activeBorderBrush
                ),
                shape = shape
            )
            .padding(contentPadding),
        content = content
    )
}
