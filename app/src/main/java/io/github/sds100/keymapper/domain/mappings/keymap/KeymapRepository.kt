package io.github.sds100.keymapper.domain.mappings.keymap

import io.github.sds100.keymapper.data.model.KeyMapEntity
import io.github.sds100.keymapper.domain.utils.State
import kotlinx.coroutines.flow.Flow

/**
 * Created by sds100 on 18/03/2021.
 */
interface KeymapRepository {
    val keyMapList: Flow<State<List<KeyMapEntity>>>

    fun insert(keymap: KeyMapEntity)
    fun update(keymap: KeyMapEntity)
    suspend fun get(uid: String): KeyMapEntity?
    fun delete(vararg uid: String)

    fun duplicate(vararg uid: String)
    fun enableById(vararg uid: String)
    fun disableById(vararg uid: String)
    fun enableAll()
    fun disableAll()
}