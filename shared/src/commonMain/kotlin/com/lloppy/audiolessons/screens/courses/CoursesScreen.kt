package com.lloppy.audiolessons.screens.courses

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lloppy.audiolessons.library.LibraryState
import com.lloppy.audiolessons.screens.courses.components.CourseCard
import com.lloppy.audiolessons.screens.courses.components.EmptyState
import com.lloppy.audiolessons.ui.AppBackground
import com.lloppy.audiolessons.ui.CircleIconButton
import com.lloppy.audiolessons.ui.ScreenHeader
import io.github.vinceglb.filekit.dialogs.compose.rememberDirectoryPickerLauncher
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CoursesScreen(
    onOpenCourse: (String) -> Unit,
    onOpenSettings: () -> Unit,
) {
    val viewModel = koinViewModel<CoursesViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()

    val picker = rememberDirectoryPickerLauncher { directory ->
        viewModel.onAction(CoursesAction.FolderSelected(directory))
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is CoursesEvent.OpenLessons -> onOpenCourse(event.courseId)
                CoursesEvent.OpenSettings -> onOpenSettings()
            }
        }
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
                title = "Мои курсы",
                subtitle = "Аудиоуроки",
                action = {
                    CircleIconButton(onClick = { viewModel.onAction(CoursesAction.SettingsClicked) }) {
                        Icon(Icons.Filled.Settings, contentDescription = "Настройки")
                    }
                },
            )

            when (val content = state.content) {
                LibraryState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator()
                }

                LibraryState.NoRoot -> EmptyState(
                    title = "Добавьте курсы",
                    text = "Выберите папку, в которую вы складываете аудиоуроки",
                    buttonText = "Выбрать папку",
                    onClick = { picker.launch() },
                )

                is LibraryState.Error -> EmptyState(
                    title = "Не удалось открыть папку",
                    text = content.message,
                    buttonText = "Выбрать другую",
                    onClick = { picker.launch() },
                )

                is LibraryState.Ready -> {
                    val courses = content.library.courses
                    if (courses.isEmpty()) {
                        EmptyState(
                            title = "Пусто",
                            text = "В папке нет курсов с аудиоуроками",
                            buttonText = "Выбрать другую папку",
                            onClick = { picker.launch() },
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp),
                        ) {
                            items(courses, key = { it.id }) { course ->
                                CourseCard(
                                    course = course,
                                    onClick = { viewModel.onAction(CoursesAction.CourseClicked(course.id)) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
