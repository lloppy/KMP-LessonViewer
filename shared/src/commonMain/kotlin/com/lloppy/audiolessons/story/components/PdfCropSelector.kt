package com.lloppy.audiolessons.story.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun PdfCropSelector(
    image: ImageBitmap,
    onApply: (ImageBitmap) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val handle = with(density) { 28.dp.toPx() }
    val minSize = with(density) { 64.dp.toPx() }

    Box(modifier.fillMaxSize().background(Color(0xCC000000))) {
        BoxWithConstraints(
            Modifier.fillMaxSize().padding(16.dp),
        ) {
            val cw = with(density) { maxWidth.toPx() }
            val ch = with(density) { maxHeight.toPx() }
            val imgAspect = image.width.toFloat() / image.height.toFloat()

            var dispW = cw
            var dispH = cw / imgAspect
            if (dispH > ch) {
                dispH = ch
                dispW = ch * imgAspect
            }
            val offX = (cw - dispW) / 2f
            val offY = (ch - dispH) / 2f

            var left by remember(image) { mutableStateOf(0f) }
            var top by remember(image) { mutableStateOf(0f) }
            var right by remember(image) { mutableStateOf(0f) }
            var bottom by remember(image) { mutableStateOf(0f) }

            LaunchedEffect(dispW, dispH, offX, offY) {
                left = offX + dispW * 0.1f
                right = offX + dispW * 0.9f
                top = offY + dispH * 0.1f
                bottom = offY + dispH * 0.9f
            }

            Box(
                Modifier
                    .offset { IntOffset(offX.roundToInt(), offY.roundToInt()) }
                    .size(with(density) { dispW.toDp() }, with(density) { dispH.toDp() }),
            ) {
                Image(
                    bitmap = image,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds,
                )
            }

            androidx.compose.foundation.Canvas(Modifier.fillMaxSize()) {
                val scrim = Color(0x99000000)
                drawRect(scrim, topLeft = Offset(0f, 0f), size = Size(size.width, top))
                drawRect(scrim, topLeft = Offset(0f, bottom), size = Size(size.width, size.height - bottom))
                drawRect(scrim, topLeft = Offset(0f, top), size = Size(left, bottom - top))
                drawRect(scrim, topLeft = Offset(right, top), size = Size(size.width - right, bottom - top))
                drawRect(
                    color = Color.White,
                    topLeft = Offset(left, top),
                    size = Size(right - left, bottom - top),
                    style = Stroke(width = with(density) { 2.dp.toPx() }),
                )
            }

            Box(
                Modifier
                    .offset { IntOffset(left.roundToInt(), top.roundToInt()) }
                    .size(with(density) { (right - left).toDp() }, with(density) { (bottom - top).toDp() })
                    .pointerInput(dispW, dispH) {
                        detectDragGestures { _, drag ->
                            val w = right - left
                            val h = bottom - top
                            val nl = (left + drag.x).coerceIn(offX, offX + dispW - w)
                            val nt = (top + drag.y).coerceIn(offY, offY + dispH - h)
                            val sx = nl - left
                            val sy = nt - top
                            left += sx; right += sx
                            top += sy; bottom += sy
                        }
                    },
            )

            CornerHandle(left, top, handle, dispW, dispH) { dx, dy ->
                left = (left + dx).coerceIn(offX, right - minSize)
                top = (top + dy).coerceIn(offY, bottom - minSize)
            }
            CornerHandle(right - handle, top, handle, dispW, dispH) { dx, dy ->
                right = (right + dx).coerceIn(left + minSize, offX + dispW)
                top = (top + dy).coerceIn(offY, bottom - minSize)
            }
            CornerHandle(left, bottom - handle, handle, dispW, dispH) { dx, dy ->
                left = (left + dx).coerceIn(offX, right - minSize)
                bottom = (bottom + dy).coerceIn(top + minSize, offY + dispH)
            }
            CornerHandle(right - handle, bottom - handle, handle, dispW, dispH) { dx, dy ->
                right = (right + dx).coerceIn(left + minSize, offX + dispW)
                bottom = (bottom + dy).coerceIn(top + minSize, offY + dispH)
            }

            ApplyBar(
                onCancel = onCancel,
                onApply = {
                    val sl = (((left - offX) / dispW) * image.width).roundToInt().coerceIn(0, image.width - 1)
                    val st = (((top - offY) / dispH) * image.height).roundToInt().coerceIn(0, image.height - 1)
                    val sw = (((right - left) / dispW) * image.width).roundToInt().coerceIn(1, image.width - sl)
                    val sh = (((bottom - top) / dispH) * image.height).roundToInt().coerceIn(1, image.height - st)
                    onApply(cropImageBitmap(image, sl, st, sw, sh))
                },
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        }
    }
}

@Composable
private fun CornerHandle(
    x: Float,
    y: Float,
    handle: Float,
    dispW: Float,
    dispH: Float,
    onDrag: (dx: Float, dy: Float) -> Unit,
) {
    val density = LocalDensity.current
    Box(
        Modifier
            .offset { IntOffset(x.roundToInt(), y.roundToInt()) }
            .size(with(density) { handle.toDp() })
            .pointerInput(dispW, dispH) {
                detectDragGestures { _, drag -> onDrag(drag.x, drag.y) }
            }
            .padding(6.dp)
            .background(Color.White, RoundedCornerShape(50)),
    )
}

@Composable
private fun ApplyBar(
    onCancel: () -> Unit,
    onApply: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) { Text("Отмена") }
        Button(onClick = onApply, modifier = Modifier.weight(1f)) { Text("Применить") }
    }
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
