package io.github.sds100.keymapper.framework.adapters

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import io.github.sds100.keymapper.domain.packages.PackageInfo
import io.github.sds100.keymapper.domain.packages.PackageManagerAdapter
import io.github.sds100.keymapper.util.DataState
import io.github.sds100.keymapper.util.Loading
import io.github.sds100.keymapper.util.getDataState
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

    override val installedPackages = MutableStateFlow<DataState<List<PackageInfo>>>(Loading())

    //TODO have broadcast receiver that updates the installed packages when a new package is installed, removed or change

    init {
        coroutineScope.launch {
            installedPackages.value = Loading()

            packageManager.getInstalledApplications(PackageManager.GET_META_DATA).map {
                val canBeLaunched =
                    (packageManager.getLaunchIntentForPackage(it.packageName) != null
                        || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                        && packageManager.getLeanbackLaunchIntentForPackage(it.packageName) != null))

                PackageInfo(it.packageName, canBeLaunched)
            }.let { installedPackages.value = it.getDataState() }
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
}