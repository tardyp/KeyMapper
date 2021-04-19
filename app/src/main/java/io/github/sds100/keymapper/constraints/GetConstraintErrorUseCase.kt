package io.github.sds100.keymapper.constraints

import android.os.Build
import io.github.sds100.keymapper.domain.actions.*
import io.github.sds100.keymapper.domain.adapter.InputMethodAdapter
import io.github.sds100.keymapper.domain.adapter.PermissionAdapter
import io.github.sds100.keymapper.domain.adapter.ServiceAdapter
import io.github.sds100.keymapper.domain.adapter.SystemFeatureAdapter
import io.github.sds100.keymapper.domain.constraints.Constraint
import io.github.sds100.keymapper.domain.constraints.IsConstraintSupportedByDeviceUseCaseImpl
import io.github.sds100.keymapper.domain.ime.KeyMapperImeHelper
import io.github.sds100.keymapper.domain.packages.PackageManagerAdapter
import io.github.sds100.keymapper.permissions.Permission
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