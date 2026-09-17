package com.guessroll.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import com.guessroll.ui.theme.Sky
import com.guessroll.ui.theme.TextMuted
import com.guessroll.ui.theme.TextPrimary

@Composable
fun EmptyStateCard(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    GlassCard(
        modifier = modifier.semantics(mergeDescendants = true) {
            contentDescription = "$title. $subtitle"
        },
        accent = Sky,
    ) {
        Text(
            text = title,
            color = TextPrimary,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = subtitle,
            color = TextMuted,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
