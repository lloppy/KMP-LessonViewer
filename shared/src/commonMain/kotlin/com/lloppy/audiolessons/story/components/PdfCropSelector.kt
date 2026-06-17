package com.lloppy.audiolessons.story.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlin.math.min
import kotlin.math.roundToInt

@Composable
fun PdfCropSelector(
    image: ImageBitmap,
    onApply: (ImageBitmap) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val handle = with(density) { 30.dp.toPx() }
    val imgW = image.width.toFloat()
    val imgH = image.height.toFloat()
    val minImg = 0.06f * min(imgW, imgH)

    Box(modifier.fillMaxSize().background(Color(0xCC000000))) {
        BoxWithConstraints(Modifier.fillMaxSize().padding(16.dp)) {
            val cw = with(density) { maxWidth.toPx() }
            val ch = with(density) { maxHeight.toPx() }
            val baseScale = min(cw / imgW, ch / imgH)

            var zoom by remember(image) { mutableFloatStateOf(1f) }
            var offset by remember(image) { mutableStateOf(Offset.Zero) }

            var iLeft by remember(image) { mutableFloatStateOf(imgW * 0.1f) }
            var iTop by remember(image) { mutableFloatStateOf(imgH * 0.1f) }
            var iRight by remember(image) { mutableFloatStateOf(imgW * 0.9f) }
            var iBottom by remember(image) { mutableFloatStateOf(imgH * 0.9f) }

            val s = baseScale * zoom

            fun toScreen(ix: Float, iy: Float): Offset = Offset(
                cw / 2f + offset.x + (ix - imgW / 2f) * s,
                ch / 2f + offset.y + (iy - imgH / 2f) * s,
            )

            // Слой жестов: pinch-zoom + панорамирование документа.
            Box(
                Modifier.matchParentSize().pointerInput(image) {
                    detectTransformGestures { _, pan, gestureZoom, _ ->
                        zoom = (zoom * gestureZoom).coerceIn(1f, 8f)
                        val es = baseScale * zoom
                        val maxX = imgW * es / 2f
                        val maxY = imgH * es / 2f
                        offset = Offset(
                            (offset.x + pan.x).coerceIn(-maxX, maxX),
                            (offset.y + pan.y).coerceIn(-maxY, maxY),
                        )
                    }
                },
            )

            // Отрисовка: документ + затемнение вне рамки + граница.
            androidx.compose.foundation.Canvas(Modifier.matchParentSize()) {
                val tl = toScreen(0f, 0f)
                val br = toScreen(imgW, imgH)
                drawImage(
                    image = image,
                    dstOffset = IntOffset(tl.x.roundToInt(), tl.y.roundToInt()),
                    dstSize = IntSize((br.x - tl.x).roundToInt(), (br.y - tl.y).roundToInt()),
                )
                val cl = toScreen(iLeft, iTop)
                val cr = toScreen(iRight, iBottom)
                val scrim = Color(0x99000000)
                drawRect(scrim, Offset(0f, 0f), Size(size.width, cl.y))
                drawRect(scrim, Offset(0f, cr.y), Size(size.width, size.height - cr.y))
                drawRect(scrim, Offset(0f, cl.y), Size(cl.x, cr.y - cl.y))
                drawRect(scrim, Offset(cr.x, cl.y), Size(size.width - cr.x, cr.y - cl.y))
                drawRect(
                    color = Color.White,
                    topLeft = Offset(cl.x, cl.y),
                    size = Size(cr.x - cl.x, cr.y - cl.y),
                    style = Stroke(width = with(density) { 2.dp.toPx() }),
                )
            }

            // Перемещение рамки (в координатах изображения).
            val cl = toScreen(iLeft, iTop)
            val cr = toScreen(iRight, iBottom)
            Box(
                Modifier
                    .offset { IntOffset(cl.x.roundToInt(), cl.y.roundToInt()) }
                    .size(with(density) { (cr.x - cl.x).toDp() }, with(density) { (cr.y - cl.y).toDp() })
                    .pointerInput(image) {
                        detectDragGestures { _, drag ->
                            val dx = drag.x / s
                            val dy = drag.y / s
                            val w = iRight - iLeft
                            val h = iBottom - iTop
                            val nl = (iLeft + dx).coerceIn(0f, imgW - w)
                            val nt = (iTop + dy).coerceIn(0f, imgH - h)
                            val sx = nl - iLeft
                            val sy = nt - iTop
                            iLeft += sx; iRight += sx
                            iTop += sy; iBottom += sy
                        }
                    },
            )

            CropHandle(cl.x, cl.y, handle) { dx, dy ->
                iLeft = (iLeft + dx / s).coerceIn(0f, iRight - minImg)
                iTop = (iTop + dy / s).coerceIn(0f, iBottom - minImg)
            }
            CropHandle(cr.x, cl.y, handle) { dx, dy ->
                iRight = (iRight + dx / s).coerceIn(iLeft + minImg, imgW)
                iTop = (iTop + dy / s).coerceIn(0f, iBottom - minImg)
            }
            CropHandle(cl.x, cr.y, handle) { dx, dy ->
                iLeft = (iLeft + dx / s).coerceIn(0f, iRight - minImg)
                iBottom = (iBottom + dy / s).coerceIn(iTop + minImg, imgH)
            }
            CropHandle(cr.x, cr.y, handle) { dx, dy ->
                iRight = (iRight + dx / s).coerceIn(iLeft + minImg, imgW)
                iBottom = (iBottom + dy / s).coerceIn(iTop + minImg, imgH)
            }

            Row(
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) { Text("Отмена") }
                Button(
                    onClick = {
                        val l = iLeft.roundToInt().coerceIn(0, image.width - 1)
                        val t = iTop.roundToInt().coerceIn(0, image.height - 1)
                        val w = (iRight - iLeft).roundToInt().coerceIn(1, image.width - l)
                        val h = (iBottom - iTop).roundToInt().coerceIn(1, image.height - t)
                        onApply(cropImageBitmap(image, l, t, w, h))
                    },
                    modifier = Modifier.weight(1f),
                ) { Text("Применить") }
            }
        }
    }
}

@Composable
private fun CropHandle(
    cx: Float,
    cy: Float,
    handle: Float,
    onDrag: (dx: Float, dy: Float) -> Unit,
) {
    val density = LocalDensity.current
    Box(
        Modifier
            .offset { IntOffset((cx - handle / 2f).roundToInt(), (cy - handle / 2f).roundToInt()) }
            .size(with(density) { handle.toDp() })
            .pointerInput(Unit) {
                detectDragGestures { _, drag -> onDrag(drag.x, drag.y) }
            }
            .padding(7.dp)
            .background(Color.White, RoundedCornerShape(50)),
    )
}

fun cropImageBitmap(src: ImageBitmap, left: Int, top: Int, width: Int, height: Int): ImageBitmap {
    val out = ImageBitmap(width, height)
    val canvas = Canvas(out)
    canvas.drawImageRect(
        image = src,
        srcOffset = IntOffset(left, top),
        srcSize = IntSize(width, height),
        dstOffset = IntOffset(0, 0),
        dstSize = IntSize(width, height),
        paint = Paint(),
    )
    return out
}
