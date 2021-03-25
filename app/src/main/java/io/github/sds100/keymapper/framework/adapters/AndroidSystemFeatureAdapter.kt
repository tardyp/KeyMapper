package io.github.sds100.keymapper.framework.adapters

import android.content.Context
import io.github.sds100.keymapper.domain.adapter.SystemFeatureAdapter

/**
 * Created by sds100 on 17/03/2021.
 */
class AndroidSystemFeatureAdapter(context: Context) : SystemFeatureAdapter {
    private val ctx = context.applicationContext

    override fun hasSystemFeature(feature: String): Boolean {
        return ctx.packageManager.hasSystemFeature(feature)
    }
}