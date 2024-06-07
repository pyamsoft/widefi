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

import com.pyamsoft.pydroid.arch.AbstractViewModeler
import com.pyamsoft.tetherfi.core.Timber
import com.pyamsoft.tetherfi.server.clients.AllowedClients
import com.pyamsoft.tetherfi.server.clients.BandwidthLimit
import com.pyamsoft.tetherfi.server.clients.BlockedClientTracker
import com.pyamsoft.tetherfi.server.clients.BlockedClients
import com.pyamsoft.tetherfi.server.clients.ClientEditor
import com.pyamsoft.tetherfi.server.clients.TetherClient
import com.pyamsoft.tetherfi.server.clients.key
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ConnectionViewModel
@Inject
internal constructor(
    override val state: MutableConnectionViewState,
    private val allowedClients: AllowedClients,
    private val blockedClients: BlockedClients,
    private val blockTracker: BlockedClientTracker,
    private val clientEditor: ClientEditor,
) : ConnectionViewState by state, AbstractViewModeler<ConnectionViewState>(state) {

  fun bind(scope: CoroutineScope) {
    scope.launch(context = Dispatchers.Default) {
      allowedClients.listenForClients().collect { clients ->
        val list = clients.toList().sortedBy { it.key() }
        state.connections.value = list
      }
    }

    scope.launch(context = Dispatchers.Default) {
      blockedClients.listenForBlocked().collect { state.blocked.value = it }
    }
  }

  fun handleToggleBlock(client: TetherClient) {
    if (blockedClients.isBlocked(client)) {
      blockTracker.unblock(client)
    } else {
      blockTracker.block(client)
    }
  }

  fun handleOpenManageNickName(client: TetherClient) {
    state.managingNickName.value = client
  }

  fun handleCloseManageNickName() {
    state.managingNickName.value = null
  }

  fun handleOpenManageBandwidthLimit(client: TetherClient) {
    state.managingBandwidthLimit.value = client
  }

  fun handleCloseManageBandwidthLimit() {
    state.managingBandwidthLimit.value = null
  }

  fun handleUpdateNickName(scope: CoroutineScope, nickName: String) {
    val client = state.managingNickName.value
    if (client == null) {
      Timber.w { "Cannot update nick name, no client" }
      return
    }

    scope.launch(context = Dispatchers.Default) {
      Timber.d { "Update client nickName: $client $nickName" }
      clientEditor.updateNickName(client, nickName)
    }
  }

  fun handleUpdateBandwidthLimit(scope: CoroutineScope, limit: BandwidthLimit?) {
    val client = state.managingBandwidthLimit.value
    if (client == null) {
      Timber.w { "Cannot update limit, no client" }
      return
    }

    scope.launch(context = Dispatchers.Default) {
      Timber.d { "Update client limit: $client $limit" }
      clientEditor.updateBandwidthLimit(client, limit)
    }
  }
}
