package io.github.sds100.keymapper.domain.constraints

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import io.github.sds100.keymapper.Constants
import io.github.sds100.keymapper.domain.adapter.PermissionAdapter
import io.github.sds100.keymapper.domain.adapter.SystemFeatureAdapter
import io.github.sds100.keymapper.util.result.Error
import io.github.sds100.keymapper.util.result.RecoverableError

/**
 * Created by sds100 on 16/03/2021.
 */

class IsConstraintSupportedUseCaseImpl(
    private val adapter: SystemFeatureAdapter,
    private val permissionAdapter: PermissionAdapter
) : IsConstraintSupportedUseCase {

    override fun invoke(constraint: Constraint): Error? {
        when (constraint) {
            is Constraint.BtDeviceConnected,
            is Constraint.BtDeviceDisconnected ->
                if (adapter.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)) {
                    return Error.SystemFeatureNotSupported(PackageManager.FEATURE_BLUETOOTH)
                }

            is Constraint.ScreenOff, Constraint.ScreenOn ->
                if (!permissionAdapter.isGranted(Constants.PERMISSION_ROOT)) {
                    return RecoverableError.PermissionDenied(Constants.PERMISSION_ROOT)
                }

            is Constraint.OrientationPortrait,
            is Constraint.OrientationLandscape,
            is Constraint.OrientationCustom ->
                if (!permissionAdapter.isGranted(Manifest.permission.WRITE_SETTINGS)) {
                    return RecoverableError.PermissionDenied(Manifest.permission.WRITE_SETTINGS)
                }

            is Constraint.AppPlayingMedia -> {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    return Error.SdkVersionTooLow(Build.VERSION_CODES.LOLLIPOP)
                }

                if (!permissionAdapter.isGranted(Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE)) {
                    return RecoverableError.PermissionDenied(Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE)
                }
            }
        }

        return null
    }
}

interface IsConstraintSupportedUseCase {
    operator fun invoke(constraint: Constraint): Error?
}