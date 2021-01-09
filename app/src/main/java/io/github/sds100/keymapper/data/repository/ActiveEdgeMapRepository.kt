package io.github.sds100.keymapper.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.preferencesKey
import androidx.datastore.preferences.core.remove
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import com.github.salomonbrys.kotson.fromJson
import com.github.salomonbrys.kotson.registerTypeAdapter
import com.google.gson.GsonBuilder
import io.github.sds100.keymapper.data.model.*
import io.github.sds100.keymapper.util.FingerprintMapUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

/**
 * Created by sds100 on 17/11/20.
 */
class ActiveEdgeMapRepository(private val mDataStore: DataStore<Preferences>) {

    private val mGson = GsonBuilder()
        .registerTypeAdapter(ActiveEdgeMap.DESERIALIZER)
        .registerTypeAdapter(Action.DESERIALIZER)
        .registerTypeAdapter(Extra.DESERIALIZER)
        .registerTypeAdapter(Constraint.DESERIALIZER).create()

    val activeEdgeMap: Flow<ActiveEdgeMap> = mDataStore.data.map { prefs ->
        val json = prefs.get(PreferenceKeys.ACTIVE_EDGE_MAP)

        if (json == null) {
            ActiveEdgeMap()
        } else {
            mGson.fromJson(json)
        }
    }

    val activeEdgeMapLiveData = activeEdgeMap.asLiveData()

    //TODO detect when active edge is available
    val activeEdgeAvailable = MutableLiveData(true)

    suspend fun setActiveEdgeAvailable(available: Boolean) {
        mDataStore.edit {
            it[PreferenceKeys.ACTIVE_EDGE_AVAILABLE] = available
        }
    }

    suspend fun edit(
        block: (old: ActiveEdgeMap) -> ActiveEdgeMap
    ) {
        mDataStore.edit { prefs ->
            val key = PreferenceKeys.ACTIVE_EDGE_MAP
            val json = prefs.get(PreferenceKeys.ACTIVE_EDGE_MAP)

            val old = if (json == null) {
                ActiveEdgeMap()
            } else {
                mGson.fromJson(json)
            }

            val new = block.invoke(old)

            prefs[key] = mGson.toJson(new)
        }
    }

    suspend fun reset() {
        mDataStore.edit { prefs ->
            prefs.remove(PreferenceKeys.ACTIVE_EDGE_MAP)
        }
    }

    private object PreferenceKeys {
        val ACTIVE_EDGE_MAP = preferencesKey<String>("active_edge_map")

        val ACTIVE_EDGE_AVAILABLE =
            preferencesKey<Boolean>("active_edge_available")
    }
}