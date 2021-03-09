package io.github.sds100.keymapper.framework.adapters

import android.graphics.drawable.Drawable
import io.github.sds100.keymapper.util.result.Result

/**
 * Created by sds100 on 26/02/2021.
 */

interface AppInfoAdapter {
    fun getAppName(packageName: String): Result<String>
    fun getAppIcon(packageName: String): Result<Drawable>
}