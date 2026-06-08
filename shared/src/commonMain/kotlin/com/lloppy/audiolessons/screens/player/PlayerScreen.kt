package com.lloppy.audiolessons.screens.player

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lloppy.audiolessons.pdf.PdfViewer
import com.lloppy.audiolessons.screens.player.components.PlayerBar
import com.lloppy.audiolessons.ui.AppBackground
import com.lloppy.audiolessons.ui.ScreenHeader
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun PlayerScreen(
    lessonId: String,
    onBack: () -> Unit,
) {
    val viewModel = koinViewModel<PlayerViewModel> { parametersOf(lessonId) }
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(lessonId) { viewModel.onAction(PlayerAction.Start) }
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                PlayerEvent.NavigateBack -> onBack()
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        AppBackground()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeContentPadding()
                .padding(horizontal = 16.dp),
        ) {
            ScreenHeader(
                title = state.lesson?.title ?: "Урок",
                onBack = { viewModel.onAction(PlayerAction.BackClicked) },
            )

            val pdf = state.lesson?.pdf
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f).padding(vertical = 4.dp),
                contentAlignment = Alignment.Center,
            ) {
                if (pdf == null) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🎧", fontSize = 56.sp)
                        Spacer(Modifier.height(12.dp))
                        Text("Текстовой версии нет", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 2.dp,
                    ) {
                        PdfViewer(
                            file = pdf.file,
                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(20.dp)),
                        )
                    }
                }
            }

            PlayerBar(
                isPlaying = state.playback.isPlaying,
                isBuffering = state.playback.isBuffering,
                positionMs = state.playback.positionMs,
                durationMs = state.playback.durationMs,
                onPlayPause = { viewModel.onAction(PlayerAction.PlayPause) },
                onSeek = { viewModel.onAction(PlayerAction.Seek(it)) },
            )
        }
    }
}
