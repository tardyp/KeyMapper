package io.github.sds100.keymapper.framework.adapters

import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import io.github.sds100.keymapper.util.result.Result

/**
 * Created by sds100 on 03/03/2021.
 */

class AndroidAppInfoAdapter(packageManager: PackageManager) : AppInfoAdapter {
    override fun getAppName(packageName: String): Result<String> {
        TODO("Not yet implemented")
    }

    override fun getAppIcon(packageName: String): Result<Drawable> {
        TODO("Not yet implemented")
    }
}