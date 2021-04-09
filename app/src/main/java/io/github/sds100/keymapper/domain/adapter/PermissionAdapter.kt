package io.github.sds100.keymapper.domain.adapter

import io.github.sds100.keymapper.permissions.Permission
import kotlinx.coroutines.flow.Flow

/**
 * Created by sds100 on 16/03/2021.
 */
interface PermissionAdapter {
    val onPermissionsUpdate: Flow<Unit>
    fun isGranted(permission: Permission): Boolean
}