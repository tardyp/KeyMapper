package io.github.sds100.keymapper.domain.permissions

import android.Manifest
import android.os.Build
import io.github.sds100.keymapper.domain.adapter.PermissionAdapter

/**
 * Created by sds100 on 31/03/2021.
 */

//TODO delete
class IsDoNotDisturbAccessGrantedImpl(
    private val adapter: PermissionAdapter
) : IsDoNotDisturbAccessGrantedUseCase {
    override fun invoke(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            adapter.isGranted(Manifest.permission.ACCESS_NOTIFICATION_POLICY)
        } else {
            true
        }
    }
}

interface IsDoNotDisturbAccessGrantedUseCase {
    operator fun invoke(): Boolean
}