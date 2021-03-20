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

    override suspend fun get(id: Long): KeyMapEntity {
        return dao.getById(id)
    }

    override fun delete(vararg id: Long) {
        coroutineScope.launch {
            dao.deleteById(*id)
        }
    }

    override fun duplicate(vararg id: Long) {
        coroutineScope.launch {
            val keymaps = mutableListOf<KeyMapEntity>()

            id.forEach {
                keymaps.add(get(it).copy(id = 0))
            }

            dao.insert(*keymaps.toTypedArray())
        }
    }

    override fun enableById(vararg id: Long) {
        coroutineScope.launch {
            dao.enableKeymapById(*id)
        }
    }

    override fun disableById(vararg id: Long) {
        coroutineScope.launch {
            dao.disableKeymapById(*id)
        }
    }

    override fun enableAll() {
        coroutineScope.launch {
            dao.enableAll()
        }
    }

    override fun disableAll() {
        coroutineScope.launch {
            dao.disableAll()
        }
    }
}