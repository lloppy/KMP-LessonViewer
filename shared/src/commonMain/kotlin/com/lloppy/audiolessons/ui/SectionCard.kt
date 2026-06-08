package com.lloppy.audiolessons.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lloppy.audiolessons.theme.LocalIsDarkTheme

@Composable
fun SectionCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    cornerRadius: Dp = 22.dp,
    content: @Composable () -> Unit,
) {
    val dark = LocalIsDarkTheme.current
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(cornerRadius),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = if (dark) 2.dp else 0.dp,
        shadowElevation = if (dark) 0.dp else 3.dp,
        onClick = onClick ?: {},
        enabled = onClick != null,
        content = content,
    )
}
