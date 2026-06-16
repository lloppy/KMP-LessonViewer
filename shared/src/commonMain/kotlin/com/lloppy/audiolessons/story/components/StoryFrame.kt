package com.lloppy.audiolessons.story.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val GradientTop = Color(0xFF2B1055)
private val GradientBottom = Color(0xFF7597DE)
private val CardColor = Color(0xFFFFFFFF)
private val AccentColor = Color(0xFF5B8DEF)

@Composable
fun StoryFrame(
    pageImage: ImageBitmap?,
    title: String,
    durationMs: Long,
    amplitude: Float,
    modifier: Modifier = Modifier,
    attachment: ImageBitmap? = null,
    isPlaying: Boolean = false,
) {
    Box(
        modifier = modifier.background(Brush.verticalGradient(listOf(GradientTop, GradientBottom))),
        contentAlignment = Alignment.Center,
    ) {
        if (attachment == null && pageImage != null) {
            Image(
                bitmap = pageImage,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth(0.86f)
                    .padding(top = 40.dp, bottom = 220.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White),
                contentScale = ContentScale.Fit,
            )
        } else if (attachment == null) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 40.dp),
            )
        }

        VoiceCard(
            title = title,
            durationMs = durationMs,
            amplitude = amplitude,
            attachment = attachment,
            isPlaying = isPlaying,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(0.9f)
                .padding(bottom = 64.dp),
        )
    }
}

@Composable
private fun VoiceCard(
    title: String,
    durationMs: Long,
    amplitude: Float,
    attachment: ImageBitmap?,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(CardColor)
            .padding(12.dp),
    ) {
        if (attachment != null) {
            Image(
                bitmap = attachment,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White),
                contentScale = ContentScale.Crop,
            )
            Spacer(Modifier.height(10.dp))
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(48.dp).clip(CircleShape).background(AccentColor),
                contentAlignment = Alignment.Center,
            ) {
                PlayPauseIcon(isPlaying = isPlaying, color = Color.White, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Color(0xFF1A1A1A),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                )
                Spacer(Modifier.height(8.dp))
                Waveform(amplitude = amplitude, modifier = Modifier.fillMaxWidth().height(22.dp))
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text = formatDuration(durationMs),
                color = AccentColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun PlayPauseIcon(isPlaying: Boolean, color: Color, modifier: Modifier = Modifier) {
    androidx.compose.foundation.Canvas(modifier) {
        val w = size.width
        val h = size.height
        if (isPlaying) {
            val barW = w * 0.26f
            val gap = w * 0.18f
            drawRect(color = color, topLeft = Offset((w - 2 * barW - gap) / 2f, h * 0.12f), size = Size(barW, h * 0.76f))
            drawRect(color = color, topLeft = Offset((w + gap) / 2f, h * 0.12f), size = Size(barW, h * 0.76f))
        } else {
            val path = Path().apply {
                moveTo(w * 0.22f, h * 0.12f)
                lineTo(w * 0.86f, h * 0.5f)
                lineTo(w * 0.22f, h * 0.88f)
                close()
            }
            drawPath(path = path, color = color)
        }
    }
}

@Composable
private fun Waveform(amplitude: Float, modifier: Modifier = Modifier) {
    val pattern = listOf(0.3f, 0.6f, 0.9f, 0.5f, 0.8f, 1f, 0.4f, 0.7f, 0.95f, 0.5f, 0.85f, 0.6f, 0.3f, 0.7f, 0.5f, 0.9f, 0.4f, 0.6f)
    val boost = 0.7f + amplitude.coerceIn(0f, 1f) * 0.3f
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        pattern.forEach { h ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height((22f * h * boost).dp)
                    .clip(RoundedCornerShape(50))
                    .background(AccentColor.copy(alpha = 0.85f)),
            )
        }
    }
}

private fun formatDuration(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    val ss = if (seconds < 10) "0$seconds" else "$seconds"
    return "$minutes:$ss"
}

@Preview
@Composable
private fun StoryFramePreview() {
    StoryFrame(
        pageImage = null,
        title = "Урок 5 · Saludos y presentaciones",
        durationMs = 7_000L,
        amplitude = 0.6f,
        modifier = Modifier.size(width = 270.dp, height = 480.dp),
    )
}

@Preview
@Composable
private fun VoiceCardPreview() {
    Box(Modifier.background(Color(0xFF3B2A66)).padding(16.dp)) {
        VoiceCard(
            title = "Урок 5 · Saludos",
            durationMs = 12_000L,
            amplitude = 0.8f,
            attachment = null,
            isPlaying = true,
            modifier = Modifier.width(320.dp),
        )
    }
}
