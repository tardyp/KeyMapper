package io.github.sds100.keymapper.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.github.sds100.keymapper.R
import io.github.sds100.keymapper.framework.adapters.ResourceProvider
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking

/**
 * Created by sds100 on 14/02/2021.
 */

class AppIntroViewModel(
    private val useCase: AppIntroUseCase,
    slides: List<AppIntroSlide>,
    resourceProvider: ResourceProvider
) : ViewModel(), ResourceProvider by resourceProvider {

    companion object {
        private const val ID_BUTTON_ENABLE_ACCESSIBILITY_SERVICE = "enable_accessibility_service"
        private const val ID_BUTTON_DISABLE_BATTERY_OPTIMISATION = "disable_battery_optimisation"
        private const val ID_BUTTON_DONT_KILL_MY_APP = "go_to_dont_kill_my_app"
        private const val ID_BUTTON_GRANT_DND_ACCESS = "grant_dnd_access"
    }

    private val slideModels: Flow<List<AppIntroSlideUi>> = combine(
        useCase.isAccessibilityServiceEnabled,
        useCase.isBatteryOptimised,
        useCase.hasDndAccessPermission,
        useCase.fingerprintGesturesSupported
    ) { isServiceEnabled, isBatteryOptimised, hasDndAccess, fingerprintGesturesSupported ->

        slidesToShow.map { slide ->
            when (slide) {
                AppIntroSlide.NOTE_FROM_DEV -> noteFromDeveloperSlide()
                AppIntroSlide.ACCESSIBILITY_SERVICE -> accessibilityServiceSlide(isServiceEnabled)
                AppIntroSlide.BATTERY_OPTIMISATION -> batteryOptimisationSlide(isBatteryOptimised)
                AppIntroSlide.FINGERPRINT_GESTURE_SUPPORT ->
                    fingerprintGestureSupportSlide(fingerprintGesturesSupported)
                AppIntroSlide.DO_NOT_DISTURB -> dndAccessSlide(hasDndAccess)
                AppIntroSlide.CONTRIBUTING -> contributingSlide()
            }
        }
    }

    private val _openUrl = MutableSharedFlow<String>()
    val openUrl = _openUrl.asSharedFlow()

    val slidesToShow: List<AppIntroSlide>

    init {
        slidesToShow = slides.mapNotNull { slide ->
            if (slide == AppIntroSlide.FINGERPRINT_GESTURE_SUPPORT
                && !useCase.deviceHasFingerprintReader()) {
                return@mapNotNull null
            }

            slide
        }
    }

    fun onButtonClick(id: String) {
        when (id) {
            ID_BUTTON_ENABLE_ACCESSIBILITY_SERVICE -> useCase.enableAccessibilityService()
            ID_BUTTON_DONT_KILL_MY_APP -> runBlocking {
                _openUrl.emit(getString(R.string.url_dont_kill_my_app))
            }
            ID_BUTTON_DISABLE_BATTERY_OPTIMISATION -> useCase.ignoreBatteryOptimisation()
            ID_BUTTON_GRANT_DND_ACCESS -> useCase.requestDndAccess()
        }
    }

    fun getSlide(slide: AppIntroSlide): Flow<AppIntroSlideUi> =
        slideModels.mapNotNull { allSlides -> allSlides.find { it.id == slide } }

    fun shownAppIntro() = useCase.shownAppIntro()

    private fun noteFromDeveloperSlide() = AppIntroSlideUi(
        id = AppIntroSlide.NOTE_FROM_DEV,
        image = getDrawable(R.mipmap.ic_launcher_round),
        title = getString(R.string.showcase_note_from_the_developer_title),
        description = getString(R.string.showcase_note_from_the_developer_description),
        backgroundColor = getColor(R.color.red)
    )

    private fun accessibilityServiceSlide(isServiceEnabled: Boolean): AppIntroSlideUi {
        if (isServiceEnabled) {
            return AppIntroSlideUi(
                id = AppIntroSlide.ACCESSIBILITY_SERVICE,
                image = getDrawable(R.drawable.ic_baseline_check_64),
                title = getString(R.string.showcase_accessibility_service_title_enabled),
                description = getString(R.string.showcase_accessibility_service_description_enabled),
                backgroundColor = getColor(R.color.purple),
            )
        } else {
            return AppIntroSlideUi(
                id = AppIntroSlide.ACCESSIBILITY_SERVICE,
                image = getDrawable(R.drawable.ic_outline_error_outline_64),
                title = getString(R.string.showcase_accessibility_service_title_disabled),
                description = getString(R.string.showcase_accessibility_service_description_disabled),
                backgroundColor = getColor(R.color.purple),

                buttonId1 = ID_BUTTON_ENABLE_ACCESSIBILITY_SERVICE,
                buttonText1 = getString(R.string.enable)
            )
        }
    }

    private fun batteryOptimisationSlide(isBatteryOptimised: Boolean): AppIntroSlideUi {
        if (isBatteryOptimised) {
            return AppIntroSlideUi(
                id = AppIntroSlide.BATTERY_OPTIMISATION,
                image = getDrawable(R.drawable.ic_battery_std_white_64dp),
                title = getString(R.string.showcase_disable_battery_optimisation_title),
                description = getString(R.string.showcase_disable_battery_optimisation_message_bad),
                backgroundColor = getColor(R.color.blue),

                buttonId1 = ID_BUTTON_DONT_KILL_MY_APP,
                buttonText1 = getString(R.string.showcase_disable_battery_optimisation_button_dont_kill_my_app),

                buttonId2 = ID_BUTTON_DISABLE_BATTERY_OPTIMISATION,
                buttonText2 = getString(R.string.showcase_disable_battery_optimisation_button_turn_off),
            )
        } else {
            return AppIntroSlideUi(
                id = AppIntroSlide.BATTERY_OPTIMISATION,
                image = getDrawable(R.drawable.ic_battery_std_white_64dp),
                title = getString(R.string.showcase_disable_battery_optimisation_title),
                description = getString(R.string.showcase_disable_battery_optimisation_message_good),
                backgroundColor = getColor(R.color.blue),

                buttonId1 = ID_BUTTON_DONT_KILL_MY_APP,
                buttonText1 = getString(R.string.showcase_disable_battery_optimisation_button_dont_kill_my_app),
            )
        }
    }

    private fun fingerprintGestureSupportSlide(areGesturesAvailable: Boolean?): AppIntroSlideUi {
        when (areGesturesAvailable) {
            true -> return AppIntroSlideUi(
                id = AppIntroSlide.FINGERPRINT_GESTURE_SUPPORT,
                image = getDrawable(R.drawable.ic_baseline_check_64),
                title = getString(R.string.showcase_fingerprint_gesture_support_title_supported),
                description = getString(R.string.showcase_fingerprint_gesture_support_message_supported),
                backgroundColor = getColor(R.color.orange),
            )

            false -> return AppIntroSlideUi(
                id = AppIntroSlide.FINGERPRINT_GESTURE_SUPPORT,
                image = getDrawable(R.drawable.ic_baseline_cross_64),
                title = getString(R.string.showcase_fingerprint_gesture_support_title_not_supported),
                description = getString(R.string.showcase_fingerprint_gesture_support_message_not_supported),
                backgroundColor = getColor(R.color.orange),
            )

            null -> return AppIntroSlideUi(
                id = AppIntroSlide.FINGERPRINT_GESTURE_SUPPORT,
                image = getDrawable(R.drawable.ic_baseline_fingerprint_64),
                title = getString(R.string.showcase_fingerprint_gesture_support_title_supported_unknown),
                description = getString(R.string.showcase_fingerprint_gesture_support_message_supported_unknown),
                backgroundColor = getColor(R.color.orange),

                buttonId1 = ID_BUTTON_ENABLE_ACCESSIBILITY_SERVICE,
                buttonText1 = getString(R.string.enable)
            )
        }
    }

    private fun dndAccessSlide(isDndAccessGranted: Boolean): AppIntroSlideUi {
        if (isDndAccessGranted) {
            return AppIntroSlideUi(
                id = AppIntroSlide.DO_NOT_DISTURB,
                image = getDrawable(R.drawable.ic_baseline_check_64),
                title = getString(R.string.showcase_dnd_access_title_enabled),
                description = getString(R.string.showcase_dnd_access_description_enabled),
                backgroundColor = getColor(R.color.red)
            )
        } else {
            return AppIntroSlideUi(
                id = AppIntroSlide.DO_NOT_DISTURB,
                image = getDrawable(R.drawable.ic_outline_dnd_circle_outline_64),
                title = getString(R.string.showcase_dnd_access_title_disabled),
                description = getString(R.string.showcase_dnd_access_description_disabled),
                backgroundColor = getColor(R.color.red),

                buttonId1 = ID_BUTTON_GRANT_DND_ACCESS,
                buttonText1 = getString(R.string.pos_grant)
            )
        }
    }

    private fun contributingSlide() = AppIntroSlideUi(
        id = AppIntroSlide.CONTRIBUTING,
        image = getDrawable(R.drawable.ic_outline_feedback_64),
        title = getString(R.string.showcase_contributing_title),
        description = getString(R.string.showcase_contributing_description),
        backgroundColor = getColor(R.color.green)
    )

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val useCase: AppIntroUseCase,
        private val slides: List<AppIntroSlide>,
        private val resourceProvider: ResourceProvider
    ) : ViewModelProvider.NewInstanceFactory() {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return AppIntroViewModel(
                useCase,
                slides,
                resourceProvider
            ) as T
        }
    }
}