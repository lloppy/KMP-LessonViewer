package com.lloppy.audiolessons.story.components

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.sp

// Единый источник правды для карточки истории — рисуется в опорной системе координат шириной CARD_W (px).
// Превью масштабирует DrawScope под свою ширину, экспорт рисует 1:1 в bitmap.

const val CARD_W = 972f
private const val PAD = 36f
private const val CORNER = 48f
private const val ATT_CORNER = 28f
private const val ATT_GAP = 24f
private const val ICON_D = 96f
private const val GAP = 24f
private const val TITLE_SLOT = 40f
private const val WAVE_H = 44f
private const val MID_SPACER = 14f
private const val ROW_H = TITLE_SLOT + MID_SPACER + WAVE_H
private const val DURATION_RESERVE = 130f
private const val TITLE_PX = 34f
private const val DURATION_PX = 32f

private val WAVE_PATTERN = floatArrayOf(
    0.3f, 0.6f, 0.9f, 0.5f, 0.8f, 1f, 0.4f, 0.7f, 0.95f, 0.5f, 0.85f, 0.6f,
    0.3f, 0.7f, 0.5f, 0.9f, 0.4f, 0.6f, 0.5f, 0.8f, 0.35f, 0.7f, 0.55f, 0.9f,
)

data class StoryCardColors(
    val card: Color = Color.White,
    val accent: Color = Color(0xFF5B8DEF),
    val title: Color = Color(0xFF1A1A1A),
    val glyph: Color = Color.White,
) {
    val barIdle: Color get() = accent.copy(alpha = 0.33f)
}

private fun attachmentHeight(attachment: ImageBitmap?): Float =
    if (attachment == null) 0f else (CARD_W - 2 * PAD) * attachment.height / attachment.width

fun storyCardHeight(attachment: ImageBitmap?): Float {
    val rowTop = PAD + if (attachment != null) attachmentHeight(attachment) + ATT_GAP else 0f
    return rowTop + ROW_H + PAD
}

fun DrawScope.drawStoryCard(
    attachment: ImageBitmap?,
    title: String,
    timeMs: Long,
    progress: Float,
    isPlaying: Boolean,
    textMeasurer: TextMeasurer,
    colors: StoryCardColors = StoryCardColors(),
) {
    val attH = attachmentHeight(attachment)
    val rowTop = PAD + if (attachment != null) attH + ATT_GAP else 0f
    val cardH = rowTop + ROW_H + PAD

    drawRoundRect(
        color = colors.card,
        topLeft = Offset(0f, 0f),
        size = Size(CARD_W, cardH),
        cornerRadius = CornerRadius(CORNER, CORNER),
    )

    if (attachment != null) {
        val w = CARD_W - 2 * PAD
        val clip = Path().apply {
            addRoundRect(RoundRect(PAD, PAD, PAD + w, PAD + attH, CornerRadius(ATT_CORNER, ATT_CORNER)))
        }
        clipPath(clip) {
            drawImage(
                image = attachment,
                dstOffset = IntOffset(PAD.toInt(), PAD.toInt()),
                dstSize = IntSize(w.toInt(), attH.toInt()),
            )
        }
    }

    val rowCenterY = rowTop + ROW_H / 2f
    val iconCx = PAD + ICON_D / 2f
    drawCircle(color = colors.accent, radius = ICON_D / 2f, center = Offset(iconCx, rowCenterY))
    drawPlayPause(isPlaying, Offset(iconCx, rowCenterY), colors.glyph)

    val colX = PAD + ICON_D + GAP
    val waveEnd = CARD_W - PAD - DURATION_RESERVE
    val colW = (waveEnd - colX).coerceAtLeast(1f)

    val titleLayout = textMeasurer.measure(
        text = title,
        style = TextStyle(color = colors.title, fontSize = TITLE_PX.sp, fontWeight = FontWeight.SemiBold),
        overflow = TextOverflow.Ellipsis,
        maxLines = 1,
        constraints = Constraints(maxWidth = colW.toInt()),
    )
    drawText(titleLayout, topLeft = Offset(colX, rowTop + (TITLE_SLOT - titleLayout.size.height) / 2f))

    val waveCenterY = rowTop + TITLE_SLOT + MID_SPACER + WAVE_H / 2f
    drawWaveform(colX, waveEnd, waveCenterY, progress, colors)

    val durationLayout = textMeasurer.measure(
        text = formatTime(timeMs),
        style = TextStyle(color = colors.accent, fontSize = DURATION_PX.sp, fontWeight = FontWeight.Medium),
    )
    drawText(
        durationLayout,
        topLeft = Offset(CARD_W - PAD - durationLayout.size.width, rowCenterY - durationLayout.size.height / 2f),
    )
}

private fun DrawScope.drawPlayPause(isPlaying: Boolean, center: Offset, color: Color) {
    if (isPlaying) {
        val barW = 11f
        val bh = 34f
        val gap = 12f
        drawRect(color, topLeft = Offset(center.x - gap / 2f - barW, center.y - bh / 2f), size = Size(barW, bh))
        drawRect(color, topLeft = Offset(center.x + gap / 2f, center.y - bh / 2f), size = Size(barW, bh))
    } else {
        val path = Path().apply {
            moveTo(center.x - 16f, center.y - 22f)
            lineTo(center.x + 24f, center.y)
            lineTo(center.x - 16f, center.y + 22f)
            close()
        }
        drawPath(path, color)
    }
}

private fun DrawScope.drawWaveform(start: Float, end: Float, centerY: Float, progress: Float, colors: StoryCardColors) {
    val count = WAVE_PATTERN.size
    val region = end - start
    if (region <= 0f) return
    val slot = region / count
    val barW = slot * 0.5f
    val played = progress.coerceIn(0f, 1f) * count
    for (i in 0 until count) {
        val h = WAVE_H * WAVE_PATTERN[i]
        val cx = start + slot * i + slot / 2f
        val color = if (i < played) colors.accent else colors.barIdle
        drawRoundRect(
            color = color,
            topLeft = Offset(cx - barW / 2f, centerY - h / 2f),
            size = Size(barW, h),
            cornerRadius = CornerRadius(barW / 2f, barW / 2f),
        )
    }
}

fun formatTime(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    val ss = if (seconds < 10) "0$seconds" else "$seconds"
    return "$minutes:$ss"
}
