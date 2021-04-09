package io.github.sds100.keymapper.framework.service

import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.os.Build
import io.github.sds100.keymapper.KeyMapperApp
import io.github.sds100.keymapper.framework.JobSchedulerHelper

/**
 * Created by sds100 on 02/04/2021.
 */
class ObserveEnabledInputMethodsJob : JobService() {
    override fun onStartJob(params: JobParameters?): Boolean {
        (applicationContext as KeyMapperApp).inputMethodAdapter.onEnabledInputMethodsUpdate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            JobSchedulerHelper.observeEnabledInputMethods(applicationContext)
        }
        return false
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        return false
    }
}