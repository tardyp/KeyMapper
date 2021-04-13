package io.github.sds100.keymapper.framework.adapters

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.provider.Settings
import io.github.sds100.keymapper.domain.packages.PackageInfo
import io.github.sds100.keymapper.domain.packages.PackageManagerAdapter
import io.github.sds100.keymapper.domain.utils.State
import io.github.sds100.keymapper.util.result.FixableError
import io.github.sds100.keymapper.util.result.Result
import io.github.sds100.keymapper.util.result.success
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

/**
 * Created by sds100 on 16/03/2021.
 */
class AndroidPackageManagerAdapter(
    context: Context,
    coroutineScope: CoroutineScope
) : PackageManagerAdapter {
    private val ctx = context.applicationContext
    private val packageManager = ctx.packageManager

    override val installedPackages = MutableStateFlow<State<List<PackageInfo>>>(State.Loading)

    //TODO have broadcast receiver that updates the installed packages when a new package is installed, removed or change

    init {
        coroutineScope.launch {
            installedPackages.value = State.Loading

            packageManager.getInstalledApplications(PackageManager.GET_META_DATA).map {
                val canBeLaunched =
                    (packageManager.getLaunchIntentForPackage(it.packageName) != null
                        || packageManager.getLeanbackLaunchIntentForPackage(it.packageName) != null)

                PackageInfo(it.packageName, canBeLaunched)
            }.let { installedPackages.value = State.Data(it) }
        }
    }

    override fun installApp(packageName: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("market://details?id=$packageName")
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            ctx.startActivity(intent)

        } catch (e: ActivityNotFoundException) {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
            ctx.startActivity(intent)
        }
    }

    override fun enableApp(packageName: String) {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:${packageName}")
            flags = Intent.FLAG_ACTIVITY_NO_HISTORY

            ctx.startActivity(this)
        }
    }

    override fun isAppEnabled(packageName: String): Boolean {
        return try {
            packageManager.getApplicationInfo(packageName, 0).enabled
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    override fun isAppInstalled(packageName: String): Boolean {
        return try {
            packageManager.getApplicationInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    override fun isVoiceAssistantInstalled(): Boolean {
        TODO("Not yet implemented")
    }

    override fun getAppName(packageName: String): Result<String> {
        try {
            return packageManager
                .getApplicationInfo(packageName, 0)
                .loadLabel(packageManager)
                .toString()
                .success()
        } catch (e: PackageManager.NameNotFoundException) {
            return FixableError.AppNotFound(packageName)
        }
    }

    override fun getAppIcon(packageName: String): Result<Drawable> {
        try {
            return packageManager
                .getApplicationInfo(packageName, 0)
                .loadIcon(packageManager)
                .success()
        } catch (e: PackageManager.NameNotFoundException) {
            return FixableError.AppNotFound(packageName)
        }
    }
}