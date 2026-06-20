package com.lloppy.audiolessons.story

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.font.createFontFamilyResolver
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import com.lloppy.audiolessons.story.components.CARD_W
import com.lloppy.audiolessons.story.components.drawStoryCard
import com.lloppy.audiolessons.story.components.storyCardHeight
import kotlin.math.ceil

class StoryCardRenderer(
    private val attachment: ImageBitmap?,
    private val title: String,
    context: Context,
) {
    private val cardW: Int = CARD_W.toInt()
    val cardH: Int = ceil(storyCardHeight(attachment).toDouble()).toInt()

    private val measurer = TextMeasurer(createFontFamilyResolver(context), Density(1f), LayoutDirection.Ltr)
    private val drawScope = CanvasDrawScope()
    private val buffers = arrayOf(ImageBitmap(cardW, cardH), ImageBitmap(cardW, cardH))
    private var toggle = 0

    fun render(progress: Float, timeMs: Long, isPlaying: Boolean): Bitmap {
        val image = buffers[toggle]
        toggle = toggle xor 1
        val canvas = Canvas(image)
        drawScope.draw(Density(1f), LayoutDirection.Ltr, canvas, Size(cardW.toFloat(), cardH.toFloat())) {
            drawRect(Color.Transparent, blendMode = BlendMode.Clear)
            drawStoryCard(
                attachment = attachment,
                title = title,
                timeMs = timeMs,
                progress = progress,
                isPlaying = isPlaying,
                textMeasurer = measurer,
            )
        }
        return image.asAndroidBitmap()
    }
}
