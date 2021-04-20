package io.github.sds100.keymapper.system.apps

import android.content.*
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.provider.Settings
import io.github.sds100.keymapper.util.State
import io.github.sds100.keymapper.util.Result
import io.github.sds100.keymapper.util.success
import io.github.sds100.keymapper.util.Error
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            context ?: return
            intent ?: return

            when (intent.action) {
                Intent.ACTION_PACKAGE_CHANGED,
                Intent.ACTION_PACKAGE_ADDED,
                Intent.ACTION_PACKAGE_REMOVED,
                Intent.ACTION_PACKAGE_REPLACED -> {
                    coroutineScope.launch(Dispatchers.Default) {
                        updatePackageList()
                    }
                }
            }
        }
    }

    init {
        coroutineScope.launch(Dispatchers.Default) {
            updatePackageList()
        }

        IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_CHANGED)
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_REPLACED)
            addDataScheme("package")

            ctx.registerReceiver(broadcastReceiver, this)
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
        val activityExists =
            Intent(Intent.ACTION_VOICE_COMMAND).resolveActivityInfo(ctx.packageManager, 0) != null

        return activityExists
    }

    override fun getAppName(packageName: String): Result<String> {
        try {
            return packageManager
                .getApplicationInfo(packageName, 0)
                .loadLabel(packageManager)
                .toString()
                .success()
        } catch (e: PackageManager.NameNotFoundException) {
            return Error.AppNotFound(packageName)
        }
    }

    override fun getAppIcon(packageName: String): Result<Drawable> {
        try {
            return packageManager
                .getApplicationInfo(packageName, 0)
                .loadIcon(packageManager)
                .success()
        } catch (e: PackageManager.NameNotFoundException) {
            return Error.AppNotFound(packageName)
        }
    }
    
    private fun updatePackageList(){
        installedPackages.value = State.Loading

       val packages = packageManager.getInstalledApplications(PackageManager.GET_META_DATA).map {
            val canBeLaunched =
                (packageManager.getLaunchIntentForPackage(it.packageName) != null
                    || packageManager.getLeanbackLaunchIntentForPackage(it.packageName) != null)

            PackageInfo(it.packageName, canBeLaunched)
        }

        installedPackages.value = State.Data(packages)
    }
}