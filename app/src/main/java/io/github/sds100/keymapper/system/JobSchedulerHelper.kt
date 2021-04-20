package io.github.sds100.keymapper.system

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.core.content.getSystemService
import io.github.sds100.keymapper.system.accessibility.ObserveEnabledAccessibilityServicesJob
import io.github.sds100.keymapper.system.inputmethod.ObserveEnabledInputMethodsJob

/**
 * Created by sds100 on 02/04/2021.
 */
object JobSchedulerHelper {

    private const val ID_OBSERVE_ACCESSIBILITY_SERVICES = 1
    private const val ID_OBSERVE_ENABLED_INPUT_METHODS = 2

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

    @RequiresApi(Build.VERSION_CODES.N)
    fun observeEnabledInputMethods(ctx: Context) {
        val uri = Settings.Secure.getUriFor(Settings.Secure.ENABLED_INPUT_METHODS)

        val contentUri = JobInfo.TriggerContentUri(
            uri,
            JobInfo.TriggerContentUri.FLAG_NOTIFY_FOR_DESCENDANTS
        )

        val builder = JobInfo.Builder(
            ID_OBSERVE_ENABLED_INPUT_METHODS,
            ComponentName(ctx, ObserveEnabledInputMethodsJob::class.java)
        )
            .addTriggerContentUri(contentUri)
            .setTriggerContentUpdateDelay(500)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            builder.setImportantWhileForeground(true)
        }

        ctx.getSystemService<JobScheduler>()?.schedule(builder.build())
    }
}