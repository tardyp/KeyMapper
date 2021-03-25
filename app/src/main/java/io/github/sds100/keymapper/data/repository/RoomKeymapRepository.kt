package io.github.sds100.keymapper.data.repository

import io.github.sds100.keymapper.data.db.dao.KeyMapDao
import io.github.sds100.keymapper.data.model.KeyMapEntity
import io.github.sds100.keymapper.domain.mappings.keymap.KeymapRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.*

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

    override suspend fun get(uid: String): KeyMapEntity? {
        return dao.getByUid(uid)
    }

    override fun delete(vararg uid: String) {
        coroutineScope.launch {
            dao.deleteById(*uid)
        }
    }

    override fun duplicate(vararg uid: String) {
        coroutineScope.launch {
            val keymaps = mutableListOf<KeyMapEntity>()

            uid.forEach {
                val keymap = get(it) ?: return@forEach
                keymaps.add(keymap.copy(id = 0, uid = UUID.randomUUID().toString()))
            }

            dao.insert(*keymaps.toTypedArray())
        }
    }

    override fun enableById(vararg uid: String) {
        coroutineScope.launch {
            dao.enableKeymapByUid(*uid)
        }
    }

    override fun disableById(vararg uid: String) {
        coroutineScope.launch {
            dao.disableKeymapByUid(*uid)
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