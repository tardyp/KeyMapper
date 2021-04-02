package io.github.sds100.keymapper.domain.constraints

import android.Manifest
import io.github.sds100.keymapper.Constants
import io.github.sds100.keymapper.domain.adapter.PermissionAdapter
import io.github.sds100.keymapper.domain.adapter.SystemFeatureAdapter
import io.github.sds100.keymapper.domain.packages.PackageManagerAdapter
import io.github.sds100.keymapper.util.result.Error
import io.github.sds100.keymapper.util.result.FixableError

/**
 * Created by sds100 on 20/03/2021.
 */

class GetConstraintErrorUseCaseImpl(
    private val packageManager: PackageManagerAdapter,
    private val permissionAdapter: PermissionAdapter,
    systemFeatureAdapter: SystemFeatureAdapter,
) : GetConstraintErrorUseCase {
    private val isSupportedByDevice =
        IsConstraintSupportedByDeviceUseCaseImpl(systemFeatureAdapter)

    override fun invoke(constraint: Constraint): Error? {
        isSupportedByDevice.invoke(constraint)?.let { return it }

        when (constraint) {
            is Constraint.AppInForeground -> return getAppError(constraint.packageName)
            is Constraint.AppNotInForeground -> return getAppError(constraint.packageName)

            is Constraint.AppPlayingMedia -> {
                if (!permissionAdapter.isGranted(Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE)) {
                    return FixableError.PermissionDenied(Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE)
                }

                return getAppError(constraint.packageName)
            }

            is Constraint.OrientationCustom,
            Constraint.OrientationLandscape,
            Constraint.OrientationPortrait ->
                if (!permissionAdapter.isGranted(Manifest.permission.WRITE_SETTINGS)) {
                    return FixableError.PermissionDenied(Manifest.permission.WRITE_SETTINGS)
                }


            Constraint.ScreenOff,
            Constraint.ScreenOn -> {
                if (!permissionAdapter.isGranted(Constants.PERMISSION_ROOT)) {
                    return FixableError.PermissionDenied(Constants.PERMISSION_ROOT)
                }
            }
        }

        return null
    }

    private fun getAppError(packageName: String): Error? {
        if (!packageManager.isAppEnabled(packageName)) {
            return FixableError.AppDisabled(packageName)
        }

        if (!packageManager.isAppInstalled(packageName)) {
            return FixableError.AppNotFound(packageName)
        }

        return null
    }
}

interface GetConstraintErrorUseCase {
    operator fun invoke(constraint: Constraint): Error?
}