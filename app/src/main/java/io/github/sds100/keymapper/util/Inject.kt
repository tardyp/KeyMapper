package io.github.sds100.keymapper.util

import android.content.Context
import io.github.sds100.keymapper.KeyMapperApp
import io.github.sds100.keymapper.ServiceLocator
import io.github.sds100.keymapper.UseCases
import io.github.sds100.keymapper.actions.CreateSystemActionUseCaseImpl
import io.github.sds100.keymapper.backup.BackupRestoreMappingsUseCaseImpl
import io.github.sds100.keymapper.data.viewmodel.*
import io.github.sds100.keymapper.devices.ChooseBluetoothDeviceUseCaseImpl
import io.github.sds100.keymapper.domain.actions.TestActionUseCaseImpl
import io.github.sds100.keymapper.domain.mappings.fingerprintmap.AreFingerprintGesturesSupportedUseCaseImpl
import io.github.sds100.keymapper.domain.mappings.fingerprintmap.ConfigFingerprintMapUseCaseImpl
import io.github.sds100.keymapper.domain.mappings.fingerprintmap.GetFingerprintMapUseCaseImpl
import io.github.sds100.keymapper.domain.mappings.fingerprintmap.SaveFingerprintMapUseCaseImpl
import io.github.sds100.keymapper.domain.mappings.keymap.ConfigKeyMapUseCaseImpl
import io.github.sds100.keymapper.domain.mappings.keymap.GetKeymapUseCaseImpl
import io.github.sds100.keymapper.domain.mappings.keymap.SaveKeymapUseCaseImpl
import io.github.sds100.keymapper.domain.settings.ConfigSettingsUseCaseImpl
import io.github.sds100.keymapper.domain.usecases.*
import io.github.sds100.keymapper.home.ShowHomeScreenAlertsUseCaseImpl
import io.github.sds100.keymapper.inputmethod.ShowInputMethodPickerUseCaseImpl
import io.github.sds100.keymapper.mappings.fingerprintmaps.ListFingerprintMapsUseCaseImpl
import io.github.sds100.keymapper.mappings.keymaps.ListKeyMapsUseCaseImpl
import io.github.sds100.keymapper.onboarding.AppIntroSlide
import io.github.sds100.keymapper.onboarding.AppIntroUseCaseImpl
import io.github.sds100.keymapper.packages.DisplayAppShortcutsUseCaseImpl
import io.github.sds100.keymapper.service.AccessibilityServiceController
import io.github.sds100.keymapper.service.MyAccessibilityService
import io.github.sds100.keymapper.ui.NotificationController
import io.github.sds100.keymapper.ui.mappings.fingerprintmap.ConfigFingerprintMapViewModel
import io.github.sds100.keymapper.ui.mappings.keymap.ConfigKeyMapViewModel
import io.github.sds100.keymapper.util.delegate.ActionPerformerDelegate

/**
 * Created by sds100 on 26/01/2020.
 */

object Inject {

    fun chooseAppViewModel(context: Context): ChooseAppViewModel.Factory {
        return ChooseAppViewModel.Factory(
            UseCases.displayPackages(context)
        )
    }

    fun chooseAppShortcutViewModel(context: Context): ChooseAppShortcutViewModel.Factory {
        return ChooseAppShortcutViewModel.Factory(
            DisplayAppShortcutsUseCaseImpl(
                ServiceLocator.appShortcutAdapter(context)
            ),
            ServiceLocator.resourceProvider(context)
        )
    }

    fun chooseConstraintListViewModel(ctx: Context): ChooseConstraintViewModel.Factory {
        return ChooseConstraintViewModel.Factory(ServiceLocator.resourceProvider(ctx))
    }

    fun keyActionTypeViewModel(): KeyActionTypeViewModel.Factory {
        return KeyActionTypeViewModel.Factory()
    }

    fun configKeyEventViewModel(
        context: Context
    ): ConfigKeyEventViewModel.Factory {
        return ConfigKeyEventViewModel.Factory(
            UseCases.getInputDevices(context),
            ServiceLocator.resourceProvider(context)
        )
    }

    fun chooseKeyCodeViewModel(): ChooseKeyCodeViewModel.Factory {
        return ChooseKeyCodeViewModel.Factory()
    }

    fun configIntentViewModel(): ConfigIntentViewModel.Factory {
        return ConfigIntentViewModel.Factory()
    }

    fun textBlockActionTypeViewModel(): TextBlockActionTypeViewModel.Factory {
        return TextBlockActionTypeViewModel.Factory()
    }

    fun urlActionTypeViewModel(): UrlActionTypeViewModel.Factory {
        return UrlActionTypeViewModel.Factory()
    }

    fun tapCoordinateActionTypeViewModel(context: Context): PickDisplayCoordinateViewModel.Factory {
        return PickDisplayCoordinateViewModel.Factory(
            ServiceLocator.resourceProvider(context)
        )
    }

    fun systemActionListViewModel(ctx: Context): SystemActionListViewModel.Factory {
        return SystemActionListViewModel.Factory(
            CreateSystemActionUseCaseImpl(
                ServiceLocator.systemFeatureAdapter(ctx),
                ServiceLocator.packageManagerAdapter(ctx),
                ServiceLocator.inputMethodAdapter(ctx)
            ),

            ServiceLocator.resourceProvider(ctx)
        )
    }

    fun unsupportedActionListViewModel(
        context: Context
    ): UnsupportedActionListViewModel.Factory {
        return UnsupportedActionListViewModel.Factory(
            UseCases.isSystemActionSupported(context),
            ServiceLocator.resourceProvider(context)
        )
    }

    fun onlineFileViewModel(
        context: Context,
        fileUrl: String,
        alternateUrl: String? = null,
        header: String
    ): OnlineFileViewModel.Factory {
        return OnlineFileViewModel.Factory(
            ServiceLocator.fileRepository(context),
            fileUrl,
            alternateUrl,
            header
        )
    }

    fun configKeyMapViewModel(
        ctx: Context
    ): ConfigKeyMapViewModel.Factory {
        val configKeymapUseCase =
            ConfigKeyMapUseCaseImpl(ServiceLocator.externalDevicesAdapter(ctx))

        return ConfigKeyMapViewModel.Factory(
            SaveKeymapUseCaseImpl(ServiceLocator.roomKeymapRepository(ctx)),
            GetKeymapUseCaseImpl(ServiceLocator.roomKeymapRepository(ctx)),
            configKeymapUseCase,
            TestActionUseCaseImpl(),
            UseCases.onboarding(ctx),
            (ctx.applicationContext as KeyMapperApp).recordTriggerController,
            UseCases.createKeymapShortcut(ctx),
            UseCases.displayKeyMap(ctx),
            ServiceLocator.resourceProvider(ctx)
        )
    }

    fun configFingerprintMapViewModel(
        ctx: Context
    ): ConfigFingerprintMapViewModel.Factory {
        val configUseCase = ConfigFingerprintMapUseCaseImpl()

        return ConfigFingerprintMapViewModel.Factory(
            SaveFingerprintMapUseCaseImpl(ServiceLocator.fingerprintMapRepository(ctx)),
            GetFingerprintMapUseCaseImpl(ServiceLocator.fingerprintMapRepository(ctx)),
            configUseCase,
            TestActionUseCaseImpl(),
            UseCases.displaySimpleMapping(ctx),
            ServiceLocator.resourceProvider(ctx)
        )
    }

    fun createActionShortcutViewModel(
        context: Context
    ): CreateKeymapShortcutViewModel.Factory {
        return CreateKeymapShortcutViewModel.Factory(
            ServiceLocator.defaultKeymapRepository(context),
            UseCases.getActionError(context)
        )
    }

    fun homeViewModel(ctx: Context): HomeViewModel.Factory {
        return HomeViewModel.Factory(
            ListKeyMapsUseCaseImpl(
                ServiceLocator.roomKeymapRepository(ctx),
                ServiceLocator.backupManager(ctx),
                UseCases.displayKeyMap(ctx)
            ),
            ListFingerprintMapsUseCaseImpl(
                ServiceLocator.fingerprintMapRepository(ctx),
                ServiceLocator.backupManager(ctx),
                ServiceLocator.preferenceRepository(ctx),
                UseCases.displaySimpleMapping(ctx)
            ),
            UseCases.pauseMappings(ctx),
            BackupRestoreMappingsUseCaseImpl(ServiceLocator.backupManager(ctx)),
            ShowHomeScreenAlertsUseCaseImpl(
                ServiceLocator.preferenceRepository(ctx),
                ServiceLocator.permissionAdapter(ctx),
                UseCases.controlAccessibilityService(ctx)
            ),
           UseCases.showImePicker(ctx),
            UseCases.onboarding(ctx),
            ServiceLocator.resourceProvider(ctx)
        )
    }

    fun keyMapperAppViewModel(context: Context): ApplicationViewModel {
        val preferenceRepository = ServiceLocator.preferenceRepository(context)
        val keyboardController = ServiceLocator.inputMethodAdapter(context)
        val bluetoothMonitor = ServiceLocator.bluetoothMonitor(context)

        return ApplicationViewModel(
            GetThemeUseCase(preferenceRepository),
            ControlKeyboardOnToggleKeymapsUseCaseImpl(
                keyboardController,
                preferenceRepository
            ),
            ControlKeyboardOnBluetoothEventUseCaseImpl(
                keyboardController,
                preferenceRepository,
                bluetoothMonitor
            )
        )
    }

    fun settingsViewModel(context: Context): SettingsViewModel.Factory {
        return SettingsViewModel.Factory(
            ConfigSettingsUseCaseImpl(ServiceLocator.preferenceRepository(context)),
            UseCases.checkRootPermission(context)
        )
    }

    fun appIntroViewModel(
        context: Context,
        slides: List<AppIntroSlide>
    ): AppIntroViewModel.Factory {
        return AppIntroViewModel.Factory(
            AppIntroUseCaseImpl(
                ServiceLocator.permissionAdapter(context),
                ServiceLocator.serviceAdapter(context),
                ServiceLocator.systemFeatureAdapter(context),
                ServiceLocator.preferenceRepository(context),
                UseCases.fingerprintGesturesSupported(context)
            ),
            slides,
            ServiceLocator.resourceProvider(context)
        )
    }

    fun performActionsDelegate(service: MyAccessibilityService): ActionPerformerDelegate {
        return ActionPerformerDelegate(
            context = service,
            iAccessibilityService = service,
            lifecycle = service.lifecycle,
            performActionsUseCase =
            PerformActionsUseCaseImpl(ServiceLocator.preferenceRepository(service))
        )
    }

    fun accessibilityServiceController(service: MyAccessibilityService)
        : AccessibilityServiceController {
        val preferenceRepository = ServiceLocator.preferenceRepository(service)

        return AccessibilityServiceController(
            lifecycleOwner = service,
            constraintState = service,
            fingerprintGestureDetectionState = service,
            clock = service,
            actionError = service,
            getKeymapsPaused = GetKeymapsPausedUseCase(preferenceRepository),
            detectKeymapsUseCase = DetectKeymapsUseCaseImpl(preferenceRepository),
            performActionsUseCase = PerformActionsUseCaseImpl(preferenceRepository),
            onboarding = UseCases.onboarding(service),
            fingerprintMapRepository = ServiceLocator.fingerprintMapRepository(service),
            keymapRepository = ServiceLocator.defaultKeymapRepository(service),
            areFingerprintGesturesSupported = AreFingerprintGesturesSupportedUseCaseImpl(
                ServiceLocator.preferenceRepository(service)
            ),
            preferenceRepository = ServiceLocator.preferenceRepository(service)
        )
    }

    fun chooseBluetoothDeviceViewModel(ctx: Context): ChooseBluetoothDeviceViewModel.Factory {
        return ChooseBluetoothDeviceViewModel.Factory(
            ChooseBluetoothDeviceUseCaseImpl(ServiceLocator.externalDevicesAdapter(ctx)),
            ServiceLocator.resourceProvider(ctx)
        )
    }
}