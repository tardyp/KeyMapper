package io.github.sds100.keymapper.data.db

import android.content.Context
import androidx.datastore.preferences.createDataStore

/**
 * Created by sds100 on 20/02/2020.
 */

class DefaultDataStoreManager(context: Context) : IDataStoreManager {
    private val ctx = context.applicationContext

    override val fingerprintGestureDataStore = ctx.createDataStore("fingerprint_gestures")
}