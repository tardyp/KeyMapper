package io.github.sds100.keymapper.broadcastreceiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.github.sds100.keymapper.ServiceLocator
import io.github.sds100.keymapper.util.OnBootEvent

/**
 * Created by sds100 on 24/03/2019.
 */

class BootBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        context ?: return
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            /*
            Initializing the controller will update any notifications since it will collect the values
            in the constructor
             */
            ServiceLocator.notificationController(context)
        }
    }
}