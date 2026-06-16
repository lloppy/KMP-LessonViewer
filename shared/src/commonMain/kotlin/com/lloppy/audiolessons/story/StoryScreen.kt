package com.lloppy.audiolessons.story

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lloppy.audiolessons.story.components.PdfCropSelector
import com.lloppy.audiolessons.story.components.StoryFrame
import com.lloppy.audiolessons.ui.AppBackground
import com.lloppy.audiolessons.ui.ScreenHeader
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

private const val STORY_W = 1080f
private const val STORY_H = 1920f

@Composable
fun StoryScreen(
    lessonId: String,
    pageIndex: Int,
    onBack: () -> Unit,
) {
    val viewModel = koinViewModel<StoryViewModel> { parametersOf(lessonId, pageIndex) }
    val state by viewModel.state.collectAsStateWithLifecycle()

    Box(Modifier.fillMaxSize()) {
        AppBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeContentPadding()
                .padding(horizontal = 20.dp),
        ) {
            ScreenHeader(title = "История урока", onBack = onBack)

            Box(
                modifier = Modifier.fillMaxWidth().weight(1f).padding(vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                StoryFrame(
                    pageImage = state.pageImage,
                    title = state.lesson?.title ?: "Урок",
                    durationMs = state.displayDurationMs,
                    amplitude = state.amplitude,
                    attachment = state.croppedImage,
                    isPlaying = state.phase == StoryPhase.Recorded || state.phase == StoryPhase.Done,
                    modifier = Modifier
                        .aspectRatio(STORY_W / STORY_H)
                        .clip(RoundedCornerShape(20.dp)),
                )
            }

            if (state.pageImage != null) {
                TextButton(
                    onClick = { viewModel.onAction(StoryAction.StartCrop) },
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                ) {
                    Text(if (state.croppedImage == null) "✂ Выбрать фрагмент урока" else "✂ Изменить фрагмент")
                }
            }

            state.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(vertical = 4.dp))
            }

            Controls(
                state = state,
                onRecord = { viewModel.onAction(StoryAction.Record) },
                onStop = { viewModel.onAction(StoryAction.StopRecording) },
                onExport = { viewModel.onAction(StoryAction.Export) },
                onShare = { viewModel.onAction(StoryAction.Share) },
                onReset = { viewModel.onAction(StoryAction.Reset) },
            )
            Spacer(Modifier.height(24.dp))
        }

        val cropTarget = state.pageImage
        if (state.isCropping && cropTarget != null) {
            PdfCropSelector(
                image = cropTarget,
                onApply = { viewModel.onAction(StoryAction.ApplyCrop(it)) },
                onCancel = { viewModel.onAction(StoryAction.CancelCrop) },
            )
        }
    }
}

@Composable
private fun Controls(
    state: StoryState,
    onRecord: () -> Unit,
    onStop: () -> Unit,
    onExport: () -> Unit,
    onShare: () -> Unit,
    onReset: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        when (state.phase) {
            StoryPhase.Idle -> Button(onClick = onRecord, modifier = Modifier.fillMaxWidth()) {
                Text("● Записать голос")
            }

            StoryPhase.Recording -> Button(onClick = onStop, modifier = Modifier.fillMaxWidth()) {
                Text("■ Стоп · ${formatTimer(state.elapsedMs)}")
            }

            StoryPhase.Recorded -> Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(onClick = onReset, modifier = Modifier.weight(1f)) { Text("Заново") }
                Button(onClick = onExport, modifier = Modifier.weight(1f)) { Text("Создать видео") }
            }

            StoryPhase.Exporting -> Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
                Text("  Собираю видео…")
            }

            StoryPhase.Done -> Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(onClick = onReset, modifier = Modifier.weight(1f)) { Text("Заново") }
                Button(onClick = onShare, modifier = Modifier.weight(1f)) { Text("Поделиться") }
            }
        }
    }
}

private fun formatTimer(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    val ss = if (seconds < 10) "0$seconds" else "$seconds"
    return "$minutes:$ss"
}
