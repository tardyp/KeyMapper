package io.github.sds100.keymapper.domain.adapter

/**
 * Created by sds100 on 16/03/2021.
 */
interface PermissionAdapter {
    fun isGranted(permission: String): Boolean
}