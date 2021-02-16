package io.github.sds100.keymapper.domain.usecases

import io.github.sds100.keymapper.Constants
import io.github.sds100.keymapper.domain.preferences.Keys
import io.github.sds100.keymapper.domain.repositories.PreferenceRepository
import io.github.sds100.keymapper.domain.utils.FlowPrefDelegate
import io.github.sds100.keymapper.domain.utils.PrefDelegate
import io.github.sds100.keymapper.util.FingerprintMapUtils
import io.github.sds100.keymapper.util.firstBlocking
import kotlinx.coroutines.flow.map

/**
 * Created by sds100 on 14/02/21.
 */
class OnboardingUseCase(
    preferenceRepository: PreferenceRepository
) : PreferenceRepository by preferenceRepository {

    var shownAppIntro by PrefDelegate(Keys.shownAppIntro, false)

    var showGuiKeyboardAd by PrefDelegate(Keys.showGuiKeyboardAd, true)
    val showGuiKeyboardAdFlow by FlowPrefDelegate(Keys.showGuiKeyboardAd, true)

    var approvedFingerprintFeaturePrompt by PrefDelegate(
        Keys.approvedFingerprintFeaturePrompt,
        false
    )
    var shownScreenOffTriggersExplanation by PrefDelegate(
        Keys.shownScreenOffTriggersExplanation,
        false
    )
    var shownParallelTriggerOrderExplanation by PrefDelegate(
        Keys.shownParallelTriggerOrderExplanation,
        false
    )
    var shownSequenceTriggerExplanation by PrefDelegate(Keys.shownSequenceTriggerExplanation, false)
    var shownMultipleOfSameKeyInSequenceTriggerExplanation by PrefDelegate(
        Keys.shownMultipleOfSameKeyInSequenceTriggerExplanation,
        false
    )

    fun showFingerprintFeatureNotificationIfAvailable(): Boolean {
        val oldVersionCode = get(Keys.lastInstalledVersionCodeAccessibilityService)
            .firstBlocking() ?: -1

        val handledUpdateInHomeScreen = !showOnboardingAfterUpdateHomeScreen.firstBlocking()

        return oldVersionCode < FingerprintMapUtils.FINGERPRINT_GESTURES_MIN_VERSION
            && !handledUpdateInHomeScreen
    }

    fun showedFingerprintFeatureNotificationIfAvailable() {
        set(Keys.lastInstalledVersionCodeAccessibilityService, Constants.VERSION_CODE)
    }

    val showOnboardingAfterUpdateHomeScreen = get(Keys.lastInstalledVersionCodeHomeScreen)
        .map { it ?: -1 < Constants.VERSION_CODE }

    fun showedOnboardingAfterUpdateHomeScreen() {
        set(Keys.lastInstalledVersionCodeHomeScreen, Constants.VERSION_CODE)
    }

    var shownQuickStartGuideHint by PrefDelegate(Keys.shownQuickStartGuideHint, false)
    val shownQuickStartGuideHintFlow by FlowPrefDelegate(Keys.shownQuickStartGuideHint, false)
}