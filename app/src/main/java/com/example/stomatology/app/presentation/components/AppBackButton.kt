package com.example.stomatology.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.stomatology.app.R

@Composable
fun AppBackButton(
    onClick: () -> Unit,
    onPrimary: Boolean = false,
    minimal: Boolean = false,
    modifier: Modifier = Modifier
) {
    if (minimal) {
        val tint = if (onPrimary) {
            Color.White
        } else {
            MaterialTheme.colorScheme.onSurface
        }
        val backLabel = stringResource(R.string.common_back)
        IconButton(onClick = onClick, modifier = modifier) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = backLabel,
                tint = tint
            )
        }
        return
    }

    val containerColor = if (onPrimary) {
        Color.White.copy(alpha = 0.24f)
    } else {
        Color(0xFFF1F3F5)
    }
    val iconColor = if (onPrimary) Color.White else Color(0xFF111827)
    val backLabel = stringResource(R.string.common_back)

    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(containerColor)
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = backLabel,
            tint = iconColor
        )
    }
}
