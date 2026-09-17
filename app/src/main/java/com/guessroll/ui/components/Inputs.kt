package com.guessroll.ui.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.guessroll.ui.theme.Amber
import com.guessroll.ui.theme.Danger
import com.guessroll.ui.theme.GlassSoft
import com.guessroll.ui.theme.GlassStrong
import com.guessroll.ui.theme.PanelStroke
import com.guessroll.ui.theme.TextDisabled
import com.guessroll.ui.theme.TextMuted
import com.guessroll.ui.theme.TextPrimary

@Composable
fun PartyTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions(
        capitalization = KeyboardCapitalization.Words,
    ),
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
            )
        },
        singleLine = singleLine,
        keyboardOptions = keyboardOptions,
        textStyle = MaterialTheme.typography.titleMedium.copy(
            color = TextPrimary,
            fontWeight = FontWeight.SemiBold,
        ),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            focusedBorderColor = Amber,
            unfocusedBorderColor = PanelStroke,
            focusedLabelColor = Amber,
            unfocusedLabelColor = TextMuted,
            cursorColor = Amber,
            focusedContainerColor = GlassStrong,
            unfocusedContainerColor = GlassSoft.copy(alpha = 0.88f),
            disabledContainerColor = Color.White.copy(alpha = 0.045f),
            disabledTextColor = TextDisabled,
            errorBorderColor = Danger,
            errorLabelColor = Danger,
        ),
        modifier = modifier,
    )
}
