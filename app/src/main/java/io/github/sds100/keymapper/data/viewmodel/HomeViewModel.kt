package io.github.sds100.keymapper.data.viewmodel

import androidx.lifecycle.*
import io.github.sds100.keymapper.domain.usecases.OnboardingUseCase
import io.github.sds100.keymapper.util.collectIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Created by sds100 on 18/01/21.
 */
class HomeViewModel(private val onboarding: OnboardingUseCase) : ViewModel() {

    private val _showGuiKeyboardAd = MutableStateFlow(false)
    val showGuiKeyboardAd: StateFlow<Boolean> = _showGuiKeyboardAd

    private val _showWhatsNew = MutableLiveData(false)
    val showWhatsNew: LiveData<Boolean> = _showWhatsNew

    private val _showQuickStartGuideHint = MutableLiveData(false)
    val showQuickStartGuide: LiveData<Boolean> = _showQuickStartGuideHint

    init {
        onboarding.showGuiKeyboardAdFlow.collectIn(viewModelScope) {
            _showGuiKeyboardAd.value = it
        }

        onboarding.shownQuickStartGuideHintFlow.collectIn(viewModelScope) { shown ->
            _showQuickStartGuideHint.value = !shown
        }

        onboarding.showOnboardingAfterUpdateHomeScreen.collectIn(viewModelScope) { show ->
            _showWhatsNew.value = show
        }
    }

    fun shownGuiKeyboardAd() = run { onboarding.showGuiKeyboardAd = false }

    fun shownWhatsNew() = onboarding.showedOnboardingAfterUpdateHomeScreen()

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val onboardingUseCase: OnboardingUseCase
    ) : ViewModelProvider.NewInstanceFactory() {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return HomeViewModel(onboardingUseCase) as T
        }
    }
}