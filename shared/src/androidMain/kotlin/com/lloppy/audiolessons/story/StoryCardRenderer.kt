package com.lloppy.audiolessons.story

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF

class StoryCardRenderer(
    private val attachment: Bitmap?,
    private val title: String,
) {
    private val white = Color.WHITE
    private val accent = Color.parseColor("#5B8DEF")
    private val titleColor = Color.parseColor("#1A1A1A")

    private val pad = 36
    private val iconD = 96
    private val gap = 24
    private val rowH = iconD
    private val durationReserve = 130

    private val attH: Int = run {
        val a = attachment ?: return@run 0
        val w = CARD_W - 2 * pad
        (w.toFloat() * a.height / a.width).toInt()
    }
    private val rowTop = pad + if (attachment != null) attH + 24 else 0
    val cardH = rowTop + rowH + pad

    private val buffers = arrayOf(
        Bitmap.createBitmap(CARD_W, cardH, Bitmap.Config.ARGB_8888),
        Bitmap.createBitmap(CARD_W, cardH, Bitmap.Config.ARGB_8888),
    )
    private var toggle = 0

    private val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = white }
    private val accentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = accent }
    private val glyphPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = white }
    private val barPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = titleColor
        textSize = 34f
        isFakeBoldText = true
    }
    private val durationPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = accent
        textSize = 32f
        textAlign = Paint.Align.RIGHT
    }
    private val imgPaint = Paint(Paint.FILTER_BITMAP_FLAG)

    fun render(progress: Float, timeMs: Long, isPlaying: Boolean): Bitmap {
        val bmp = buffers[toggle]
        toggle = toggle xor 1
        bmp.eraseColor(Color.TRANSPARENT)
        val canvas = Canvas(bmp)

        canvas.drawRoundRect(RectF(0f, 0f, CARD_W.toFloat(), cardH.toFloat()), 48f, 48f, cardPaint)

        if (attachment != null) {
            val dst = RectF(pad.toFloat(), pad.toFloat(), (CARD_W - pad).toFloat(), (pad + attH).toFloat())
            canvas.save()
            val clip = Path().apply { addRoundRect(dst, 28f, 28f, Path.Direction.CW) }
            canvas.clipPath(clip)
            canvas.drawBitmap(attachment, Rect(0, 0, attachment.width, attachment.height), dst, imgPaint)
            canvas.restore()
        }

        val rowCenterY = rowTop + rowH / 2f
        val iconCx = pad + iconD / 2f
        canvas.drawCircle(iconCx, rowCenterY, iconD / 2f, accentPaint)
        drawPlayPause(canvas, iconCx, rowCenterY, isPlaying)

        val waveStart = pad + iconD + gap
        val waveEnd = CARD_W - pad - durationReserve
        drawWaveform(canvas, waveStart.toFloat(), waveEnd.toFloat(), rowCenterY, progress)

        canvas.drawText(formatTime(timeMs), (CARD_W - pad).toFloat(), rowCenterY - (durationPaint.descent() + durationPaint.ascent()) / 2f, durationPaint)

        return bmp
    }

    private fun drawPlayPause(canvas: Canvas, cx: Float, cy: Float, isPlaying: Boolean) {
        if (isPlaying) {
            val bw = 11f
            val bh = 34f
            val g = 12f
            canvas.drawRect(cx - g / 2f - bw, cy - bh / 2f, cx - g / 2f, cy + bh / 2f, glyphPaint)
            canvas.drawRect(cx + g / 2f, cy - bh / 2f, cx + g / 2f + bw, cy + bh / 2f, glyphPaint)
        } else {
            val path = Path().apply {
                moveTo(cx - 16f, cy - 22f)
                lineTo(cx + 24f, cy)
                lineTo(cx - 16f, cy + 22f)
                close()
            }
            canvas.drawPath(path, glyphPaint)
        }
    }

    private fun drawWaveform(canvas: Canvas, start: Float, end: Float, centerY: Float, progress: Float) {
        val pattern = floatArrayOf(0.3f, 0.6f, 0.9f, 0.5f, 0.8f, 1f, 0.4f, 0.7f, 0.95f, 0.5f, 0.85f, 0.6f, 0.3f, 0.7f, 0.5f, 0.9f, 0.4f, 0.6f, 0.5f, 0.8f, 0.35f, 0.7f, 0.55f, 0.9f)
        val count = pattern.size
        val region = end - start
        if (region <= 0f) return
        val slot = region / count
        val barW = slot * 0.5f
        val maxH = 44f
        val played = (progress.coerceIn(0f, 1f) * count)
        for (i in 0 until count) {
            val h = maxH * pattern[i]
            val cx = start + slot * i + slot / 2f
            barPaint.color = if (i < played) accent else (accent and 0x00FFFFFF) or 0x55000000.toInt()
            canvas.drawRoundRect(cx - barW / 2f, centerY - h / 2f, cx + barW / 2f, centerY + h / 2f, barW, barW, barPaint)
        }
    }

    private fun formatTime(ms: Long): String {
        val totalSeconds = (ms / 1000).coerceAtLeast(0)
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        val ss = if (seconds < 10) "0$seconds" else "$seconds"
        return "$minutes:$ss"
    }

    companion object {
        const val CARD_W = 972
    }
}
