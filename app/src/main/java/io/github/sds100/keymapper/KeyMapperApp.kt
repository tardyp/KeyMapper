package io.github.sds100.keymapper

import android.content.Intent
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.multidex.MultiDexApplication
import io.github.sds100.keymapper.domain.mappings.keymap.trigger.RecordTriggerController
import io.github.sds100.keymapper.domain.usecases.ManageNotificationsUseCaseImpl
import io.github.sds100.keymapper.files.AndroidFileAdapter
import io.github.sds100.keymapper.framework.adapters.*
import io.github.sds100.keymapper.inputmethod.ShowHideInputMethodUseCaseImpl
import io.github.sds100.keymapper.notifications.AndroidNotificationAdapter
import io.github.sds100.keymapper.permissions.Permission
import io.github.sds100.keymapper.ui.NotificationController
import io.github.sds100.keymapper.ui.activity.SplashActivity
import io.github.sds100.keymapper.util.*
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Created by sds100 on 19/05/2020.
 */
class KeyMapperApp : MultiDexApplication() {
    val appCoroutineScope = MainScope()

    val notificationAdapter by lazy { AndroidNotificationAdapter(this, appCoroutineScope) }

    lateinit var notificationController: NotificationController

    val resourceProvider by lazy { ResourceProviderImpl(this) }

    val bluetoothMonitor by lazy { AndroidBluetoothMonitor(this, appCoroutineScope) }

    val packageManagerAdapter by lazy {
        AndroidPackageManagerAdapter(
            this,
            appCoroutineScope
        )
    }

    val inputMethodAdapter by lazy {
        AndroidInputMethodAdapter(
            this,
            serviceAdapter,
            permissionAdapter
        )
    }
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

        notificationController = NotificationController(
            appCoroutineScope,
            ManageNotificationsUseCaseImpl(
                ServiceLocator.preferenceRepository(this),
                notificationAdapter,
                UseCases.checkRootPermission(this)
            ),
            UseCases.pauseMappings(this),
            UseCases.showImePicker(this),
            UseCases.controlAccessibilityService(this),
            UseCases.toggleCompatibleIme(this),
            ShowHideInputMethodUseCaseImpl(ServiceLocator.serviceAdapter(this)),
            UseCases.fingerprintGesturesSupported(this),
            UseCases.onboarding(this),
            ServiceLocator.resourceProvider(this)
        )

        processLifecycleOwner.lifecycle.addObserver(object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
            fun onResume() {
                //when the user returns to the app let everything know that the permissions could have changed
                permissionAdapter.onPermissionsChanged()
                serviceAdapter.updateWhetherServiceIsEnabled()
                notificationController.onOpenApp()

                if (BuildConfig.DEBUG && permissionAdapter.isGranted(Permission.WRITE_SECURE_SETTINGS)) {
                    serviceAdapter.enableService()
                }
            }
        })

        appCoroutineScope.launch {
            notificationController.openApp.collectLatest {
                Intent(this@KeyMapperApp, SplashActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK

                    startActivity(this)
                }
            }
        }
    }
}