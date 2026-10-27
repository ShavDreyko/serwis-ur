package com.example.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Priority
import com.example.data.model.TaskStatus
import com.example.data.model.TaskType
import com.example.ui.theme.LocalIsDarkTheme

@Composable
fun PriorityBadge(priority: Priority, modifier: Modifier = Modifier) {
    val isDark = LocalIsDarkTheme.current
    val color = priority.getColor(isDark = isDark)
    val isCritical = priority == Priority.KRYTYCZNY

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .background(color.copy(alpha = if (isDark) 0.15f else 0.12f), RoundedCornerShape(6.dp))
            .border(1.dp, color.copy(alpha = if (isCritical) alpha else 0.5f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        if (isCritical) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .alpha(alpha)
                    .background(color, CircleShape)
            )
            Spacer(modifier = Modifier.width(5.dp))
        }
        Icon(
            imageVector = if (isCritical) Icons.Default.Error else Icons.Default.Warning,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(13.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = priority.label,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}

@Composable
fun StatusBadge(status: TaskStatus, modifier: Modifier = Modifier) {
    val isDark = LocalIsDarkTheme.current
    val color = status.getColor(isDark = isDark)
    val icon = when (status) {
        TaskStatus.NOWE -> Icons.Default.HourglassTop
        TaskStatus.W_TRAKCIE -> Icons.Default.PlayArrow
        TaskStatus.WSTRZYMANE -> Icons.Default.Warning
        TaskStatus.ZAKONCZONE -> Icons.Default.CheckCircle
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .background(color.copy(alpha = if (isDark) 0.15f else 0.12f), RoundedCornerShape(6.dp))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(13.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = status.label,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
    }
}

@Composable
fun TypeBadge(type: TaskType, modifier: Modifier = Modifier) {
    val isDark = LocalIsDarkTheme.current
    val color = when (type) {
        TaskType.AWARIA -> if (isDark) Color(0xFFF87171) else Color(0xFFDC2626)
        TaskType.PRZEGLAD -> if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)
        TaskType.KONSERWACJA -> if (isDark) Color(0xFFFBBF24) else Color(0xFFD97706)
        TaskType.MODYFIKACJA -> if (isDark) Color(0xFFA78BFA) else Color(0xFF7C3AED)
        TaskType.NADGODZINY -> if (isDark) Color(0xFFFB923C) else Color(0xFFEA580C)
    }

    Box(
        modifier = modifier
            .background(color.copy(alpha = if (isDark) 0.12f else 0.10f), RoundedCornerShape(4.dp))
            .border(0.8.dp, color.copy(alpha = 0.35f), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 3.dp)
    ) {
        Text(
            text = type.label,
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )
    }
}
