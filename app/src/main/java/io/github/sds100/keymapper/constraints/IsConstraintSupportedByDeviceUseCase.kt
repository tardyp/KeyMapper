package io.github.sds100.keymapper.constraints

import android.content.pm.PackageManager
import io.github.sds100.keymapper.system.permissions.SystemFeatureAdapter
import io.github.sds100.keymapper.util.result.Error

/**
 * Created by sds100 on 16/03/2021.
 */

class IsConstraintSupportedByDeviceUseCaseImpl(
    private val systemFeatureAdapter: SystemFeatureAdapter
) : IsConstraintSupportedByDeviceUseCase {

    override fun invoke(constraint: Constraint): Error? {
        when (constraint) {
            is Constraint.BtDeviceConnected,
            is Constraint.BtDeviceDisconnected ->
                if (!systemFeatureAdapter.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)) {
                    return Error.SystemFeatureNotSupported(PackageManager.FEATURE_BLUETOOTH)
                }
        }

        return null
    }
}

interface IsConstraintSupportedByDeviceUseCase {
    operator fun invoke(constraint: Constraint): Error?
}