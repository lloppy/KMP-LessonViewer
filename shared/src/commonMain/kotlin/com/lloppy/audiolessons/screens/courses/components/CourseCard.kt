package com.lloppy.audiolessons.screens.courses.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lloppy.audiolessons.library.model.Course
import com.lloppy.audiolessons.ui.LetterAvatar
import com.lloppy.audiolessons.ui.SectionCard
import com.lloppy.audiolessons.ui.avatarColor

@Composable
fun CourseCard(course: Course, onClick: () -> Unit) {
    SectionCard(Modifier.fillMaxWidth(), onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LetterAvatar(course.title, avatarColor(course.title))
            Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
                Text(
                    course.title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    "${course.lessons.size} ${lessonsWord(course.lessons.size)}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text("›", fontSize = 24.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private fun lessonsWord(n: Int): String {
    val mod10 = n % 10
    val mod100 = n % 100
    return when {
        mod10 == 1 && mod100 != 11 -> "урок"
        mod10 in 2..4 && mod100 !in 12..14 -> "урока"
        else -> "уроков"
    }
}
