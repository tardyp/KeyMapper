package io.github.sds100.keymapper.data.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.github.sds100.keymapper.domain.usecases.OnboardingUseCase

/**
 * Created by sds100 on 14/02/2021.
 */

class AppIntroViewModel(private val onboarding: OnboardingUseCase) : ViewModel() {

    fun shownAppIntro() {
        onboarding.shownAppIntro = true
        onboarding.approvedFingerprintFeaturePrompt = true
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val onboardingUseCase: OnboardingUseCase
    ) : ViewModelProvider.NewInstanceFactory() {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return AppIntroViewModel(onboardingUseCase) as T
        }
    }
}