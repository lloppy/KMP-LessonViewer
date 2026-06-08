package com.lloppy.audiolessons.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import com.lloppy.audiolessons.theme.LocalIsDarkTheme

@Composable
fun AppBackground(modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val top = colors.primary.copy(alpha = if (LocalIsDarkTheme.current) 0.10f else 0.07f)
    Box(
        modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(top, colors.background, colors.background)))
    )
}
