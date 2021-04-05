package io.github.sds100.keymapper.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.preferencesKey
import androidx.datastore.preferences.core.remove
import androidx.lifecycle.MutableLiveData
import com.github.salomonbrys.kotson.fromJson
import com.github.salomonbrys.kotson.nullInt
import com.github.salomonbrys.kotson.registerTypeAdapter
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import io.github.sds100.keymapper.data.db.migration.JsonMigration
import io.github.sds100.keymapper.data.db.migration.fingerprintmaps.Migration_0_1
import io.github.sds100.keymapper.data.model.ActionEntity
import io.github.sds100.keymapper.data.model.ConstraintEntity
import io.github.sds100.keymapper.data.model.Extra
import io.github.sds100.keymapper.data.model.FingerprintMapEntity
import io.github.sds100.keymapper.mappings.fingerprintmaps.FingerprintMapEntityGroup
import io.github.sds100.keymapper.util.BackupRequest
import io.github.sds100.keymapper.util.MigrationUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Created by sds100 on 17/11/20.
 */
class DataStoreFingerprintMapRepository(
    private val dataStore: DataStore<Preferences>,
    private val coroutineScope: CoroutineScope
) : FingerprintMapRepository {

    companion object {
        val PREF_KEYS_MAP = mapOf(
            FingerprintMapEntity.ID_SWIPE_DOWN to PreferenceKeys.FINGERPRINT_GESTURE_SWIPE_DOWN,
            FingerprintMapEntity.ID_SWIPE_UP to PreferenceKeys.FINGERPRINT_GESTURE_SWIPE_UP,
            FingerprintMapEntity.ID_SWIPE_LEFT to PreferenceKeys.FINGERPRINT_GESTURE_SWIPE_LEFT,
            FingerprintMapEntity.ID_SWIPE_RIGHT to PreferenceKeys.FINGERPRINT_GESTURE_SWIPE_RIGHT
        )

        private val MIGRATIONS = listOf(
            JsonMigration(0, 1) { gson, json -> Migration_0_1.migrate(gson, json) }
        )
    }

    private val gson = GsonBuilder()
        .registerTypeAdapter(FingerprintMapEntity.DESERIALIZER)
        .registerTypeAdapter(ActionEntity.DESERIALIZER)
        .registerTypeAdapter(Extra.DESERIALIZER)
        .registerTypeAdapter(ConstraintEntity.DESERIALIZER).create()

    private val jsonParser = JsonParser()

    override val fingerprintMaps: Flow<FingerprintMapEntityGroup> = dataStore.data.map { prefs ->
        val group = FingerprintMapEntityGroup(
            swipeDown = prefs.getGesture(PreferenceKeys.FINGERPRINT_GESTURE_SWIPE_DOWN),
            swipeUp = prefs.getGesture(PreferenceKeys.FINGERPRINT_GESTURE_SWIPE_UP),
            swipeLeft = prefs.getGesture(PreferenceKeys.FINGERPRINT_GESTURE_SWIPE_LEFT),
            swipeRight = prefs.getGesture(PreferenceKeys.FINGERPRINT_GESTURE_SWIPE_RIGHT),
        )

        group
    }.dropWhile { group ->
        group.swipeDown.version < FingerprintMapEntity.CURRENT_VERSION
            || group.swipeUp.version < FingerprintMapEntity.CURRENT_VERSION
            || group.swipeLeft.version < FingerprintMapEntity.CURRENT_VERSION
            || group.swipeRight.version < FingerprintMapEntity.CURRENT_VERSION
    }

    override val requestAutomaticBackup =
        MutableLiveData<BackupRequest<Map<String, FingerprintMapEntity>>>()

    override fun restore(id: String, fingerprintMapJson: String) {
        //TODO
//            val rootElement = jsonParser.parse(fingerprintMapJson)
//            val initialVersion =
//                rootElement.asJsonObject.get(FingerprintMapEntity.NAME_VERSION).nullInt
//                    ?: 0
//
//            val migratedJson = MigrationUtils.migrate(
//                gson,
//                MIGRATIONS,
//                initialVersion,
//                fingerprintMapJson,
//                FingerprintMapEntity.CURRENT_VERSION
//            )


        //TODO
//            setGesture(id, migratedJson)    }
    }

    override fun update(id: String, fingerprintMap: FingerprintMapEntity) {
        coroutineScope.launch {
            dataStore.edit { prefs ->
                val key = PREF_KEYS_MAP[id]!!

                prefs[key] = gson.toJson(fingerprintMap)
            }

            requestBackup()
        }
    }

    override fun reset() {
        coroutineScope.launch {
            dataStore.edit { prefs ->
                PreferenceKeys.ALL_SWIPE_KEYS.forEach {
                    prefs.remove(it)
                }
            }
        }
    }

    override fun enableFingerprintMap(id: String) {
        coroutineScope.launch {
            val fingerprintMap = fingerprintMaps.firstOrNull()?.get(id) ?: return@launch
            update(id, fingerprintMap.copy(isEnabled = true))
        }
    }

    override fun disableFingerprintMap(id: String) {
        coroutineScope.launch {
            val fingerprintMap = fingerprintMaps.firstOrNull()?.get(id) ?: return@launch
            update(id, fingerprintMap.copy(isEnabled = false))
        }
    }

    override fun enableAll() {
        coroutineScope.launch {
            val group = fingerprintMaps.firstOrNull()?: return@launch

            val newGroup = group.copy(
                swipeDown = group.swipeDown.copy(isEnabled = true),
                swipeUp = group.swipeUp.copy(isEnabled = true),
                swipeLeft = group.swipeLeft.copy(isEnabled = true),
                swipeRight = group.swipeRight.copy(isEnabled = true),
            )

            dataStore.edit { prefs ->
                prefs[PreferenceKeys.FINGERPRINT_GESTURE_SWIPE_DOWN] = gson.toJson(newGroup.swipeDown)
                prefs[PreferenceKeys.FINGERPRINT_GESTURE_SWIPE_UP] = gson.toJson(newGroup.swipeUp)
                prefs[PreferenceKeys.FINGERPRINT_GESTURE_SWIPE_LEFT] = gson.toJson(newGroup.swipeLeft)
                prefs[PreferenceKeys.FINGERPRINT_GESTURE_SWIPE_RIGHT] = gson.toJson(newGroup.swipeRight)
            }
        }
    }

    override fun disableAll() {
        coroutineScope.launch {
            val group = fingerprintMaps.firstOrNull()?: return@launch

            val newGroup = group.copy(
                swipeDown = group.swipeDown.copy(isEnabled = false),
                swipeUp = group.swipeUp.copy(isEnabled = false),
                swipeLeft = group.swipeLeft.copy(isEnabled = false),
                swipeRight = group.swipeRight.copy(isEnabled = false),
            )

            dataStore.edit { prefs ->
                prefs[PreferenceKeys.FINGERPRINT_GESTURE_SWIPE_DOWN] = gson.toJson(newGroup.swipeDown)
                prefs[PreferenceKeys.FINGERPRINT_GESTURE_SWIPE_UP] = gson.toJson(newGroup.swipeUp)
                prefs[PreferenceKeys.FINGERPRINT_GESTURE_SWIPE_LEFT] = gson.toJson(newGroup.swipeLeft)
                prefs[PreferenceKeys.FINGERPRINT_GESTURE_SWIPE_RIGHT] = gson.toJson(newGroup.swipeRight)
            }
        }
    }

    private suspend fun setGesture(
        gestureId: String,
        fingerprintMapJson: String
    ) {
        dataStore.edit { prefs ->
            val key = PREF_KEYS_MAP[gestureId]!!
            prefs[key] = fingerprintMapJson
        }

        requestBackup()
    }

    private suspend fun requestBackup() {
        val maps = PREF_KEYS_MAP.mapValues { (_, preferenceKey) ->
            dataStore.data.firstOrNull()?.getGesture(preferenceKey) ?: FingerprintMapEntity()
        }

        //don't back up if they haven't been migrated
        if (maps.any { it.value.version < FingerprintMapEntity.CURRENT_VERSION }) return

        requestAutomaticBackup.value = BackupRequest(maps)
    }

    private suspend fun Preferences.getGesture(key: Preferences.Key<String>): FingerprintMapEntity {
        val json = this[key] ?: return FingerprintMapEntity()

        val rootElement = jsonParser.parse(json)
        val initialVersion =
            rootElement.asJsonObject.get(FingerprintMapEntity.NAME_VERSION).nullInt ?: 0

        val migratedJson = MigrationUtils.migrate(
            gson,
            MIGRATIONS,
            initialVersion,
            json,
            FingerprintMapEntity.CURRENT_VERSION
        )

        if (initialVersion < FingerprintMapEntity.CURRENT_VERSION) {
            setGesture(key.name, migratedJson)
        }

        return gson.fromJson(migratedJson)
    }

    private fun FingerprintMapEntityGroup.get(id: String): FingerprintMapEntity {
        return when (id) {
            FingerprintMapEntity.ID_SWIPE_DOWN -> swipeDown
            FingerprintMapEntity.ID_SWIPE_UP -> swipeUp
            FingerprintMapEntity.ID_SWIPE_LEFT -> swipeLeft
            FingerprintMapEntity.ID_SWIPE_RIGHT -> swipeRight
            else -> throw IllegalArgumentException("Don't know how to get fingerprint map for id $id")
        }
    }

    private object PreferenceKeys {
        val FINGERPRINT_GESTURE_SWIPE_DOWN = preferencesKey<String>("swipe_down")
        val FINGERPRINT_GESTURE_SWIPE_UP = preferencesKey<String>("swipe_up")
        val FINGERPRINT_GESTURE_SWIPE_LEFT = preferencesKey<String>("swipe_left")
        val FINGERPRINT_GESTURE_SWIPE_RIGHT = preferencesKey<String>("swipe_right")

        val ALL_SWIPE_KEYS = arrayOf(
            FINGERPRINT_GESTURE_SWIPE_DOWN,
            FINGERPRINT_GESTURE_SWIPE_LEFT,
            FINGERPRINT_GESTURE_SWIPE_UP,
            FINGERPRINT_GESTURE_SWIPE_RIGHT
        )
    }
}