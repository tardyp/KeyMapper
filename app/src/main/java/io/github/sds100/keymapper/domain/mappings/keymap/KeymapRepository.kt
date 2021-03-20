package io.github.sds100.keymapper.domain.mappings.keymap

import io.github.sds100.keymapper.data.model.KeyMapEntity
import kotlinx.coroutines.flow.Flow

/**
 * Created by sds100 on 18/03/2021.
 */
interface KeymapRepository {
    val keymapList: Flow<List<KeyMapEntity>>

    fun insert(keymap: KeyMapEntity)
    fun update(keymap: KeyMapEntity)
}