package io.github.sds100.keymapper.constraints

import io.github.sds100.keymapper.system.permissions.PermissionAdapter
import io.github.sds100.keymapper.system.permissions.SystemFeatureAdapter
import io.github.sds100.keymapper.system.apps.PackageManagerAdapter
import io.github.sds100.keymapper.system.permissions.Permission
import io.github.sds100.keymapper.util.result.Error
import io.github.sds100.keymapper.util.result.FixableError
import kotlinx.coroutines.flow.Flow

/**
 * Created by sds100 on 17/04/2021.
 */

class GetConstraintErrorUseCaseImpl(
    private val packageManager: PackageManagerAdapter,
    private val permissionAdapter: PermissionAdapter,
    private val systemFeatureAdapter: SystemFeatureAdapter,
):GetConstraintErrorUseCase{

    override val invalidateErrors: Flow<Unit> = permissionAdapter.onPermissionsUpdate

    private val isConstraintSupportedByDevice =
        IsConstraintSupportedByDeviceUseCaseImpl(systemFeatureAdapter)

    override fun getConstraintError(constraint: Constraint): Error? {
        isConstraintSupportedByDevice(constraint)?.let { return it }

        when (constraint) {
            is Constraint.AppInForeground -> return getAppError(constraint.packageName)
            is Constraint.AppNotInForeground -> return getAppError(constraint.packageName)

            is Constraint.AppPlayingMedia -> {
                if (!permissionAdapter.isGranted(Permission.NOTIFICATION_LISTENER)) {
                    return FixableError.PermissionDenied(Permission.NOTIFICATION_LISTENER)
                }

                return getAppError(constraint.packageName)
            }

            is Constraint.OrientationCustom,
            Constraint.OrientationLandscape,
            Constraint.OrientationPortrait ->
                if (!permissionAdapter.isGranted(Permission.WRITE_SETTINGS)) {
                    return FixableError.PermissionDenied(Permission.WRITE_SETTINGS)
                }


            Constraint.ScreenOff,
            Constraint.ScreenOn -> {
                if (!permissionAdapter.isGranted(Permission.ROOT)) {
                    return FixableError.PermissionDenied(Permission.ROOT)
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
    val invalidateErrors: Flow<Unit>

    fun getConstraintError(constraint: Constraint): Error?
}