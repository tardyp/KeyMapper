package io.github.sds100.keymapper.framework.adapters

import android.graphics.drawable.Drawable
import kotlinx.coroutines.flow.Flow

/**
 * Created by sds100 on 26/02/2021.
 */

interface AppInfoAdapter {
    fun getAppName(packageName: String): Flow<String>
    fun getAppIcon(packageName: String): Flow<Drawable>
}