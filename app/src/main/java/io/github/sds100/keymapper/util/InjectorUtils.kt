package io.github.sds100.keymapper.util

import android.content.Context
import io.github.sds100.keymapper.ServiceLocator
import io.github.sds100.keymapper.UseCases
import io.github.sds100.keymapper.data.viewmodel.*
import io.github.sds100.keymapper.domain.mappings.fingerprintmap.ConfigFingerprintMapUseCaseImpl
import io.github.sds100.keymapper.domain.mappings.fingerprintmap.GetFingerprintMapUseCaseImpl
import io.github.sds100.keymapper.domain.mappings.fingerprintmap.SaveFingerprintMapUseCaseImpl
import io.github.sds100.keymapper.domain.mappings.keymap.ConfigKeymapUseCaseImpl
import io.github.sds100.keymapper.domain.mappings.keymap.GetKeymapUseCaseImpl
import io.github.sds100.keymapper.domain.mappings.keymap.SaveKeymapUseCaseImpl
import io.github.sds100.keymapper.domain.trigger.RecordTriggerUseCaseImpl
import io.github.sds100.keymapper.domain.usecases.*
import io.github.sds100.keymapper.service.AccessibilityServiceController
import io.github.sds100.keymapper.service.MyAccessibilityService
import io.github.sds100.keymapper.ui.mappings.fingerprintmap.ConfigFingerprintMapViewModel
import io.github.sds100.keymapper.ui.mappings.fingerprintmap.FingerprintMapActionListItemMapper
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
        return AppListViewModel.Factory(
            ServiceLocator.appInfoAdapter(context),
            ServiceLocator.packageManagerAdapter(context)
        )
    }

    fun provideAppShortcutListViewModel(context: Context): AppShortcutListViewModel.Factory {
        return AppShortcutListViewModel.Factory(ServiceLocator.packageRepository(context))
    }

    fun provideKeymapListViewModel(context: Context): KeymapListViewModel.Factory {
        return KeymapListViewModel.Factory(
            ServiceLocator.keymapRepository(context),
            UseCases.getActionError(context)
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
            UseCases.showDeviceInfo(context)
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
            UseCases.showDeviceInfo(context),
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
        ctx: Context
    ): ConfigKeymapViewModel.Factory {
        val configKeymapUseCase = ConfigKeymapUseCaseImpl()

        return ConfigKeymapViewModel.Factory(
            SaveKeymapUseCaseImpl(ServiceLocator.keymapRepository(ctx)),
            GetKeymapUseCaseImpl(
                ServiceLocator.keymapRepository(ctx),
                ServiceLocator.externalDeviceAdapter(ctx)
            ),
            configKeymapUseCase,
            configKeymapUseCase.configActions,
            configKeymapUseCase.configTrigger,
            UseCases.getActionError(ctx),
            UseCases.testAction(ctx),
            UseCases.onboarding(ctx),
            RecordTriggerUseCaseImpl(),
            UseCases.showDeviceInfo(ctx),
            KeymapActionListItemMapper(
                UseCases.getActionError(ctx),
                ServiceLocator.appInfoAdapter(ctx),
                ServiceLocator.inputMethodAdapter(ctx),
                ServiceLocator.resourceProvider(ctx)
            ),

            TriggerKeyListItemMapperImpl(
                ServiceLocator.resourceProvider(ctx),
                UseCases.showDeviceInfo(ctx)
            )
        )
    }

    fun provideConfigFingerprintMapViewModel(
        ctx: Context
    ): ConfigFingerprintMapViewModel.Factory {
        val configUseCase = ConfigFingerprintMapUseCaseImpl()

        return ConfigFingerprintMapViewModel.Factory(
            SaveFingerprintMapUseCaseImpl(ServiceLocator.fingerprintMapRepository(ctx)),
            GetFingerprintMapUseCaseImpl(ServiceLocator.fingerprintMapRepository(ctx)),
            configUseCase,
            configUseCase.configActions,
            UseCases.getActionError(ctx),
            UseCases.testAction(ctx),
            FingerprintMapActionListItemMapper(
                UseCases.getActionError(ctx),
                ServiceLocator.appInfoAdapter(ctx),
                ServiceLocator.inputMethodAdapter(ctx),
                ServiceLocator.resourceProvider(ctx)
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