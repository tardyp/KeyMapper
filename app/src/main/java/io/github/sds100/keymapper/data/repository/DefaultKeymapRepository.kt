package io.github.sds100.keymapper.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.github.salomonbrys.kotson.fromJson
import com.google.gson.Gson
import io.github.sds100.keymapper.data.db.AppDatabase
import io.github.sds100.keymapper.data.db.dao.KeyMapDao
import io.github.sds100.keymapper.data.db.migration.JsonMigration
import io.github.sds100.keymapper.data.db.migration.keymaps.Migration_9_10
import io.github.sds100.keymapper.data.model.KeyMapEntity
import io.github.sds100.keymapper.data.usecase.*
import io.github.sds100.keymapper.util.BackupRequest
import io.github.sds100.keymapper.util.MigrationUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Created by sds100 on 26/01/2020.
 */
class DefaultKeymapRepository internal constructor(
    private val keymapDao: KeyMapDao,
    private val coroutineScope: CoroutineScope
) : GlobalKeymapUseCase,
    KeymapListUseCase,
    BackupRestoreUseCase,
    MenuKeymapUseCase,
    CreateKeymapShortcutUseCase,
KeymapRepository{

    companion object {
        private val MIGRATIONS = listOf(
            JsonMigration(9, 10) { gson, json -> Migration_9_10.migrateJson(gson, json) }
        )
    }

    private val gson = Gson()

    override val requestAutomaticBackup = MutableLiveData<BackupRequest<List<KeyMapEntity>>>()
    override val keymapList: LiveData<List<KeyMapEntity>> = keymapDao.observeAll()

    override suspend fun getKeymaps(): List<KeyMapEntity> = keymapDao.getAll()

    override suspend fun getKeymap(id: Long) = keymapDao.getById(id)

    /**
     * Ensure the database version for the backed up key maps is <= the database version in this version of Key Mapper
     */
    override fun restore(dbVersion: Int, keymapListJson: List<String>) {
        val migratedKeymapList = keymapListJson.map {
            val migratedJson = MigrationUtils.migrate(
                gson,
                MIGRATIONS,
                dbVersion,
                it,
                AppDatabase.DATABASE_VERSION
            )

            gson.fromJson<KeyMapEntity>(migratedJson)
        }

        coroutineScope.launch {
            insertKeymap(*migratedKeymapList.toTypedArray())
        }
    }

    override fun insertKeymap(vararg keymap: KeyMapEntity) {
        coroutineScope.launch {
            keymapDao.insert(*keymap)

            requestBackup()
        }
    }

    override fun updateKeymap(keymap: KeyMapEntity) {
        coroutineScope.launch {
            keymapDao.update(keymap)

            requestBackup()
        }
    }

    override fun duplicateKeymap(vararg id: Long) {
        coroutineScope.launch {
            val keymaps = mutableListOf<KeyMapEntity>()

            id.forEach {
                keymaps.add(getKeymap(it).copy(id = 0))
            }

            insertKeymap(*keymaps.toTypedArray())
        }
    }

    override fun enableKeymapById(vararg id: Long) {
        coroutineScope.launch {
            keymapDao.enableKeymapById(*id)

            requestBackup()
        }
    }

    override fun disableKeymapById(vararg id: Long) {
        coroutineScope.launch {
            keymapDao.disableKeymapById(*id)

            requestBackup()
        }
    }

    override fun deleteKeymap(vararg id: Long) {
        coroutineScope.launch {
            keymapDao.deleteById(*id)

            requestBackup()
        }
    }

    override fun deleteAll() {
        coroutineScope.launch {
            keymapDao.deleteAll()

            requestBackup()
        }
    }

    override fun enableAll() {
        coroutineScope.launch {
            keymapDao.enableAll()

            requestBackup()
        }
    }

    override fun disableAll() {
        coroutineScope.launch {
            keymapDao.disableAll()

            requestBackup()
        }
    }

    private suspend fun requestBackup() {
        withContext(Dispatchers.Default) {
            requestAutomaticBackup.postValue(BackupRequest(getKeymaps()))
        }
    }
}