package io.github.sds100.keymapper

import android.content.Context
import io.github.sds100.keymapper.domain.actions.GetActionErrorUseCaseImpl
import io.github.sds100.keymapper.domain.actions.IsSystemActionSupportedUseCaseImpl
import io.github.sds100.keymapper.domain.devices.GetInputDevicesUseCaseImpl
import io.github.sds100.keymapper.domain.mappings.fingerprintmap.AreFingerprintGesturesSupportedUseCaseImpl
import io.github.sds100.keymapper.domain.usecases.OnboardingUseCaseImpl
import io.github.sds100.keymapper.inputmethod.ShowInputMethodPickerUseCase
import io.github.sds100.keymapper.inputmethod.ShowInputMethodPickerUseCaseImpl
import io.github.sds100.keymapper.inputmethod.ToggleCompatibleImeUseCaseImpl
import io.github.sds100.keymapper.mappings.DisplaySimpleMappingUseCase
import io.github.sds100.keymapper.mappings.DisplaySimpleMappingUseCaseImpl
import io.github.sds100.keymapper.mappings.PauseMappingsUseCaseImpl
import io.github.sds100.keymapper.mappings.keymaps.DisplayKeyMapUseCase
import io.github.sds100.keymapper.mappings.keymaps.DisplayKeyMapUseCaseImpl
import io.github.sds100.keymapper.packages.DisplayAppsUseCase
import io.github.sds100.keymapper.packages.DisplayAppsUseCaseImpl
import io.github.sds100.keymapper.permissions.CheckRootPermissionUseCase
import io.github.sds100.keymapper.permissions.CheckRootPermissionUseCaseImpl
import io.github.sds100.keymapper.service.ControlAccessibilityServiceUseCase
import io.github.sds100.keymapper.service.ControlAccessibilityServiceUseCaseImpl
import io.github.sds100.keymapper.ui.shortcuts.CreateKeyMapShortcutUseCaseImpl

/**
 * Created by sds100 on 03/03/2021.
 */
object UseCases {

    fun displayPackages(ctx: Context): DisplayAppsUseCase =
        DisplayAppsUseCaseImpl(
            ServiceLocator.packageManagerAdapter(ctx)
        )

    fun displayKeyMap(ctx: Context): DisplayKeyMapUseCase {
        return DisplayKeyMapUseCaseImpl(
            ServiceLocator.permissionAdapter(ctx),
            displaySimpleMapping(ctx)
        )
    }

    fun displaySimpleMapping(ctx: Context): DisplaySimpleMappingUseCase {
        return DisplaySimpleMappingUseCaseImpl(
            ServiceLocator.packageManagerAdapter(ctx),
            ServiceLocator.permissionAdapter(ctx),
            ServiceLocator.inputMethodAdapter(ctx),
            ServiceLocator.systemFeatureAdapter(ctx),
            ServiceLocator.cameraAdapter(ctx),
            ServiceLocator.serviceAdapter(ctx)
        )
    }

    fun getActionError(ctx: Context) = GetActionErrorUseCaseImpl(
        ServiceLocator.packageManagerAdapter(ctx),
        ServiceLocator.inputMethodAdapter(ctx),
        ServiceLocator.permissionAdapter(ctx),
        ServiceLocator.systemFeatureAdapter(ctx),
        ServiceLocator.cameraAdapter(ctx)
    )

    fun onboarding(ctx: Context) = OnboardingUseCaseImpl(ServiceLocator.preferenceRepository(ctx))

    fun getInputDevices(ctx: Context) = GetInputDevicesUseCaseImpl(
        ServiceLocator.deviceInfoRepository(ctx),
        ServiceLocator.preferenceRepository(ctx),
        ServiceLocator.externalDevicesAdapter(ctx)
    )

    fun createKeymapShortcut(ctx: Context) = CreateKeyMapShortcutUseCaseImpl(
        ServiceLocator.appShortcutAdapter(ctx),
        displayKeyMap(ctx),
        ServiceLocator.resourceProvider(ctx)
    )

    fun isSystemActionSupported(ctx: Context) =
        IsSystemActionSupportedUseCaseImpl(ServiceLocator.systemFeatureAdapter(ctx))

    fun fingerprintGesturesSupported(ctx: Context) =
        AreFingerprintGesturesSupportedUseCaseImpl(ServiceLocator.preferenceRepository(ctx))

    fun pauseMappings(ctx: Context) =
        PauseMappingsUseCaseImpl(ServiceLocator.preferenceRepository(ctx))

    fun checkRootPermission(ctx: Context): CheckRootPermissionUseCase {
        return CheckRootPermissionUseCaseImpl(
            ServiceLocator.preferenceRepository(ctx)
        )
    }

    fun showImePicker(ctx: Context): ShowInputMethodPickerUseCase {
        return ShowInputMethodPickerUseCaseImpl(
            ServiceLocator.inputMethodAdapter(ctx)
        )
    }

    fun controlAccessibilityService(ctx: Context): ControlAccessibilityServiceUseCase {
        return ControlAccessibilityServiceUseCaseImpl(
            ServiceLocator.serviceAdapter(ctx)
        )
    }

    fun toggleCompatibleIme(ctx: Context) =
        ToggleCompatibleImeUseCaseImpl(
            ServiceLocator.inputMethodAdapter(ctx)
        )
}