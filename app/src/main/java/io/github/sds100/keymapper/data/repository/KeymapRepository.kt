package io.github.sds100.keymapper.data.repository

import androidx.lifecycle.LiveData
import io.github.sds100.keymapper.data.model.KeyMapEntity

/**
 * Created by sds100 on 13/02/21.
 */
interface KeymapRepository {
    val keymapList: LiveData<List<KeyMapEntity>>
    fun duplicateKeymap(vararg id: Long)

    fun insertKeymap(vararg keymap: KeyMapEntity)
    fun updateKeymap(keymap: KeyMapEntity)
    suspend fun getKeymap(id: Long): KeyMapEntity
    fun enableKeymapById(vararg id: Long)
    fun disableKeymapById(vararg id: Long)
    fun deleteKeymap(vararg id: Long)
    fun enableAll()
    fun disableAll()
}