package com.lloppy.audiolessons.screens.lessons

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lloppy.audiolessons.screens.lessons.components.LessonCard
import com.lloppy.audiolessons.ui.AppBackground
import com.lloppy.audiolessons.ui.ScreenHeader
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun LessonsScreen(
    courseId: String,
    onOpenLesson: (String) -> Unit,
    onBack: () -> Unit,
) {
    val viewModel = koinViewModel<LessonsViewModel> { parametersOf(courseId) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val lessons = state.course?.lessons.orEmpty()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is LessonsEvent.OpenPlayer -> onOpenLesson(event.lessonId)
                LessonsEvent.NavigateBack -> onBack()
            }
        }
    }

    val listState = rememberLazyListState()
    LaunchedEffect(lessons, state.lastLessonId) {
        val index = lessons.indexOfFirst { it.id == state.lastLessonId }
        if (index >= 0) listState.scrollToItem(index)
    }

    Box(Modifier.fillMaxSize()) {
        AppBackground()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeContentPadding()
                .padding(horizontal = 20.dp),
        ) {
            ScreenHeader(
                title = state.course?.title ?: "Курс",
                subtitle = "${lessons.size} уроков",
                onBack = { viewModel.onAction(LessonsAction.BackClicked) },
            )
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp),
            ) {
                itemsIndexed(lessons, key = { _, l -> l.id }) { index, lesson ->
                    LessonCard(
                        number = index + 1,
                        lesson = lesson,
                        fraction = state.progress[lesson.id]?.fraction ?: 0f,
                        isLast = lesson.id == state.lastLessonId,
                        onClick = { viewModel.onAction(LessonsAction.LessonClicked(lesson.id)) },
                    )
                }
            }
        }
    }
}
