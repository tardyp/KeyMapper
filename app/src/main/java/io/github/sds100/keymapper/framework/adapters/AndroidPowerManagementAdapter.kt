package io.github.sds100.keymapper.framework.adapters

import android.content.Context
import android.os.Build
import android.os.PowerManager
import androidx.core.content.getSystemService
import io.github.sds100.keymapper.Constants
import io.github.sds100.keymapper.domain.adapter.PowerManagementAdapter

/**
 * Created by sds100 on 02/04/2021.
 */
class AndroidPowerManagementAdapter(context: Context) : PowerManagementAdapter {
    private val ctx: Context = context.applicationContext

    override val isIgnoringBatteryOptimisation: Boolean
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ctx.getSystemService<PowerManager>()
                ?.isIgnoringBatteryOptimizations(Constants.PACKAGE_NAME) ?: false
        } else {
            true
        }
}