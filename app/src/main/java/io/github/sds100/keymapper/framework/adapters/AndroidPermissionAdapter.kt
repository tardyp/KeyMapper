package io.github.sds100.keymapper.framework.adapters

import android.content.Context
import io.github.sds100.keymapper.domain.adapter.PermissionAdapter

/**
 * Created by sds100 on 17/03/2021.
 */
class AndroidPermissionAdapter(context: Context) : PermissionAdapter {
    private val ctx = context.applicationContext

    override fun isGranted(permission: String): Boolean {
        TODO("Not yet implemented")
    }
}