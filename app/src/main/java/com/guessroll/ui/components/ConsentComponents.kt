package com.guessroll.ui.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.guessroll.ui.theme.Amber

@Composable
fun ConsentGlassSheet(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    GlassCard(
        modifier = modifier.semantics {
            contentDescription = "Согласие на случайный выбор фото"
        },
        accent = Amber,
        featured = true,
        blurred = true,
        dense = true,
        content = content,
    )
}
