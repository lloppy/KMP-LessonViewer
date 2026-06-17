package com.lloppy.audiolessons.screens.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import com.lloppy.audiolessons.pdf.PdfViewer
import com.lloppy.audiolessons.screens.player.components.PlayerBar
import com.lloppy.audiolessons.story.di.isStoryAvailable
import com.lloppy.audiolessons.ui.AppBackground
import com.lloppy.audiolessons.ui.CircleIconButton
import com.lloppy.audiolessons.ui.ScreenHeader
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.math.roundToInt

private const val SKIP_MS = 10_000L
private const val ZOOM_STEP = 0.1f
private const val ZOOM_MIN = 1f
private const val ZOOM_MAX = 3f

@Composable
fun PlayerScreen(
    lessonId: String,
    onBack: () -> Unit,
    onOpenStory: (lessonId: String, pageIndex: Int) -> Unit = { _, _ -> },
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
                action = if (isStoryAvailable) {
                    {
                        CircleIconButton(
                            onClick = { onOpenStory(state.lesson?.id ?: lessonId, 0) },
                            container = MaterialTheme.colorScheme.surface,
                        ) {
                            Text("🎤", fontSize = 18.sp)
                        }
                    }
                } else null,
            )

            val pdf = state.lesson?.pdf
            var zoom by remember(pdf?.id) { mutableStateOf(1f) }
            var zoomVisible by remember(pdf?.id) { mutableStateOf(false) }
            val interaction = remember { mutableStateOf(0) }
            val scrollConnection = remember {
                object : NestedScrollConnection {
                    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                        if (available.y != 0f) interaction.value++
                        return Offset.Zero
                    }
                }
            }
            LaunchedEffect(interaction.value) {
                if (interaction.value == 0) return@LaunchedEffect
                zoomVisible = true
                delay(700)
                zoomVisible = false
            }
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
                        modifier = Modifier.fillMaxSize().nestedScroll(scrollConnection),
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 2.dp,
                    ) {
                        PdfViewer(
                            file = pdf.file,
                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(20.dp)),
                            zoom = zoom,
                        )
                    }
                    PdfZoomOverlay(
                        visible = zoomVisible,
                        zoom = zoom,
                        onZoomIn = {
                            zoom = (zoom + ZOOM_STEP).coerceAtMost(ZOOM_MAX)
                            interaction.value++
                        },
                        onZoomOut = {
                            zoom = (zoom - ZOOM_STEP).coerceAtLeast(ZOOM_MIN)
                            interaction.value++
                        },
                    )
                }
            }

            PlayerBar(
                isPlaying = state.playback.isPlaying,
                isBuffering = state.playback.isBuffering,
                positionMs = state.playback.positionMs,
                durationMs = state.playback.durationMs,
                hasPrev = state.hasPrev,
                hasNext = state.hasNext,
                onPlayPause = { viewModel.onAction(PlayerAction.PlayPause) },
                onSeek = { viewModel.onAction(PlayerAction.Seek(it)) },
                onSkipBack = { viewModel.onAction(PlayerAction.Skip(-SKIP_MS)) },
                onSkipForward = { viewModel.onAction(PlayerAction.Skip(SKIP_MS)) },
                onPrevLesson = { viewModel.onAction(PlayerAction.PrevLesson) },
                onNextLesson = { viewModel.onAction(PlayerAction.NextLesson) },
            )
        }
    }
}

@Composable
private fun BoxScope.PdfZoomOverlay(
    visible: Boolean,
    zoom: Float,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(12.dp),
    ) {
        ZoomControls(zoom = zoom, onZoomIn = onZoomIn, onZoomOut = onZoomOut)
    }
}

@Composable
private fun ZoomControls(
    zoom: Float,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        CircleIconButton(onClick = onZoomIn, size = 40.dp) {
            Text("+", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
        ) {
            Text(
                "${(zoom * 100).roundToInt()}%",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            )
        }
        CircleIconButton(onClick = onZoomOut, size = 40.dp) {
            Text("−", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}
