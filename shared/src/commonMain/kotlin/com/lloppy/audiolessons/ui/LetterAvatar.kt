package com.lloppy.audiolessons.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LetterAvatar(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(size / 3))
            .background(Brush.linearGradient(listOf(color, color.copy(alpha = 0.7f)))),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text.take(1).uppercase(),
            color = Color.White,
            fontSize = (size.value * 0.42f).sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

private val AvatarPalette = listOf(
    Color(0xFF5B53E0),
    Color(0xFF12B5AC),
    Color(0xFFE0699F),
    Color(0xFFF0883E),
    Color(0xFF3E9BF0),
    Color(0xFF8E63D6),
)

fun avatarColor(seed: String): Color {
    if (seed.isEmpty()) return AvatarPalette.first()
    val index = seed.sumOf { it.code } % AvatarPalette.size
    return AvatarPalette[index]
}
