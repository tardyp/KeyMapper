package io.github.sds100.keymapper.framework.adapters

import android.content.*
import android.database.ContentObserver
import android.net.Uri
import android.os.*
import android.provider.Settings
import io.github.sds100.keymapper.Constants
import io.github.sds100.keymapper.ServiceLocator
import io.github.sds100.keymapper.domain.adapter.PermissionAdapter
import io.github.sds100.keymapper.domain.adapter.ServiceAdapter
import io.github.sds100.keymapper.framework.JobSchedulerHelper
import io.github.sds100.keymapper.permissions.Permission
import io.github.sds100.keymapper.service.MyAccessibilityService
import io.github.sds100.keymapper.ui.activity.MainActivity
import io.github.sds100.keymapper.ui.utils.getJsonSerializable
import io.github.sds100.keymapper.ui.utils.putJsonSerializable
import io.github.sds100.keymapper.util.*
import io.github.sds100.keymapper.util.result.FixableError
import io.github.sds100.keymapper.util.result.Result
import io.github.sds100.keymapper.util.result.Success
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Created by sds100 on 17/03/2021.
 */
class AccessibilityServiceAdapter(
    context: Context,
    private val coroutineScope: CoroutineScope
) : ServiceAdapter {

    private val ctx = context.applicationContext
    override val eventReceiver = MutableSharedFlow<Event>()

    override val isEnabled = MutableStateFlow(getIsEnabled())

    private val permissionAdapter: PermissionAdapter by lazy { ServiceLocator.permissionAdapter(ctx) }

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            context ?: return
            intent ?: return

            when (intent.action) {
                MyAccessibilityService.ACTION_SEND_EVENT ->
                    coroutineScope.launch {
                        val event =
                            intent.extras?.getJsonSerializable<Event>(MyAccessibilityService.KEY_EVENT)
                        event ?: return@launch

                        eventReceiver.emit(event)
                    }
            }
        }
    }

    init {
        //use job scheduler because there is there is a much shorter delay when the app is in the background
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            JobSchedulerHelper.observeEnabledAccessibilityServices(ctx)
        } else {
            val uri = Settings.Secure.getUriFor(Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
            val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
                override fun onChange(selfChange: Boolean, uri: Uri?) {
                    super.onChange(selfChange, uri)

                    isEnabled.value = getIsEnabled()
                }
            }

            ctx.contentResolver.registerContentObserver(uri, false, observer)
        }

        IntentFilter().apply {
            addAction(MyAccessibilityService.ACTION_SEND_EVENT)
            ctx.registerReceiver(broadcastReceiver, this)
        }
    }

    override fun send(event: Event): Result<Unit> {

        if (!AccessibilityUtils.isServiceEnabled(ctx)) {
            return FixableError.AccessibilityServiceDisabled
        }

        val bundle = Bundle().apply {
            putJsonSerializable(MyAccessibilityService.KEY_EVENT, event)
        }

        ctx.sendPackageBroadcast(MyAccessibilityService.ACTION_SEND_EVENT, bundle)

        return Success(Unit)
    }

    override fun enableService() {
        if (permissionAdapter.isGranted(Permission.WRITE_SECURE_SETTINGS)) {
            val enabledServices = SettingsUtils.getSecureSetting<String>(
                ctx,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            )

            val className = MyAccessibilityService::class.java.name

            val keyMapperEntry = "${Constants.PACKAGE_NAME}/$className"

            val newEnabledServices = when {
                enabledServices == null -> keyMapperEntry
                enabledServices.contains(keyMapperEntry) -> enabledServices
                else -> "$keyMapperEntry:$enabledServices"
            }

            SettingsUtils.putSecureSetting(
                ctx,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES, newEnabledServices
            )

            /*
            Turning on the accessibility service doesn't necessarily mean that it is running so
            this will check if it is indeed running and then turn it off and on so that it
            is running.
             */
            coroutineScope.launch {
                send(PingService("ping_accessibility_service"))
                var isCrashed = true

                val job = eventReceiver.onEach {
                    if (it is PingServiceResponse) {
                        isCrashed = false
                    }
                }.launchIn(coroutineScope)

                delay(1000L)
                job.cancel()

                if (isCrashed) {
                    disableService()
                    delay(200)

                    SettingsUtils.putSecureSetting(
                        ctx,
                        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES, newEnabledServices
                    )
                }
            }
        } else {
            openAccessibilitySettings()
        }
    }

    override fun disableService() {
        if (permissionAdapter.isGranted(Permission.WRITE_SECURE_SETTINGS)) {
            val enabledServices = SettingsUtils.getSecureSetting<String>(
                ctx,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            )

            enabledServices ?: return

            val className = MyAccessibilityService::class.java.name

            val keyMapperEntry = "${Constants.PACKAGE_NAME}/$className"

            val newEnabledServices = if (enabledServices.contains(keyMapperEntry)) {
                val services = enabledServices.split(':').toMutableList()
                services.remove(keyMapperEntry)

                services.joinToString(":")
            } else {
                enabledServices
            }

            SettingsUtils.putSecureSetting(
                ctx,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
                newEnabledServices
            )
        } else {
            openAccessibilitySettings()
        }
    }

    fun updateWhetherServiceIsEnabled() {
        isEnabled.value = getIsEnabled()
    }

    private fun openAccessibilitySettings() {
        try {
            val settingsIntent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)

            settingsIntent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                    or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
            )

            ctx.startActivity(settingsIntent)

        } catch (e: ActivityNotFoundException) {
            //open the app to show a dialog to tell the user to give the app WRITE_SECURE_SETTINGS permission
            Intent(ctx, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                putExtra(MainActivity.KEY_SHOW_ACCESSIBILITY_SETTINGS_NOT_FOUND_DIALOG, true)

                ctx.startActivity(this)
            }
        }
    }

    private fun getIsEnabled(): Boolean {
        /* get a list of all the enabled accessibility services.
         * The AccessibilityManager.getEnabledAccessibilityServices() method just returns an empty
         * list. :(*/
        val settingValue = Settings.Secure.getString(
            ctx.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )

        //it can be null if the user has never interacted with accessibility settings before
        if (settingValue != null) {
            /* cant just use .contains because the debug and release accessibility service both contain
               io.github.sds100.keymapper. the enabled_accessibility_services are stored as

                 io.github.sds100.keymapper.debug/io.github.sds100.keymapper.service.MyAccessibilityService
                 :io.github.sds100.keymapper/io.github.sds100.keymapper.service.MyAccessibilityService

                 without the new line before the :
            */
            return settingValue.split(':').any { it.split('/')[0] == ctx.packageName }
        }

        return false
    }
}