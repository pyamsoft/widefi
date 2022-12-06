package com.pyamsoft.tetherfi.status

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.pyamsoft.pydroid.theme.ZeroSize
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.tetherfi.server.ServerNetworkBand
import com.pyamsoft.tetherfi.ui.MaterialCheckable
import com.pyamsoft.tetherfi.ui.rememberMaterialCheckableHeightMatcherGenerator

@Composable
internal fun NetworkBands(
    modifier: Modifier = Modifier,
    isEditable: Boolean,
    band: ServerNetworkBand?,
    onSelectBand: (ServerNetworkBand) -> Unit,
) {
  val bands = remember { ServerNetworkBand.values() }
  val bandIterator = remember(bands) { bands.withIndex() }

  val generator = rememberMaterialCheckableHeightMatcherGenerator<ServerNetworkBand>()

  Column(
      modifier = modifier,
  ) {

    // Small label above
    Text(
        modifier = Modifier.fillMaxWidth().padding(bottom = MaterialTheme.keylines.baseline),
        text = "Broadcast Frequency",
        style =
            MaterialTheme.typography.caption.copy(
                fontWeight = FontWeight.W700,
                color =
                    MaterialTheme.colors.onBackground.copy(
                        alpha = ContentAlpha.medium,
                    ),
            ),
    )

    // Then the buttons
    Row {
      for ((index, b) in bandIterator) {
        val isSelected = remember(b, band) { b == band }
        val heightMatcher = generator.generateFor(b)

        MaterialCheckable(
            modifier =
                Modifier.weight(1F)
                    .then(heightMatcher.onSizeChangedModifier)
                    .padding(
                        end =
                            if (index < bands.lastIndex) MaterialTheme.keylines.content
                            else ZeroSize,
                    ),
            isEditable = isEditable,
            condition = isSelected,
            title = b.displayName,
            description = b.description,
            extraHeight = heightMatcher.extraHeight,
            onClick = { onSelectBand(b) },
        )
      }
    }
  }
}
