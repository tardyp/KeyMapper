package io.github.sds100.keymapper.data.repository

import io.github.sds100.keymapper.data.db.dao.KeyMapDao
import io.github.sds100.keymapper.data.model.KeyMapEntity
import io.github.sds100.keymapper.domain.mappings.keymap.KeymapRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * Created by sds100 on 18/03/2021.
 */
class RoomKeymapRepository(
    private val dao: KeyMapDao,
    private val coroutineScope: CoroutineScope
) : KeymapRepository {
    //TODO implement automatic backing up stuff
    override val keymapList: Flow<List<KeyMapEntity>> = dao.getAllFlow()

    override fun insert(keymap: KeyMapEntity) {
        coroutineScope.launch {
            dao.insert(keymap)
        }
    }

    override fun update(keymap: KeyMapEntity) {
        coroutineScope.launch {
            dao.update(keymap)
        }
    }
}