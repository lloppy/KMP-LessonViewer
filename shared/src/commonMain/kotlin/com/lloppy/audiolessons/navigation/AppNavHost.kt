package com.lloppy.audiolessons.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.lloppy.audiolessons.screens.courses.CoursesScreen
import com.lloppy.audiolessons.screens.lessons.LessonsScreen
import com.lloppy.audiolessons.screens.player.PlayerScreen
import com.lloppy.audiolessons.screens.settings.SettingsScreen
import com.lloppy.audiolessons.story.StoryScreen

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = CoursesDestination) {
        composable<CoursesDestination> {
            CoursesScreen(
                onOpenCourse = { courseId -> navController.navigate(LessonsDestination(courseId)) },
                onOpenSettings = { navController.navigate(SettingsDestination) },
            )
        }
        composable<LessonsDestination> { backStackEntry ->
            LessonsScreen(
                courseId = backStackEntry.toRoute<LessonsDestination>().courseId,
                onOpenLesson = { lessonId -> navController.navigate(PlayerDestination(lessonId)) },
                onBack = { navController.popBackStack() },
            )
        }
        composable<PlayerDestination> { backStackEntry ->
            PlayerScreen(
                lessonId = backStackEntry.toRoute<PlayerDestination>().lessonId,
                onBack = { navController.popBackStack() },
                onOpenStory = { lessonId, pageIndex ->
                    navController.navigate(StoryDestination(lessonId, pageIndex))
                },
            )
        }
        composable<StoryDestination> { backStackEntry ->
            val args = backStackEntry.toRoute<StoryDestination>()
            StoryScreen(
                lessonId = args.lessonId,
                pageIndex = args.pageIndex,
                onBack = { navController.popBackStack() },
            )
        }
        composable<SettingsDestination> {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
