package io.github.sds100.keymapper.util

import android.content.Context
import io.github.sds100.keymapper.ServiceLocator
import io.github.sds100.keymapper.UseCases
import io.github.sds100.keymapper.data.viewmodel.*
import io.github.sds100.keymapper.domain.actions.GetActionErrorUseCaseImpl
import io.github.sds100.keymapper.domain.devices.SaveDeviceInfoUseCase
import io.github.sds100.keymapper.domain.devices.ShowDeviceInfoUseCaseImpl
import io.github.sds100.keymapper.domain.usecases.*
import io.github.sds100.keymapper.service.AccessibilityServiceController
import io.github.sds100.keymapper.service.MyAccessibilityService
import io.github.sds100.keymapper.ui.mappings.keymap.ConfigKeymapViewModel
import io.github.sds100.keymapper.ui.mappings.keymap.KeymapActionListItemMapper
import io.github.sds100.keymapper.ui.mappings.keymap.TriggerKeyListItemMapperImpl
import io.github.sds100.keymapper.util.delegate.ActionPerformerDelegate

/**
 * Created by sds100 on 26/01/2020.
 */

//TODO rename to Inject. remove provide prefix from functions
object InjectorUtils {

    fun provideAppListViewModel(context: Context): AppListViewModel.Factory {
        return AppListViewModel.Factory(ServiceLocator.packageRepository(context))
    }

    fun provideAppShortcutListViewModel(context: Context): AppShortcutListViewModel.Factory {
        return AppShortcutListViewModel.Factory(ServiceLocator.packageRepository(context))
    }

    fun provideKeymapListViewModel(context: Context): KeymapListViewModel.Factory {
        return KeymapListViewModel.Factory(
            ServiceLocator.keymapRepository(context),
            GetActionErrorUseCaseImpl(
                ServiceLocator.preferenceRepository(context),
                ServiceLocator.deviceInfoRepository(context)
            )
        )
    }

    fun provideBackupRestoreViewModel(context: Context): BackupRestoreViewModel.Factory {
        return BackupRestoreViewModel.Factory(ServiceLocator.backupManager(context))
    }

    fun provideChooseConstraintListViewModel(
        supportedConstraints: List<String>
    ): ChooseConstraintListViewModel.Factory {
        return ChooseConstraintListViewModel.Factory(supportedConstraints)
    }

    fun provideKeyActionTypeViewModel(): KeyActionTypeViewModel.Factory {
        return KeyActionTypeViewModel.Factory()
    }

    fun provideKeyEventActionTypeViewModel(
        context: Context
    ): KeyEventActionTypeViewModel.Factory {
        val deviceInfoRepository = ServiceLocator.deviceInfoRepository(context)
        return KeyEventActionTypeViewModel.Factory(
            ShowDeviceInfoUseCaseImpl(
                deviceInfoRepository,
                ServiceLocator.preferenceRepository(context)
            ),

            SaveDeviceInfoUseCase(deviceInfoRepository)
        )
    }

    fun provideKeycodeListViewModel(): KeycodeListViewModel.Factory {
        return KeycodeListViewModel.Factory()
    }

    fun provideIntentActionTypeViewModel(): IntentActionTypeViewModel.Factory {
        return IntentActionTypeViewModel.Factory()
    }

    fun provideTextBlockActionTypeViewModel(): TextBlockActionTypeViewModel.Factory {
        return TextBlockActionTypeViewModel.Factory()
    }

    fun provideUrlActionTypeViewModel(): UrlActionTypeViewModel.Factory {
        return UrlActionTypeViewModel.Factory()
    }

    fun provideTapCoordinateActionTypeViewModel(): TapCoordinateActionTypeViewModel.Factory {
        return TapCoordinateActionTypeViewModel.Factory()
    }

    fun provideSystemActionListViewModel(context: Context): SystemActionListViewModel.Factory {
        return SystemActionListViewModel.Factory(ServiceLocator.systemActionRepository(context))
    }

    fun provideUnsupportedActionListViewModel(
        context: Context
    ): UnsupportedActionListViewModel.Factory {
        return UnsupportedActionListViewModel.Factory(ServiceLocator.systemActionRepository(context))
    }

    fun provideKeymapActionOptionsViewModel(): KeymapActionOptionsViewModel.Factory {
        return KeymapActionOptionsViewModel.Factory()
    }

    fun provideFingerprintActionOptionsViewModel(): FingerprintActionOptionsViewModel.Factory {
        return FingerprintActionOptionsViewModel.Factory()
    }

    fun provideTriggerKeyOptionsViewModel(): TriggerKeyOptionsViewModel.Factory {
        return TriggerKeyOptionsViewModel.Factory()
    }

    fun provideOnlineViewModel(
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

    fun provideFingerprintMapListViewModel(context: Context): FingerprintMapListViewModel.Factory {
        return FingerprintMapListViewModel.Factory(
            ServiceLocator.fingerprintMapRepository(context),
            ShowDeviceInfoUseCaseImpl(
                ServiceLocator.deviceInfoRepository(context),
                ServiceLocator.preferenceRepository(context)
            ),
            ListFingerprintMapsUseCase(ServiceLocator.preferenceRepository(context))
        )
    }

    fun provideMenuFragmentViewModel(context: Context): MenuFragmentViewModel.Factory {
        return MenuFragmentViewModel.Factory(
            ServiceLocator.keymapRepository(context),
            ServiceLocator.fingerprintMapRepository(context),
            ControlKeymapsPausedState(ServiceLocator.preferenceRepository(context))
        )
    }

    fun provideConfigKeymapViewModel(
        context: Context
    ): ConfigKeymapViewModel.Factory {
        val configKeymapUseCase = UseCases.configKeymap(context)

        return ConfigKeymapViewModel.Factory(
            configKeymapUseCase,
            configKeymapUseCase.configActions,
            configKeymapUseCase.configTrigger,
            UseCases.getActionError(context),
            UseCases.testAction(context),
            UseCases.onboarding(context),
            KeymapActionListItemMapper(
                UseCases.getActionError(context),
                UseCases.showDeviceInfo(context),
                ServiceLocator.appInfoAdapter(context),
                ServiceLocator.resourceProvider(context)
            ),

            TriggerKeyListItemMapperImpl(
                ServiceLocator.resourceProvider(context),
                UseCases.showDeviceInfo(context)
            )
        )
    }

    fun provideConfigFingerprintMapViewModel(
        context: Context
    ): ConfigFingerprintMapViewModel.Factory {
        return ConfigFingerprintMapViewModel.Factory(
            ServiceLocator.fingerprintMapRepository(context),
            GetActionErrorUseCaseImpl(
                ServiceLocator.preferenceRepository(context),
                ServiceLocator.deviceInfoRepository(context)
            )
        )
    }

    fun provideCreateActionShortcutViewModel(
        context: Context
    ): CreateKeymapShortcutViewModel.Factory {
        return CreateKeymapShortcutViewModel.Factory(
            ServiceLocator.keymapRepository(context),
            UseCases.getActionError(context)
        )
    }

    fun provideHomeViewModel(context: Context): HomeViewModel.Factory {
        return HomeViewModel.Factory(UseCases.onboarding(context))
    }

    fun provideApplicationViewModel(context: Context): ApplicationViewModel {
        val preferenceRepository = ServiceLocator.preferenceRepository(context)
        val keyboardController = ServiceLocator.keyboardController(context)
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

    fun provideSettingsViewModel(context: Context): SettingsViewModel.Factory {
        return SettingsViewModel.Factory(
            ConfigSettingsUseCaseImpl(ServiceLocator.preferenceRepository(context))
        )
    }

    fun provideAppIntroViewModel(context: Context): AppIntroViewModel.Factory {
        return AppIntroViewModel.Factory(
            OnboardingUseCaseImpl(ServiceLocator.preferenceRepository(context))
        )
    }

    fun provideFingerprintGestureIntroViewModel(context: Context): FingerprintGestureMapIntroViewModel.Factory {
        return FingerprintGestureMapIntroViewModel.Factory(
            OnboardingUseCaseImpl(ServiceLocator.preferenceRepository(context))
        )
    }

    fun providePerformActionsDelegate(service: MyAccessibilityService): ActionPerformerDelegate {
        return ActionPerformerDelegate(
            context = service,
            iAccessibilityService = service,
            lifecycle = service.lifecycle,
            performActionsUseCase =
            PerformActionsUseCaseImpl(ServiceLocator.preferenceRepository(service))
        )
    }

    fun provideAccessibilityServiceController(service: MyAccessibilityService)
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
            keymapRepository = ServiceLocator.keymapRepository(service)
        )
    }
}