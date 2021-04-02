package io.github.sds100.keymapper.framework.adapters

import android.content.*
import android.database.ContentObserver
import android.net.Uri
import android.os.*
import android.provider.Settings
import io.github.sds100.keymapper.domain.adapter.ServiceAdapter
import io.github.sds100.keymapper.framework.JobSchedulerHelper
import io.github.sds100.keymapper.service.MyAccessibilityService
import io.github.sds100.keymapper.ui.utils.getJsonSerializable
import io.github.sds100.keymapper.ui.utils.putJsonSerializable
import io.github.sds100.keymapper.util.AccessibilityUtils
import io.github.sds100.keymapper.util.Event
import io.github.sds100.keymapper.util.result.FixableError
import io.github.sds100.keymapper.util.result.Result
import io.github.sds100.keymapper.util.result.Success
import io.github.sds100.keymapper.util.sendPackageBroadcast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

/**
 * Created by sds100 on 17/03/2021.
 */
class AccessibilityServiceAdapter(context: Context, coroutineScope: CoroutineScope) :
    ServiceAdapter {
    private val ctx = context.applicationContext
    override val eventReceiver = MutableSharedFlow<Event>()

    override val isEnabled = MutableStateFlow(getIsEnabled())

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

    fun updateWhetherServiceIsEnabled() {
        isEnabled.value = getIsEnabled()
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