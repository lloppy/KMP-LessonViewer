package com.lloppy.audiolessons.story

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.net.Uri
import android.os.SystemClock
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.core.content.FileProvider
import androidx.media3.common.MediaItem
import androidx.media3.common.OverlaySettings
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.BitmapOverlay
import androidx.media3.effect.OverlayEffect
import androidx.media3.effect.StaticOverlaySettings
import androidx.media3.effect.TextureOverlay
import androidx.media3.transformer.Composition
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.EditedMediaItemSequence
import androidx.media3.transformer.Effects
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.Transformer
import com.google.common.collect.ImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

private const val FRAME_W = 1080
private const val FRAME_H = 1920
private const val BOTTOM_MARGIN = 130
private const val GRADIENT_TOP = 0xFF2B1055.toInt()
private const val GRADIENT_BOTTOM = 0xFF7597DE.toInt()

@UnstableApi
class Media3StoryExporter(context: Context) : StoryExporter {

    private val appContext = context.applicationContext
    private val _state = MutableStateFlow<ExportState>(ExportState.Idle)
    override val state: StateFlow<ExportState> = _state.asStateFlow()

    override suspend fun export(attachment: ImageBitmap?, title: String, audioPath: String, durationMs: Long): String {
        val durationUs = durationMs.coerceAtLeast(1_000L) * 1_000

        val attachmentBmp = attachment?.let {
            val raw = it.asAndroidBitmap()
            if (raw.config == Bitmap.Config.HARDWARE) raw.copy(Bitmap.Config.ARGB_8888, false) else raw
        }

        val bgFile = withContext(Dispatchers.IO) {
            val bg = Bitmap.createBitmap(FRAME_W, FRAME_H, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bg)
            val paint = Paint().apply {
                shader = LinearGradient(0f, 0f, 0f, FRAME_H.toFloat(), GRADIENT_TOP, GRADIENT_BOTTOM, Shader.TileMode.CLAMP)
            }
            canvas.drawRect(0f, 0f, FRAME_W.toFloat(), FRAME_H.toFloat(), paint)
            val dir = File(appContext.cacheDir, "stories").apply { mkdirs() }
            val f = File(dir, "story_bg_${SystemClock.elapsedRealtime()}.png")
            FileOutputStream(f).use { out -> bg.compress(Bitmap.CompressFormat.PNG, 100, out) }
            f
        }
        val outFile = File(File(appContext.cacheDir, "stories").apply { mkdirs() }, "story_${SystemClock.elapsedRealtime()}.mp4")

        val renderer = StoryCardRenderer(attachmentBmp, title)
        val cardCenterFromBottom = BOTTOM_MARGIN + renderer.cardH / 2f
        val ndcY = 2f * cardCenterFromBottom / FRAME_H - 1f
        val settings = StaticOverlaySettings.Builder()
            .setBackgroundFrameAnchor(0f, ndcY)
            .setOverlayFrameAnchor(0f, 0f)
            .build()
        val overlay = AnimatedCardOverlay(renderer, durationUs, settings)
        val overlayEffect = OverlayEffect(ImmutableList.of<TextureOverlay>(overlay))

        return withContext(Dispatchers.Main) {
            suspendCancellableCoroutine { cont ->
                val bgItem = EditedMediaItem.Builder(MediaItem.fromUri(Uri.fromFile(bgFile)))
                    .setDurationUs(durationUs)
                    .setFrameRate(30)
                    .setEffects(Effects(emptyList(), listOf(overlayEffect)))
                    .build()
                val audioItem = EditedMediaItem.Builder(MediaItem.fromUri(Uri.fromFile(File(audioPath)))).build()
                val composition = Composition.Builder(
                    EditedMediaItemSequence.Builder(bgItem).build(),
                    EditedMediaItemSequence.Builder(audioItem).build(),
                ).build()

                _state.value = ExportState.Exporting(0f)
                val transformer = Transformer.Builder(appContext)
                    .addListener(object : Transformer.Listener {
                        override fun onCompleted(composition: Composition, result: ExportResult) {
                            _state.value = ExportState.Done(outFile.absolutePath)
                            if (cont.isActive) cont.resumeWith(Result.success(outFile.absolutePath))
                        }

                        override fun onError(
                            composition: Composition,
                            result: ExportResult,
                            exception: ExportException,
                        ) {
                            _state.value = ExportState.Error(exception.message ?: "Ошибка экспорта")
                            if (cont.isActive) cont.resumeWith(Result.failure(exception))
                        }
                    })
                    .build()
                transformer.start(composition, outFile.absolutePath)
                cont.invokeOnCancellation { runCatching { transformer.cancel() } }
            }
        }
    }

    override fun share(mp4Path: String) {
        val file = File(mp4Path)
        val uri: Uri = FileProvider.getUriForFile(appContext, "${appContext.packageName}.storyprovider", file)
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "video/mp4"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(send, "Поделиться историей").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        appContext.startActivity(chooser)
    }
}

@UnstableApi
private class AnimatedCardOverlay(
    private val renderer: StoryCardRenderer,
    private val durationUs: Long,
    private val settings: OverlaySettings,
) : BitmapOverlay() {

    override fun getBitmap(presentationTimeUs: Long): Bitmap {
        val progress = if (durationUs > 0) presentationTimeUs.toFloat() / durationUs else 0f
        val timeMs = presentationTimeUs / 1000
        return renderer.render(progress = progress, timeMs = timeMs, isPlaying = timeMs >= 1000)
    }

    override fun getOverlaySettings(presentationTimeUs: Long): OverlaySettings = settings
}
