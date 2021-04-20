package io.github.sds100.keymapper.system.apps

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * Created by sds100 on 27/10/2018.
 */

object PackageUtils {

    fun isAppInstalled(ctx: Context, packageName: String): Boolean {
        try {
            ctx.packageManager.getApplicationInfo(packageName, 0)

            return true

        } catch (e: Exception) {
            return false
        }
    }
}