package io.github.sds100.keymapper

import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.multidex.MultiDexApplication
import io.github.sds100.keymapper.data.repository.AndroidAppRepository
import io.github.sds100.keymapper.domain.mappings.keymap.trigger.RecordTriggerController
import io.github.sds100.keymapper.domain.usecases.ManageNotificationsUseCase
import io.github.sds100.keymapper.files.AndroidFileAdapter
import io.github.sds100.keymapper.framework.adapters.*
import io.github.sds100.keymapper.permissions.Permission
import io.github.sds100.keymapper.ui.INotificationController
import io.github.sds100.keymapper.ui.NotificationViewModel
import io.github.sds100.keymapper.util.*
import kotlinx.coroutines.MainScope
import timber.log.Timber

/**
 * Created by sds100 on 19/05/2020.
 */
class KeyMapperApp : MultiDexApplication(), INotificationManagerWrapper, INotificationController {
    val appCoroutineScope = MainScope()

    val notificationController by lazy {
        NotificationViewModel(
            appCoroutineScope,
            manager = this,
            ManageNotificationsUseCase(ServiceLocator.preferenceRepository(this)),
            iNotificationController = this,
            isServiceEnabled = UseCases.isAccessibilityServiceEnabled(this)
        )
    }

    val appRepository by lazy { AndroidAppRepository(packageManager) }
    val resourceProvider by lazy { ResourceProviderImpl(this) }

    val bluetoothMonitor by lazy { AndroidBluetoothMonitor(this, appCoroutineScope) }

    val packageManagerAdapter by lazy {
        AndroidPackageManagerAdapter(
            this,
            appCoroutineScope
        )
    }

    val inputMethodAdapter by lazy { AndroidInputMethodAdapter(this) }
    val externalDevicesAdapter by lazy {
        AndroidExternalDevicesAdapter(
            this,
            bluetoothMonitor,
            appCoroutineScope
        )
    }
    val cameraAdapter by lazy { AndroidCameraAdapter(this) }
    val permissionAdapter by lazy {
        AndroidPermissionAdapter(
            this,
            appCoroutineScope,
            ServiceLocator.preferenceRepository(this)
        )
    }
    val systemFeatureAdapter by lazy { AndroidSystemFeatureAdapter(this) }
    val serviceAdapter by lazy { AccessibilityServiceAdapter(this, appCoroutineScope) }
    val appShortcutAdapter by lazy { AndroidAppShortcutAdapter(this) }

    val recordTriggerController by lazy {
        RecordTriggerController(appCoroutineScope, serviceAdapter)
    }

    val fileAdapter by lazy { AndroidFileAdapter(this) }

    private val applicationViewModel by lazy { Inject.keyMapperAppViewModel(this) }

    private val processLifecycleOwner by lazy { ProcessLifecycleOwner.get() }

    override fun onCreate() {

        applicationViewModel.theme.observeForever {
            AppCompatDelegate.setDefaultNightMode(it)
        }

        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        initialiseManagers()

        processLifecycleOwner.lifecycle.addObserver(object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
            fun onResume() {
                //when the user returns to the app let everything know that the permissions could have changed
                permissionAdapter.onPermissionsChanged()
                serviceAdapter.updateWhetherServiceIsEnabled()

                if (BuildConfig.DEBUG && permissionAdapter.isGranted(Permission.WRITE_SECURE_SETTINGS)) {
                    serviceAdapter.enableService()
                }
            }
        })
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