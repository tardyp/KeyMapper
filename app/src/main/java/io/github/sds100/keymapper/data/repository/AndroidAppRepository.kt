package io.github.sds100.keymapper.data.repository

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.PackageManager.GET_META_DATA
import android.content.pm.ResolveInfo
import android.graphics.drawable.Drawable
import android.os.Build
import io.github.sds100.keymapper.util.result.Result

/**
 * Created by sds100 on 27/01/2020.
 */
class AndroidAppRepository(private val packageManager: PackageManager) : AppRepository{

    override suspend fun getAllAppList(): List<ApplicationInfo> {
        return packageManager.getInstalledApplications(GET_META_DATA)
    }

    override suspend fun getLaunchableAppList(): List<ApplicationInfo> {
        return packageManager.getInstalledApplications(GET_META_DATA)
            .filter {
                it.icon
                (packageManager.getLaunchIntentForPackage(it.packageName) != null
                    || packageManager.getLeanbackLaunchIntentForPackage(it.packageName) != null)
            }
    }

    override suspend fun getAppShortcutList(): List<ResolveInfo> {
        val shortcutIntent = Intent(Intent.ACTION_CREATE_SHORTCUT)
        return packageManager.queryIntentActivities(shortcutIntent, 0)
    }

    override fun getAppIcon(applicationInfo: ApplicationInfo): Drawable? {
        return applicationInfo.loadIcon(packageManager)
    }

    override fun getIntentIcon(resolveInfo: ResolveInfo): Drawable? {
        return resolveInfo.loadIcon(packageManager)
    }

    override fun getAppName(applicationInfo: ApplicationInfo): String {
        return applicationInfo.loadLabel(packageManager).toString()
    }

    override fun getAppName(packageName: String): String {
        val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
        return getAppName(applicationInfo)
    }

    override fun getIntentLabel(resolveInfo: ResolveInfo): String {
        return resolveInfo.loadLabel(packageManager).toString()
    }
}
