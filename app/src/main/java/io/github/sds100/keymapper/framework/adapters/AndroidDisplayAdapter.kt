package io.github.sds100.keymapper.framework.adapters

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.view.Surface
import androidx.core.hardware.display.DisplayManagerCompat
import io.github.sds100.keymapper.domain.adapter.DisplayAdapter
import io.github.sds100.keymapper.domain.utils.Orientation
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Created by sds100 on 17/04/2021.
 */
class AndroidDisplayAdapter(context: Context) : DisplayAdapter {
    private val ctx = context.applicationContext

    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent ?: return
            context ?: return

            when (intent.action) {
                Intent.ACTION_SCREEN_ON -> isScreenOn.value = true
                Intent.ACTION_SCREEN_OFF -> isScreenOn.value = false
            }
        }
    }

    override val isScreenOn = MutableStateFlow(true)

    override val orientation: Orientation
        get() {
            val sdkRotation = DisplayManagerCompat.getInstance(ctx).displays[0].rotation

            return when (sdkRotation) {
                Surface.ROTATION_0 -> Orientation.ORIENTATION_0
                Surface.ROTATION_90 -> Orientation.ORIENTATION_90
                Surface.ROTATION_180 -> Orientation.ORIENTATION_180
                Surface.ROTATION_270 -> Orientation.ORIENTATION_270

                else -> throw Exception("Don't know how to convert $sdkRotation to Orientation")
            }
        }

    init {
        IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)

            ctx.registerReceiver(broadcastReceiver, this)
        }
    }

}