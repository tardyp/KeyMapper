package io.github.sds100.keymapper.domain.usecases

import io.github.sds100.keymapper.Constants
import io.github.sds100.keymapper.domain.preferences.Keys
import io.github.sds100.keymapper.domain.repositories.PreferenceRepository
import io.github.sds100.keymapper.domain.utils.FlowPrefDelegate
import io.github.sds100.keymapper.domain.utils.PrefDelegate
import io.github.sds100.keymapper.util.FingerprintMapUtils
import kotlinx.coroutines.flow.Flow

/**
 * Created by sds100 on 14/02/21.
 */
class OnboardingUseCaseImpl(
    private val preferenceRepository: PreferenceRepository
) : PreferenceRepository by preferenceRepository, OnboardingUseCase {

    override var shownAppIntro by PrefDelegate(Keys.shownAppIntro, false)

    override val showGuiKeyboardAdFlow by FlowPrefDelegate(Keys.showGuiKeyboardAd, true)
    override fun shownGuiKeyboardAd() {
        preferenceRepository.set(Keys.showGuiKeyboardAd, true)
    }

    override var approvedFingerprintFeaturePrompt by PrefDelegate(
        Keys.approvedFingerprintFeaturePrompt,
        false
    )

    override var shownScreenOffTriggersExplanation by PrefDelegate(
        Keys.shownScreenOffTriggersExplanation,
        false
    )

    override var shownParallelTriggerOrderExplanation by PrefDelegate(
        Keys.shownParallelTriggerOrderExplanation,
        false
    )
    override var shownSequenceTriggerExplanation by PrefDelegate(
        Keys.shownSequenceTriggerExplanation,
        false
    )

    override val showFingerprintFeatureNotificationIfAvailable: Boolean
        get() {
            val oldVersionCode = get(Keys.lastInstalledVersionCodeAccessibilityService)
                .firstBlocking() ?: -1

            val handledUpdateInHomeScreen = !showOnboardingAfterUpdateHomeScreen.firstBlocking()

            return oldVersionCode < FingerprintMapUtils.FINGERPRINT_GESTURES_MIN_VERSION
                && !handledUpdateInHomeScreen
        }

    override fun showedFingerprintFeatureNotificationIfAvailable() {
        set(Keys.lastInstalledVersionCodeAccessibilityService, Constants.VERSION_CODE)
    }

    override val showOnboardingAfterUpdateHomeScreen = get(Keys.lastInstalledVersionCodeHomeScreen)
        .map { it ?: -1 < Constants.VERSION_CODE }

    override fun showedOnboardingAfterUpdateHomeScreen() {
        set(Keys.lastInstalledVersionCodeHomeScreen, Constants.VERSION_CODE)
    }

    override val shownQuickStartGuideHint by FlowPrefDelegate(Keys.shownQuickStartGuideHint, false)
    override fun shownQuickStartGuideHint() {
        preferenceRepository.set(Keys.shownQuickStartGuideHint, true)
    }
}

interface OnboardingUseCase {
    var shownAppIntro: Boolean

    val showGuiKeyboardAdFlow: Flow<Boolean>
    fun shownGuiKeyboardAd()

    var approvedFingerprintFeaturePrompt: Boolean
    var shownScreenOffTriggersExplanation: Boolean
    var shownParallelTriggerOrderExplanation: Boolean
    var shownSequenceTriggerExplanation: Boolean

    val showFingerprintFeatureNotificationIfAvailable: Boolean
    fun showedFingerprintFeatureNotificationIfAvailable()

    val showOnboardingAfterUpdateHomeScreen: Flow<Boolean>
    fun showedOnboardingAfterUpdateHomeScreen()

    val shownQuickStartGuideHint: Flow<Boolean>
    fun shownQuickStartGuideHint()
}