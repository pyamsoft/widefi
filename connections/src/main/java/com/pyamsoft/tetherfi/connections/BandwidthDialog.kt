/*
 * Copyright 2024 pyamsoft
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pyamsoft.tetherfi.connections

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.PopupProperties
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.pydroid.ui.app.rememberDialogProperties
import com.pyamsoft.pydroid.ui.defaults.TypographyDefaults
import com.pyamsoft.pydroid.ui.haptics.LocalHapticManager
import com.pyamsoft.tetherfi.core.Timber
import com.pyamsoft.tetherfi.server.clients.BandwidthLimit
import com.pyamsoft.tetherfi.server.clients.BandwidthUnit
import com.pyamsoft.tetherfi.server.clients.ByteTransferReport
import com.pyamsoft.tetherfi.server.clients.TetherClient
import java.time.Clock
import org.jetbrains.annotations.TestOnly

@Composable
internal fun BandwidthDialog(
    modifier: Modifier = Modifier,
    client: TetherClient,
    onDismiss: () -> Unit,
    onUpdateBandwidthLimit: (BandwidthLimit?) -> Unit,
) {
  // Don't let people pick bytes, who wants to limit bytes?
  val availableUnits = remember { BandwidthUnit.entries.filterNot { it == BandwidthUnit.BYTE } }

  // Initialize this to the current name
  // This way we can track changes quickly without needing to update the model
  val (enabled, setEnabled) = remember(client) { mutableStateOf(client.limit != null) }
  val (amount, setAmount) =
      remember(client) { mutableStateOf(client.limit?.amount?.toString().orEmpty()) }
  val (limitUnit, setLimitUnit) =
      remember(client) { mutableStateOf(client.limit?.unit ?: BandwidthUnit.KB) }

  val hapticManager = LocalHapticManager.current
  val amountValue =
      remember(amount) {
        amount.toULongOrNull().also {
          if (it == null) {
            Timber.d { "Invalid amount: $amount" }
          }
        }
      }
  val canSave =
      remember(amountValue, enabled) {
        if (!enabled) {
          return@remember true
        } else {
          return@remember amountValue != null
        }
      }

  val (showDropdown, setShowDropdown) = remember { mutableStateOf(false) }

  val handleDismissDropdown by rememberUpdatedState { setShowDropdown(false) }

  Dialog(
      properties = rememberDialogProperties(),
      onDismissRequest = onDismiss,
  ) {
    Card(
        modifier = modifier.padding(MaterialTheme.keylines.content),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.elevatedCardElevation(),
        colors = CardDefaults.elevatedCardColors(),
    ) {
      Column(
          modifier = Modifier.padding(MaterialTheme.keylines.content),
      ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
              modifier = Modifier.weight(1F),
              text = stringResource(R.string.bandwidth_label),
              style = MaterialTheme.typography.titleSmall,
              color =
                  if (enabled) {
                    MaterialTheme.colorScheme.onSurface
                  } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                  })

          Switch(
              checked = enabled,
              onCheckedChange = { newEnabled ->
                if (newEnabled) {
                  hapticManager?.toggleOn()
                } else {
                  hapticManager?.toggleOff()
                }
                setEnabled(newEnabled)
              },
          )
        }

        Row(
            modifier = Modifier.padding(top = MaterialTheme.keylines.content),
            verticalAlignment = Alignment.CenterVertically,
        ) {
          TextField(
              modifier = Modifier.weight(1F),
              value = amount,
              onValueChange = { setAmount(it) },
              enabled = enabled,
              keyboardOptions =
                  KeyboardOptions(
                      keyboardType = KeyboardType.Number,
                  ),
          )

          Column(
              modifier =
                  Modifier.padding(
                      start = MaterialTheme.keylines.baseline,
                  ),
          ) {
            Text(
                modifier =
                    Modifier.run {
                          if (!enabled) {
                            this
                          } else {
                            border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.primary,
                                shape = MaterialTheme.shapes.small,
                            )
                          }
                        }
                        .padding(
                            horizontal = MaterialTheme.keylines.content,
                            vertical = MaterialTheme.keylines.baseline,
                        )
                        .clickable(enabled = enabled) { setShowDropdown(true) },
                text = limitUnit.displayName,
                style = MaterialTheme.typography.bodySmall,
                color =
                    MaterialTheme.colorScheme.onSurfaceVariant.run {
                      if (enabled) {
                        this
                      } else {
                        copy(alpha = TypographyDefaults.ALPHA_DISABLED)
                      }
                    },
            )

            DropdownMenu(
                expanded = showDropdown,
                properties = remember { PopupProperties(focusable = true) },
                onDismissRequest = { handleDismissDropdown() },
            ) {
              availableUnits.forEach { u ->
                DropdownMenuItem(
                    onClick = {
                      hapticManager?.toggleOn()
                      setLimitUnit(u)
                      handleDismissDropdown()
                    },
                    text = {
                      val isSelected = remember(limitUnit, u) { u == limitUnit }
                      RadioButton(
                          enabled = enabled,
                          selected = isSelected,
                          onClick = {
                            hapticManager?.toggleOn()
                            setLimitUnit(u)
                            handleDismissDropdown()
                          },
                      )

                      Text(
                          text = u.displayName,
                          style = MaterialTheme.typography.bodySmall,
                          color =
                              MaterialTheme.colorScheme.onSurfaceVariant.run {
                                if (enabled) {
                                  this
                                } else {
                                  copy(alpha = TypographyDefaults.ALPHA_DISABLED)
                                }
                              },
                      )
                    },
                )
              }
            }
          }
        }

        Row(
            modifier = Modifier.padding(top = MaterialTheme.keylines.content),
            verticalAlignment = Alignment.CenterVertically,
        ) {
          Spacer(
              modifier = Modifier.weight(1F),
          )

          TextButton(
              onClick = onDismiss,
          ) {
            Text(
                text = stringResource(android.R.string.cancel),
            )
          }
          Button(
              modifier = Modifier.padding(start = MaterialTheme.keylines.baseline),
              enabled = canSave,
              onClick = {
                val limit =
                    if (enabled) {
                      amountValue?.let { v ->
                        BandwidthLimit(
                            amount = v,
                            unit = limitUnit,
                        )
                      }
                    } else {
                      null
                    }
                onUpdateBandwidthLimit(limit)
                onDismiss()
              },
          ) {
            Text(
                text = stringResource(android.R.string.ok),
            )
          }
        }
      }
    }
  }
}

@TestOnly
@Composable
private fun PreviewBandwidthDialog(limit: BandwidthLimit?) {
  BandwidthDialog(
      client =
          TetherClient.testCreate(
              hostNameOrIp = "127.0.0.1",
              clock = Clock.systemDefaultZone(),
              nickName = "",
              limit = limit,
              totalBytes = ByteTransferReport.EMPTY,
          ),
      onDismiss = {},
      onUpdateBandwidthLimit = {},
  )
}

@Preview
@Composable
private fun PreviewBandwidthDialogEmpty() {
  PreviewBandwidthDialog(
      limit = null,
  )
}

@Preview
@Composable
private fun PreviewBandwidthDialogName() {
  PreviewBandwidthDialog(
      limit =
          BandwidthLimit(
              10UL,
              BandwidthUnit.MB,
          ))
}
