package io.github.sds100.keymapper.framework.adapters

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import io.github.sds100.keymapper.domain.adapter.ServiceAdapter
import io.github.sds100.keymapper.service.MyAccessibilityService
import io.github.sds100.keymapper.ui.utils.getJsonSerializable
import io.github.sds100.keymapper.ui.utils.putJsonSerializable
import io.github.sds100.keymapper.util.AccessibilityUtils
import io.github.sds100.keymapper.util.Event
import io.github.sds100.keymapper.util.result.RecoverableError
import io.github.sds100.keymapper.util.result.Result
import io.github.sds100.keymapper.util.result.Success
import io.github.sds100.keymapper.util.sendPackageBroadcast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

/**
 * Created by sds100 on 17/03/2021.
 */
class AccessibilityServiceAdapter(context: Context, coroutineScope: CoroutineScope) :
    ServiceAdapter {
    private val ctx = context.applicationContext
    override val eventReceiver = MutableSharedFlow<Event>()

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
        IntentFilter().apply {
            addAction(MyAccessibilityService.ACTION_SEND_EVENT)
            ctx.registerReceiver(broadcastReceiver, this)
        }
    }

    override fun send(event: Event): Result<Unit> {

        if (!AccessibilityUtils.isServiceEnabled(ctx)) {
            return RecoverableError.AccessibilityServiceDisabled
        }

        val bundle = Bundle().apply {
            putJsonSerializable(MyAccessibilityService.KEY_EVENT, event)
        }

        ctx.sendPackageBroadcast(MyAccessibilityService.ACTION_SEND_EVENT, bundle)

        return Success(Unit)
    }
}