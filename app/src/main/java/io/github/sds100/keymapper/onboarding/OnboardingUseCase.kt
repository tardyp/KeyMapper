package io.github.sds100.keymapper.onboarding

import io.github.sds100.keymapper.Constants
import io.github.sds100.keymapper.data.Keys
import io.github.sds100.keymapper.data.repositories.PreferenceRepository
import io.github.sds100.keymapper.util.FlowPrefDelegate
import io.github.sds100.keymapper.util.PrefDelegate
import io.github.sds100.keymapper.mappings.fingerprintmaps.FingerprintMapUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

/**
 * Created by sds100 on 14/02/21.
 */
class OnboardingUseCaseImpl(
    private val preferenceRepository: PreferenceRepository
) : PreferenceRepository by preferenceRepository, OnboardingUseCase {

    override var shownAppIntro by PrefDelegate(Keys.shownAppIntro, false)

    override val showGuiKeyboardAdFlow by FlowPrefDelegate(Keys.showGuiKeyboardAd, true)
    override fun shownGuiKeyboardAd() {
        preferenceRepository.set(Keys.showGuiKeyboardAd, false)
    }

    override var approvedFingerprintFeaturePrompt by PrefDelegate(
        Keys.approvedFingerprintFeaturePrompt,
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

    override val showWhatsNew = get(Keys.lastInstalledVersionCodeHomeScreen)
        .map { it ?: -1 < Constants.VERSION_CODE }

    override fun showedWhatsNew() {
        set(Keys.lastInstalledVersionCodeHomeScreen, Constants.VERSION_CODE)
    }

    override val showFingerprintFeatureNotificationIfAvailable: Flow<Boolean> = combine(
        get(Keys.lastInstalledVersionCodeAccessibilityService).map { it ?: -1 },
        showWhatsNew
    ) { oldVersionCode, showWhatsNew ->
        //has the user opened the app and will have already seen that they can remap fingerprint gestures
        val handledUpdateInHomeScreen = !showWhatsNew

        oldVersionCode < FingerprintMapUtils.FINGERPRINT_GESTURES_MIN_VERSION && !handledUpdateInHomeScreen
    }

    override fun showedFingerprintFeatureNotificationIfAvailable() {
        set(Keys.lastInstalledVersionCodeAccessibilityService, Constants.VERSION_CODE)
    }

    override val showQuickStartGuideHint = get(Keys.shownQuickStartGuideHint).map {
        if (it == null) {
            true
        } else {
            !it
        }
    }

    override fun shownQuickStartGuideHint() {
        preferenceRepository.set(Keys.shownQuickStartGuideHint, true)
    }
}

interface OnboardingUseCase {
    var shownAppIntro: Boolean

    val showGuiKeyboardAdFlow: Flow<Boolean>
    fun shownGuiKeyboardAd()

    var approvedFingerprintFeaturePrompt: Boolean
    var shownParallelTriggerOrderExplanation: Boolean
    var shownSequenceTriggerExplanation: Boolean

    val showFingerprintFeatureNotificationIfAvailable: Flow<Boolean>
    fun showedFingerprintFeatureNotificationIfAvailable()

    val showWhatsNew: Flow<Boolean>
    fun showedWhatsNew()

    val showQuickStartGuideHint: Flow<Boolean>
    fun shownQuickStartGuideHint()
}