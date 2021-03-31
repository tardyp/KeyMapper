package io.github.sds100.keymapper.domain.permissions

import io.github.sds100.keymapper.domain.adapter.PermissionAdapter

/**
 * Created by sds100 on 31/03/2021.
 */

class IsPermissionGrantedUseCaseImpl(
    private val adapter: PermissionAdapter
) : IsPermissionGrantedUseCase {
    override fun invoke(permission: String): Boolean {
        return adapter.isGranted(permission)
    }
}

interface IsPermissionGrantedUseCase {
    operator fun invoke(permission: String): Boolean
}