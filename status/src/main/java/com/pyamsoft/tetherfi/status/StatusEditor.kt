package com.pyamsoft.tetherfi.status

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.VisualTransformation

@Composable
internal fun StatusEditor(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    title: String,
    value: String,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    onChange: (String) -> Unit,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
) {
  val textStyle = LocalTextStyle.current

  Column(
      modifier = modifier,
  ) {
    TextField(
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled,
        keyboardOptions = keyboardOptions,
        value = value,
        textStyle =
            textStyle.copy(
                fontFamily = FontFamily.Monospace,
            ),
        visualTransformation = visualTransformation,
        onValueChange = onChange,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        singleLine = true,
        label = {
          Text(
              text = title,
          )
        },
    )
  }
}
