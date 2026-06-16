package com.lloppy.audiolessons.screens.player.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
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
    var expanded by remember { mutableStateOf(false) }
    val effective = dragValue ?: positionMs.toFloat()
    val max = durationMs.coerceAtLeast(1L).toFloat()

    SectionCard(Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp)) {
            HandleRow(
                hasPrev = hasPrev,
                hasNext = hasNext,
                expanded = expanded,
                onExpandedChange = { expanded = it },
                onPrevLesson = onPrevLesson,
                onNextLesson = onNextLesson,
            )
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
            Box(Modifier.fillMaxWidth().padding(bottom = 16.dp), contentAlignment = Alignment.Center) {
                PlayPauseButton(isPlaying = isPlaying, isBuffering = isBuffering, onClick = onPlayPause)
            }
            AnimatedVisibility(visible = expanded) {
                ExpandedControls(
                    onSkipBack = onSkipBack,
                    onSkipForward = onSkipForward,
                    onSeekToStart = { onSeek(0L) },
                    onSeekToEnd = { onSeek(durationMs.coerceAtLeast(0L)) },
                )
            }
        }
    }
}

@Composable
private fun ExpandedControls(
    onSkipBack: () -> Unit,
    onSkipForward: () -> Unit,
    onSeekToStart: () -> Unit,
    onSeekToEnd: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Top,
        ) {
            FilledControlButton(onClick = onSeekToStart, enabled = true) {
                SeekEdgeGlyph(toStart = true)
            }
            ControlButton(onClick = onSkipBack, enabled = true) {
                SkipLabel("−10")
            }
            ControlButton(onClick = onSkipForward, enabled = true) {
                SkipLabel("+10")
            }
            FilledControlButton(onClick = onSeekToEnd, enabled = true) {
                SeekEdgeGlyph(toStart = false)
            }
        }
    }
}

@Composable
private fun HandleRow(
    hasPrev: Boolean,
    hasNext: Boolean,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onPrevLesson: () -> Unit,
    onNextLesson: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp, bottom = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LessonNavButton(pointsRight = false, enabled = hasPrev, onClick = onPrevLesson)
        DragHandle(
            expanded = expanded,
            onExpandedChange = onExpandedChange,
            modifier = Modifier.weight(1f),
        )
        LessonNavButton(pointsRight = true, enabled = hasNext, onClick = onNextLesson)
    }
}

@Composable
private fun LessonNavButton(
    pointsRight: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val color = MaterialTheme.colorScheme.onSurfaceVariant
    Box(
        modifier = Modifier
            .size(width = 40.dp, height = 24.dp)
            .alpha(if (enabled) 1f else 0.3f)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = enabled,
            ) { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.size(width = 6.dp, height = 12.dp)) {
            val w = size.width
            val h = size.height
            val path = Path().apply {
                if (pointsRight) {
                    moveTo(0f, 0f)
                    lineTo(w, h / 2f)
                    lineTo(0f, h)
                } else {
                    moveTo(w, 0f)
                    lineTo(0f, h / 2f)
                    lineTo(w, h)
                }
            }
            drawPath(
                path = path,
                color = color,
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
            )
        }
    }
}

@Composable
private fun SeekEdgeGlyph(toStart: Boolean) {
    val color = MaterialTheme.colorScheme.onSurfaceVariant
    Canvas(Modifier.size(20.dp)) {
        val w = size.width
        val h = size.height
        val triangle = Path()
        if (toStart) {
            drawRect(color = color, topLeft = Offset(w * 0.16f, h * 0.28f), size = Size(w * 0.12f, h * 0.44f))
            triangle.moveTo(w * 0.82f, h * 0.28f)
            triangle.lineTo(w * 0.36f, h * 0.5f)
            triangle.lineTo(w * 0.82f, h * 0.72f)
        } else {
            drawRect(color = color, topLeft = Offset(w * 0.72f, h * 0.28f), size = Size(w * 0.12f, h * 0.44f))
            triangle.moveTo(w * 0.18f, h * 0.28f)
            triangle.lineTo(w * 0.64f, h * 0.5f)
            triangle.lineTo(w * 0.18f, h * 0.72f)
        }
        triangle.close()
        drawPath(path = triangle, color = color)
    }
}

@Composable
private fun DragHandle(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(durationMillis = 150),
        label = "handleRotation",
    )
    val chevronColor = MaterialTheme.colorScheme.onSurfaceVariant
    Box(
        modifier = modifier
            .height(24.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) { onExpandedChange(!expanded) }
            .pointerInput(Unit) {
                var total = 0f
                detectVerticalDragGestures(
                    onDragStart = { total = 0f },
                    onDragEnd = { total = 0f },
                    onVerticalDrag = { _, delta ->
                        total += delta
                        if (total < -32f) {
                            onExpandedChange(true)
                            total = 0f
                        } else if (total > 32f) {
                            onExpandedChange(false)
                            total = 0f
                        }
                    },
                )
            },
        contentAlignment = Alignment.TopCenter,
    ) {
        Canvas(
            modifier = Modifier
                .size(width = 26.dp, height = 6.dp)
                .rotate(rotation),
        ) {
            val w = size.width
            val h = size.height
            val path = Path().apply {
                moveTo(0f, h)
                lineTo(w / 2f, 0f)
                lineTo(w, h)
            }
            drawPath(
                path = path,
                color = chevronColor,
                style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
            )
        }
    }
}

@Composable
private fun PlayPauseButton(
    isPlaying: Boolean,
    isBuffering: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
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
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface,
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
