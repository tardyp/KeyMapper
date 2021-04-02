package io.github.sds100.keymapper.framework

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.core.content.getSystemService
import io.github.sds100.keymapper.framework.service.ObserveEnabledAccessibilityServicesJob

/**
 * Created by sds100 on 02/04/2021.
 */
object JobSchedulerHelper {

    private const val ID_OBSERVE_ACCESSIBILITY_SERVICES = 123

    @RequiresApi(Build.VERSION_CODES.N)
    fun observeEnabledAccessibilityServices(ctx: Context) {
        val uri = Settings.Secure.getUriFor(Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)

        val contentUri = JobInfo.TriggerContentUri(
            uri,
            JobInfo.TriggerContentUri.FLAG_NOTIFY_FOR_DESCENDANTS
        )

        val builder = JobInfo.Builder(
            ID_OBSERVE_ACCESSIBILITY_SERVICES,
            ComponentName(ctx, ObserveEnabledAccessibilityServicesJob::class.java)
        )
            .addTriggerContentUri(contentUri)
            .setTriggerContentUpdateDelay(500)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            builder.setImportantWhileForeground(true)
        }

        ctx.getSystemService<JobScheduler>()?.schedule(builder.build())
    }
}