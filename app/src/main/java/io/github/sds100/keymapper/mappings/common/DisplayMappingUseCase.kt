package io.github.sds100.keymapper.mappings.common

import android.Manifest
import android.graphics.drawable.Drawable
import android.os.Build
import io.github.sds100.keymapper.Constants
import io.github.sds100.keymapper.domain.actions.*
import io.github.sds100.keymapper.domain.adapter.CameraAdapter
import io.github.sds100.keymapper.domain.adapter.InputMethodAdapter
import io.github.sds100.keymapper.domain.adapter.PermissionAdapter
import io.github.sds100.keymapper.domain.adapter.SystemFeatureAdapter
import io.github.sds100.keymapper.domain.constraints.Constraint
import io.github.sds100.keymapper.domain.constraints.IsConstraintSupportedByDeviceUseCaseImpl
import io.github.sds100.keymapper.domain.ime.KeyMapperImeManager
import io.github.sds100.keymapper.domain.packages.PackageManagerAdapter
import io.github.sds100.keymapper.domain.utils.CameraLens
import io.github.sds100.keymapper.util.SystemActionUtils
import io.github.sds100.keymapper.util.result.Error
import io.github.sds100.keymapper.util.result.FixableError
import io.github.sds100.keymapper.util.result.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Created by sds100 on 03/04/2021.
 */

class DisplaySimpleMappingUseCaseImpl(
    private val packageManager: PackageManagerAdapter,
    private val permissionAdapter: PermissionAdapter,
    private val inputMethodAdapter: InputMethodAdapter,
    private val systemFeatureAdapter: SystemFeatureAdapter,
    private val cameraAdapter: CameraAdapter
) : DisplaySimpleMappingUseCase {

    private val isSystemActionSupported = IsSystemActionSupportedUseCaseImpl(systemFeatureAdapter)
    private val keyMapperImeManager = KeyMapperImeManager(inputMethodAdapter)

    private val isConstraintSupportedByDevice =
        IsConstraintSupportedByDeviceUseCaseImpl(systemFeatureAdapter)

    override val invalidateErrors: Flow<Unit> = combine(inputMethodAdapter.chosenIme) {}

    override fun getAppName(packageName: String): Result<String> =
        packageManager.getAppName(packageName)

    override fun getAppIcon(packageName: String): Result<Drawable> =
        packageManager.getAppIcon(packageName)

    override fun getInputMethodLabel(imeId: String): Result<String> =
        inputMethodAdapter.getLabel(imeId)

    //TODO move this to its own use case and displaysimplemapping should take this use case as a param
    override fun getActionError(actionData: ActionData): Error? {
        if (actionData.requiresImeToPerform()) {
            if (!keyMapperImeManager.isCompatibleImeEnabled()) {
                return FixableError.NoCompatibleImeEnabled
            }

            if (!keyMapperImeManager.isCompatibleImeChosen()) {
                return FixableError.NoCompatibleImeChosen
            }
        }

        when (actionData) {
            is OpenAppAction -> {
                return getAppError(actionData.packageName)
            }

            is OpenAppShortcutAction -> {
                actionData.packageName ?: return null

                return getAppError(actionData.packageName)
            }

            is KeyEventAction ->
                if (
                    actionData.useShell
                    && !permissionAdapter.isGranted(Constants.PERMISSION_ROOT)
                ) {
                    FixableError.PermissionDenied(Constants.PERMISSION_ROOT)
                }

            is TapCoordinateAction ->
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                    return Error.SdkVersionTooLow(Build.VERSION_CODES.N)
                }

            is PhoneCallAction ->
                if (!permissionAdapter.isGranted(Manifest.permission.CALL_PHONE)) {
                    return FixableError.PermissionDenied(Manifest.permission.CALL_PHONE)
                }

            is SystemAction -> return actionData.getError()
        }

        return null
    }

    private fun SystemAction.getError(): Error? {
        isSystemActionSupported.invoke(this.id)?.let {
            return it
        }

        SystemActionUtils.getRequiredPermissions(this.id).forEach { permission ->
            if (!permissionAdapter.isGranted(permission)) {
                return FixableError.PermissionDenied(permission)
            }
        }

        when {
            id == SystemActionId.OPEN_VOICE_ASSISTANT -> if (packageManager.isVoiceAssistantInstalled()) {
                return Error.NoVoiceAssistant
            }

            this is FlashlightSystemAction ->
                if (!cameraAdapter.hasFlashFacing(this.lens)) {
                    return when (lens) {
                        CameraLens.FRONT -> Error.FrontFlashNotFound
                        CameraLens.BACK -> Error.BackFlashNotFound
                    }
                }

            this is SwitchKeyboardSystemAction ->
                if (!inputMethodAdapter.isImeEnabled(this.imeId)) {
                    return Error.ImeNotFound(this.savedImeName)
                }
        }

        return null
    }

    override fun getConstraintError(constraint: Constraint): Error? {
        isConstraintSupportedByDevice(constraint)?.let { return it }

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

interface DisplaySimpleMappingUseCase : DisplayActionUseCase, DisplayConstraintUseCase {
}

interface DisplayActionUseCase {
    val invalidateErrors: Flow<Unit>

    fun getAppName(packageName: String): Result<String>
    fun getAppIcon(packageName: String): Result<Drawable>
    fun getInputMethodLabel(imeId: String): Result<String>
    fun getActionError(actionData: ActionData): Error?
}

interface DisplayConstraintUseCase {
    val invalidateErrors: Flow<Unit>

    fun getAppName(packageName: String): Result<String>
    fun getAppIcon(packageName: String): Result<Drawable>
    fun getInputMethodLabel(imeId: String): Result<String>
    fun getConstraintError(constraint: Constraint): Error?
}