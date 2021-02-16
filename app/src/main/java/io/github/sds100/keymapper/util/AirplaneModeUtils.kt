package io.github.sds100.keymapper.util

import android.content.Context
import android.provider.Settings

/**
 * Created by sds100 on 16/06/2020.
 */

object AirplaneModeUtils {
    fun toggleAirplaneMode(ctx: Context, hasRootPermission: Boolean) {
        if (SettingsUtils.getGlobalSetting<Int>(ctx, Settings.Global.AIRPLANE_MODE_ON) == 0) {
            enableAirplaneMode(hasRootPermission)
        } else {
            disableAirplaneMode(hasRootPermission)
        }
    }

    fun enableAirplaneMode(hasRootPermission: Boolean) {
        if (!hasRootPermission) return

        RootUtils.executeRootCommand("settings put global airplane_mode_on 1")
        broadcastAirplaneModeChanged(true)
    }

    fun disableAirplaneMode(hasRootPermission: Boolean) {
        if (!hasRootPermission) return

        RootUtils.executeRootCommand("settings put global airplane_mode_on 0")
        broadcastAirplaneModeChanged(false)
    }

    private fun broadcastAirplaneModeChanged(enabled: Boolean) {
        RootUtils.executeRootCommand("am broadcast -a android.intent.action.AIRPLANE_MODE --ez state $enabled")
    }
}
