package io.github.sds100.keymapper.util

import android.content.Context
import io.github.sds100.keymapper.KeyMapperApp
import io.github.sds100.keymapper.ServiceLocator
import io.github.sds100.keymapper.UseCases
import io.github.sds100.keymapper.actions.CreateSystemActionUseCaseImpl
import io.github.sds100.keymapper.data.viewmodel.*
import io.github.sds100.keymapper.devices.ChooseBluetoothDeviceUseCaseImpl
import io.github.sds100.keymapper.domain.actions.TestActionUseCaseImpl
import io.github.sds100.keymapper.domain.mappings.fingerprintmap.*
import io.github.sds100.keymapper.domain.mappings.keymap.*
import io.github.sds100.keymapper.domain.settings.ConfigSettingsUseCaseImpl
import io.github.sds100.keymapper.domain.usecases.*
import io.github.sds100.keymapper.home.HomeScreenUseCaseImpl
import io.github.sds100.keymapper.onboarding.AppIntroSlide
import io.github.sds100.keymapper.onboarding.AppIntroUseCase
import io.github.sds100.keymapper.onboarding.AppIntroUseCaseImpl
import io.github.sds100.keymapper.packages.DisplayAppShortcutsUseCaseImpl
import io.github.sds100.keymapper.service.AccessibilityServiceController
import io.github.sds100.keymapper.service.MyAccessibilityService
import io.github.sds100.keymapper.ui.mappings.fingerprintmap.ConfigFingerprintMapViewModel
import io.github.sds100.keymapper.ui.mappings.keymap.ConfigKeyMapViewModel
import io.github.sds100.keymapper.util.delegate.ActionPerformerDelegate

/**
 * Created by sds100 on 26/01/2020.
 */

//TODO rename to Inject. remove provide prefix from functions
object InjectorUtils {

    fun provideAppListViewModel(context: Context): ChooseAppViewModel.Factory {
        return ChooseAppViewModel.Factory(
            UseCases.displayPackages(context)
        )
    }

    fun provideAppShortcutListViewModel(context: Context): ChooseAppShortcutViewModel.Factory {
        return ChooseAppShortcutViewModel.Factory(
           DisplayAppShortcutsUseCaseImpl(
               ServiceLocator.appShortcutAdapter(context)
           ),
            ServiceLocator.resourceProvider(context)
        )
    }

    fun provideChooseConstraintListViewModel(ctx: Context): ChooseConstraintViewModel.Factory {
        return ChooseConstraintViewModel.Factory(ServiceLocator.resourceProvider(ctx))
    }

    fun provideKeyActionTypeViewModel(): KeyActionTypeViewModel.Factory {
        return KeyActionTypeViewModel.Factory()
    }

    fun provideKeyEventActionTypeViewModel(
        context: Context
    ): ConfigKeyEventViewModel.Factory {
        return ConfigKeyEventViewModel.Factory(
            UseCases.getInputDevices(context),
            ServiceLocator.resourceProvider(context)
        )
    }

    fun provideKeycodeListViewModel(): KeyCodeListViewModel.Factory {
        return KeyCodeListViewModel.Factory()
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

    fun provideTapCoordinateActionTypeViewModel(context: Context): PickDisplayCoordinateViewModel.Factory {
        return PickDisplayCoordinateViewModel.Factory(
            ServiceLocator.resourceProvider(context)
        )
    }

    fun provideSystemActionListViewModel(ctx: Context): SystemActionListViewModel.Factory {
        return SystemActionListViewModel.Factory(
            CreateSystemActionUseCaseImpl(
                ServiceLocator.systemFeatureAdapter(ctx),
                ServiceLocator.packageManagerAdapter(ctx),
                ServiceLocator.inputMethodAdapter(ctx)
            ),

            ServiceLocator.resourceProvider(ctx)
        )
    }

    fun provideUnsupportedActionListViewModel(
        context: Context
    ): UnsupportedActionListViewModel.Factory {
        return UnsupportedActionListViewModel.Factory(
            UseCases.isSystemActionSupported(context),
            ServiceLocator.resourceProvider(context)
        )
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

    fun provideConfigKeyMapViewModel(
        ctx: Context
    ): ConfigKeyMapViewModel.Factory {
        val configKeymapUseCase = ConfigKeyMapUseCaseImpl(ServiceLocator.externalDevicesAdapter(ctx))

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

    fun provideConfigFingerprintMapViewModel(
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

    fun provideCreateActionShortcutViewModel(
        context: Context
    ): CreateKeymapShortcutViewModel.Factory {
        return CreateKeymapShortcutViewModel.Factory(
            ServiceLocator.defaultKeymapRepository(context),
            UseCases.getActionError(context)
        )
    }

    fun provideHomeViewModel(ctx: Context): HomeViewModel.Factory {
        return HomeViewModel.Factory(
            HomeScreenUseCaseImpl(
                keyMapRepository = ServiceLocator.roomKeymapRepository(ctx),
                fingerprintMapRepository = ServiceLocator.fingerprintMapRepository(ctx),
                preferenceRepository = ServiceLocator.preferenceRepository(ctx),
                serviceAdapter = ServiceLocator.serviceAdapter(ctx),
                permissionAdapter = ServiceLocator.permissionAdapter(ctx),
                displayKeyMapUseCase = UseCases.displayKeyMap(ctx),
                displaySimpleMappingUseCase = UseCases.displaySimpleMapping(ctx),
                inputMethodAdapter = ServiceLocator.inputMethodAdapter(ctx),
                backupManager = ServiceLocator.backupManager(ctx)
            ),
            UseCases.onboarding(ctx),
            ServiceLocator.resourceProvider(ctx)
        )
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

    fun provideAppIntroViewModel(context: Context, slides: List<AppIntroSlide>): AppIntroViewModel.Factory {
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
            keymapRepository = ServiceLocator.defaultKeymapRepository(service),
            areFingerprintGesturesSupported = AreFingerprintGesturesSupportedUseCaseImpl(
                ServiceLocator.preferenceRepository(service)
            ),
            preferenceRepository = ServiceLocator.preferenceRepository(service)
        )
    }

    fun provideChooseBluetoothDevicesViewModel(ctx: Context): ChooseBluetoothDeviceViewModel.Factory{
        return ChooseBluetoothDeviceViewModel.Factory(
            ChooseBluetoothDeviceUseCaseImpl(ServiceLocator.externalDevicesAdapter(ctx)),
            ServiceLocator.resourceProvider(ctx)
        )
    }
}