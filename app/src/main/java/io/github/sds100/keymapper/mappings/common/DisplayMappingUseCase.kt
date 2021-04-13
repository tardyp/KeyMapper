package io.github.sds100.keymapper.mappings.common

import android.graphics.drawable.Drawable
import android.os.Build
import io.github.sds100.keymapper.domain.actions.*
import io.github.sds100.keymapper.domain.adapter.*
import io.github.sds100.keymapper.domain.constraints.Constraint
import io.github.sds100.keymapper.domain.constraints.IsConstraintSupportedByDeviceUseCaseImpl
import io.github.sds100.keymapper.domain.ime.KeyMapperImeHelper
import io.github.sds100.keymapper.domain.packages.PackageManagerAdapter
import io.github.sds100.keymapper.domain.utils.CameraLens
import io.github.sds100.keymapper.permissions.Permission
import io.github.sds100.keymapper.util.SystemActionUtils
import io.github.sds100.keymapper.util.result.Error
import io.github.sds100.keymapper.util.result.FixableError
import io.github.sds100.keymapper.util.result.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge

/**
 * Created by sds100 on 03/04/2021.
 */

class DisplaySimpleMappingUseCaseImpl(
    private val packageManager: PackageManagerAdapter,
    private val permissionAdapter: PermissionAdapter,
    private val inputMethodAdapter: InputMethodAdapter,
    private val systemFeatureAdapter: SystemFeatureAdapter,
    private val cameraAdapter: CameraAdapter,
    private val serviceAdapter: ServiceAdapter,
) : DisplaySimpleMappingUseCase {

    private val isSystemActionSupported = IsSystemActionSupportedUseCaseImpl(systemFeatureAdapter)
    private val keyMapperImeHelper = KeyMapperImeHelper(inputMethodAdapter)

    private val isConstraintSupportedByDevice =
        IsConstraintSupportedByDeviceUseCaseImpl(systemFeatureAdapter)

    override val invalidateErrors: Flow<Unit> =
        merge(
            inputMethodAdapter.chosenIme.drop(1).map { }, //dont collect the initial value
            permissionAdapter.onPermissionsUpdate
        )

    override fun getAppName(packageName: String): Result<String> =
        packageManager.getAppName(packageName)

    override fun getAppIcon(packageName: String): Result<Drawable> =
        packageManager.getAppIcon(packageName)

    override fun getInputMethodLabel(imeId: String): Result<String> =
        inputMethodAdapter.getLabel(imeId)

    override fun fixError(error: FixableError) {
        when (error) {
            FixableError.AccessibilityServiceDisabled -> serviceAdapter.enableService()
            is FixableError.AppDisabled -> packageManager.enableApp(error.packageName)
            is FixableError.AppNotFound -> packageManager.installApp(error.packageName)
            FixableError.NoCompatibleImeChosen -> keyMapperImeHelper.chooseCompatibleInputMethod(
                fromForeground = true
            )
            FixableError.NoCompatibleImeEnabled -> keyMapperImeHelper.enableCompatibleInputMethods()
            is FixableError.PermissionDenied -> permissionAdapter.request(error.permission)
        }
    }

    //TODO move this to its own use case and displaysimplemapping should take this use case as a param
    override fun getActionError(actionData: ActionData): Error? {
        if (actionData.requiresImeToPerform()) {
            if (!keyMapperImeHelper.isCompatibleImeEnabled()) {
                return FixableError.NoCompatibleImeEnabled
            }

            if (!keyMapperImeHelper.isCompatibleImeChosen()) {
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
                    && !permissionAdapter.isGranted(Permission.ROOT)
                ) {
                    FixableError.PermissionDenied(Permission.ROOT)
                }

            is TapCoordinateAction ->
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                    return Error.SdkVersionTooLow(Build.VERSION_CODES.N)
                }

            is PhoneCallAction ->
                if (!permissionAdapter.isGranted(Permission.CALL_PHONE)) {
                    return FixableError.PermissionDenied(Permission.CALL_PHONE)
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
                if (!inputMethodAdapter.isImeEnabledById(this.imeId)) {
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

interface DisplaySimpleMappingUseCase : DisplayActionUseCase, DisplayConstraintUseCase

interface DisplayActionUseCase {
    val invalidateErrors: Flow<Unit>

    fun getAppName(packageName: String): Result<String>
    fun getAppIcon(packageName: String): Result<Drawable>
    fun getInputMethodLabel(imeId: String): Result<String>
    fun getActionError(actionData: ActionData): Error?
    fun fixError(error: FixableError)
}

interface DisplayConstraintUseCase {
    val invalidateErrors: Flow<Unit>

    fun getAppName(packageName: String): Result<String>
    fun getAppIcon(packageName: String): Result<Drawable>
    fun getInputMethodLabel(imeId: String): Result<String>
    fun getConstraintError(constraint: Constraint): Error?
    fun fixError(error: FixableError)
}