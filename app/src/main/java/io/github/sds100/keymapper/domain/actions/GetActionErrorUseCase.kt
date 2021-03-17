package io.github.sds100.keymapper.domain.actions

import android.Manifest
import android.os.Build
import io.github.sds100.keymapper.Constants
import io.github.sds100.keymapper.data.repository.DeviceInfoCache
import io.github.sds100.keymapper.domain.KeyMapperImeManager
import io.github.sds100.keymapper.domain.adapter.CameraAdapter
import io.github.sds100.keymapper.domain.adapter.InputMethodAdapter
import io.github.sds100.keymapper.domain.adapter.PermissionAdapter
import io.github.sds100.keymapper.domain.adapter.SystemFeatureAdapter
import io.github.sds100.keymapper.domain.packages.PackageManagerAdapter
import io.github.sds100.keymapper.domain.repositories.PreferenceRepository
import io.github.sds100.keymapper.domain.utils.CameraLens
import io.github.sds100.keymapper.util.SystemActionUtils
import io.github.sds100.keymapper.util.result.Error
import io.github.sds100.keymapper.util.result.RecoverableError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Created by sds100 on 15/02/2021.
 */
class GetActionErrorUseCaseImpl(
    private val preferenceRepository: PreferenceRepository,
    private val deviceInfoRepository: DeviceInfoCache,
    private val packageManager: PackageManagerAdapter,
    private val inputMethodAdapter: InputMethodAdapter,
    private val permissionAdapter: PermissionAdapter,
    private val systemFeatureAdapter: SystemFeatureAdapter,
    private val cameraAdapter: CameraAdapter
) : GetActionErrorUseCase {

    private val isSystemActionSupported = IsSystemActionSupportedUseCaseImpl(systemFeatureAdapter)
    private val keyMapperImeManager = KeyMapperImeManager(inputMethodAdapter)

    override val invalidateErrors = combine(inputMethodAdapter.chosenImePackageName) {}

    override fun getError(action: ActionData): Error? {
        if (action.requiresImeToPerform()) {
            if (!keyMapperImeManager.isCompatibleImeEnabled()) {
                return RecoverableError.NoCompatibleImeEnabled
            }

            if (keyMapperImeManager.isCompatibleImeChosen()) {
                return RecoverableError.NoCompatibleImeChosen
            }
        }

        when (action) {
            is AppAction -> {
                if (!packageManager.isAppEnabled(action.packageName)) {
                    return RecoverableError.AppDisabled(action.packageName)
                }

                if (!packageManager.isAppInstalled(action.packageName)) {
                    return RecoverableError.AppNotFound(action.packageName)
                }
            }

            is KeyEventAction ->
                if (
                    action.useShell
                    && !permissionAdapter.isGranted(Constants.PERMISSION_ROOT)
                ) {
                    RecoverableError.PermissionDenied(Constants.PERMISSION_ROOT)
                }

            is TapCoordinateAction ->
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                    return Error.SdkVersionTooLow(Build.VERSION_CODES.N)
                }

            is PhoneCallAction ->
                if (!permissionAdapter.isGranted(Manifest.permission.CALL_PHONE)) {
                    return RecoverableError.PermissionDenied(Manifest.permission.CALL_PHONE)
                }

            is SystemActionData -> action.getError()
        }

        return null
    }

    private fun SystemActionData.getError(): Error? {
        isSystemActionSupported.invoke(this)?.let {
            return it
        }

        SystemActionUtils.getRequiredPermissions(this).forEach { permission ->
            if (!permissionAdapter.isGranted(permission)) {
                return RecoverableError.PermissionDenied(permission)
            }
        }

        when {
            id == SystemActionId.OPEN_VOICE_ASSISTANT -> if (packageManager.isVoiceAssistantInstalled()) {
                return Error.NoVoiceAssistant
            }

            this is FlashlightSystemAction -> if (!cameraAdapter.hasFlashFacing(this.lens)) {
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
}

interface GetActionErrorUseCase {
    val invalidateErrors: Flow<Unit>
    fun getError(action: ActionData): Error?
}