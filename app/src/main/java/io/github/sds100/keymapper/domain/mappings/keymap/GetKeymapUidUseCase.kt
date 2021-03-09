package io.github.sds100.keymapper.domain.mappings.keymap

import kotlinx.coroutines.flow.StateFlow

/**
 * Created by sds100 on 01/03/2021.
 */

interface GetKeymapUidUseCase{
    val uid: StateFlow<String>
}