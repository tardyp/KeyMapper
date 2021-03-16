package io.github.sds100.keymapper

import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.multidex.MultiDexApplication
import io.github.sds100.keymapper.data.repository.AndroidAppRepository
import io.github.sds100.keymapper.domain.usecases.ManageNotificationsUseCase
import io.github.sds100.keymapper.framework.adapters.AndroidAppInfoAdapter
import io.github.sds100.keymapper.framework.adapters.AndroidBluetoothMonitor
import io.github.sds100.keymapper.framework.adapters.AndroidPackageManagerAdapter
import io.github.sds100.keymapper.framework.adapters.ResourceProviderImpl
import io.github.sds100.keymapper.util.*
import io.github.sds100.keymapper.util.result.FileAccessDenied
import io.github.sds100.keymapper.util.result.GenericError
import io.github.sds100.keymapper.util.result.Result
import io.github.sds100.keymapper.util.result.Success
import kotlinx.coroutines.MainScope
import timber.log.Timber
import java.io.OutputStream

/**
 * Created by sds100 on 19/05/2020.
 */
class MyApplication : MultiDexApplication(),
    IContentResolver, INotificationManagerWrapper, INotificationController {
    val appCoroutineScope = MainScope()

    val notificationController by lazy {
        NotificationController(
            appCoroutineScope,
            manager = this,
            ManageNotificationsUseCase(ServiceLocator.preferenceRepository(this)),
            iNotificationController = this
        )
    }

    internal val appRepository by lazy { AndroidAppRepository(packageManager) }
    internal val appInfoAdapter by lazy { AndroidAppInfoAdapter(packageManager) }
    internal val resourceProvider by lazy { ResourceProviderImpl(this) }

    internal val bluetoothMonitor by lazy { AndroidBluetoothMonitor(appCoroutineScope) }
    internal val packageManagerAdapter by lazy {
        AndroidPackageManagerAdapter(
            this,
            appCoroutineScope
        )
    }

    private val applicationViewModel by lazy { InjectorUtils.provideApplicationViewModel(this) }

    override fun onCreate() {

        applicationViewModel.theme.observeForever {
            AppCompatDelegate.setDefaultNightMode(it)
        }

        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        initialiseManagers()
    }

    override fun openOutputStream(uriString: String): Result<OutputStream> {
        val uri = Uri.parse(uriString)

        return try {
            val outputStream = contentResolver.openOutputStream(uri)!!

            Success(outputStream)
        } catch (e: Exception) {
            when (e) {
                is SecurityException -> FileAccessDenied()
                else -> GenericError(e)
            }
        }
    }

    override fun showNotification(notification: AppNotification) {
        NotificationUtils.showNotification(this, notification)
    }

    override fun dismissNotification(notificationId: Int) {
        NotificationUtils.dismissNotification(this, notificationId)
    }

    override fun createChannel(vararg channelId: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationUtils.createChannel(this, *channelId)
        }
    }

    override fun deleteChannel(channelId: String) {
        NotificationUtils.deleteChannel(this, channelId)
    }

    override fun isAccessibilityServiceEnabled(): Boolean {
        return AccessibilityUtils.isServiceEnabled(this)
    }

    override fun haveWriteSecureSettingsPermission(): Boolean {
        return PermissionUtils.haveWriteSecureSettingsPermission(this)
    }

    private fun initialiseManagers() {
        ServiceLocator.backupManager(this)
    }
}