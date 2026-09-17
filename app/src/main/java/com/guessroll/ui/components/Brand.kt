package com.guessroll.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.guessroll.R
import com.guessroll.ui.theme.Amber
import com.guessroll.ui.theme.PanelDeep
import com.guessroll.ui.theme.Violet

@Composable
fun GuessRollLogoMark(
    modifier: Modifier = Modifier,
    size: Dp = 34.dp,
    contentDescription: String? = "GuessRoll",
) {
    val shape = RoundedCornerShape(size * 0.38f)
    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(
                        Amber.copy(alpha = 0.10f),
                        Violet.copy(alpha = 0.30f),
                        PanelDeep.copy(alpha = 0.98f),
                    ),
                ),
            )
            .border(
                BorderStroke(1.dp, Amber.copy(alpha = 0.16f)),
                shape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_guessroll_mark),
            contentDescription = contentDescription,
            modifier = Modifier.size(size * 0.78f),
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(shape)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Amber.copy(alpha = 0.04f),
                            Color.White.copy(alpha = 0.022f),
                            Color.Transparent,
                        ),
                    ),
                ),
        )
    }
}
