package com.lloppy.audiolessons.story.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val GradientTop = Color(0xFF2B1055)
private val GradientBottom = Color(0xFF7597DE)
private const val CARD_WIDTH_FRACTION = 0.9f
private const val CARD_BOTTOM_FRACTION = 130f / 1920f

@Composable
fun StoryFrame(
    pageImage: ImageBitmap?,
    title: String,
    durationMs: Long,
    amplitude: Float,
    modifier: Modifier = Modifier,
    attachment: ImageBitmap? = null,
    isPlaying: Boolean = false,
    progress: Float = 0f,
) {
    val resolver = LocalFontFamilyResolver.current
    val measurer = remember(resolver) { TextMeasurer(resolver, Density(1f), LayoutDirection.Ltr) }
    val density = LocalDensity.current

    BoxWithConstraints(
        modifier = modifier.background(Brush.verticalGradient(listOf(GradientTop, GradientBottom))),
        contentAlignment = Alignment.Center,
    ) {
        val frameWpx = constraints.maxWidth.toFloat()
        val frameHpx = constraints.maxHeight.toFloat()

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

        val cardScale = (frameWpx * CARD_WIDTH_FRACTION) / CARD_W
        val cardWpx = CARD_W * cardScale
        val cardHpx = storyCardHeight(attachment) * cardScale
        val bottomPx = frameHpx * CARD_BOTTOM_FRACTION

        Canvas(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = with(density) { bottomPx.toDp() })
                .size(with(density) { cardWpx.toDp() }, with(density) { cardHpx.toDp() }),
        ) {
            scale(cardScale, cardScale, pivot = Offset.Zero) {
                drawStoryCard(
                    attachment = attachment,
                    title = title,
                    timeMs = durationMs,
                    progress = progress,
                    isPlaying = isPlaying,
                    textMeasurer = measurer,
                )
            }
        }
    }
}

@Preview
@Composable
private fun StoryFramePreview() {
    StoryFrame(
        pageImage = null,
        title = "Урок 5 · Saludos y presentaciones",
        durationMs = 7_000L,
        amplitude = 0.6f,
        isPlaying = true,
        progress = 0.4f,
        modifier = Modifier.size(width = 270.dp, height = 480.dp),
    )
}
