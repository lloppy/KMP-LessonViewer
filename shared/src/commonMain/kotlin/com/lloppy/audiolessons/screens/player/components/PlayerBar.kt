package com.lloppy.audiolessons.screens.player.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lloppy.audiolessons.ui.CircleIconButton
import com.lloppy.audiolessons.ui.SectionCard

@Composable
fun PlayerBar(
    isPlaying: Boolean,
    isBuffering: Boolean,
    positionMs: Long,
    durationMs: Long,
    hasPrev: Boolean,
    hasNext: Boolean,
    onPlayPause: () -> Unit,
    onSeek: (Long) -> Unit,
    onSkipBack: () -> Unit,
    onSkipForward: () -> Unit,
    onPrevLesson: () -> Unit,
    onNextLesson: () -> Unit,
) {
    var dragValue by remember { mutableStateOf<Float?>(null) }
    val effective = dragValue ?: positionMs.toFloat()
    val max = durationMs.coerceAtLeast(1L).toFloat()

    SectionCard(Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp)) {
            Slider(
                value = effective.coerceIn(0f, max),
                onValueChange = { dragValue = it },
                onValueChangeFinished = {
                    dragValue?.let { onSeek(it.toLong()) }
                    dragValue = null
                },
                valueRange = 0f..max,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                ),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(formatTime(effective.toLong()), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(formatTime(durationMs), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FilledControlButton(onClick = onPrevLesson, enabled = hasPrev) {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Предыдущий урок",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                ControlButton(onClick = onSkipBack, enabled = true) {
                    SkipLabel("−10")
                }
                Surface(
                    onClick = onPlayPause,
                    modifier = Modifier.size(64.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    shadowElevation = 4.dp,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        when {
                            isBuffering -> CircularProgressIndicator(
                                Modifier.size(26.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp,
                            )
                            isPlaying -> PauseGlyph()
                            else -> Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = "Играть",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(34.dp),
                            )
                        }
                    }
                }
                ControlButton(onClick = onSkipForward, enabled = true) {
                    SkipLabel("+10")
                }
                FilledControlButton(onClick = onNextLesson, enabled = hasNext) {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Следующий урок",
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}

@Composable
private fun FilledControlButton(
    onClick: () -> Unit,
    enabled: Boolean,
    content: @Composable () -> Unit,
) {
    CircleIconButton(
        onClick = { if (enabled) onClick() },
        modifier = Modifier.alpha(if (enabled) 1f else 0.35f),
        size = 48.dp,
    ) {
        content()
    }
}

@Composable
private fun ControlButton(
    onClick: () -> Unit,
    enabled: Boolean,
    content: @Composable () -> Unit,
) {
    Surface(
        onClick = { if (enabled) onClick() },
        modifier = Modifier.size(48.dp).alpha(if (enabled) 1f else 0.3f),
        shape = CircleShape,
        color = Color.Transparent,
    ) {
        Box(contentAlignment = Alignment.Center) { content() }
    }
}

@Composable
private fun SkipLabel(text: String) {
    Text(
        text = text,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun PauseGlyph() {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(2) {
            Box(
                Modifier
                    .size(width = 6.dp, height = 26.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.onPrimary)
            )
        }
    }
}

private fun formatTime(ms: Long): String {
    if (ms <= 0) return "0:00"
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    val ss = if (seconds < 10) "0$seconds" else "$seconds"
    return "$minutes:$ss"
}
