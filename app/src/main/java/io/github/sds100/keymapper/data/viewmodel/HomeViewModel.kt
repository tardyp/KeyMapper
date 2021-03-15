package io.github.sds100.keymapper.data.viewmodel

import androidx.lifecycle.*
import io.github.sds100.keymapper.domain.usecases.OnboardingUseCase
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * Created by sds100 on 18/01/21.
 */
class HomeViewModel(private val onboarding: OnboardingUseCase) : ViewModel() {

    private val _showGuiKeyboardAd = MutableLiveData(false)
    val showGuiKeyboardAd: LiveData<Boolean> = _showGuiKeyboardAd

    private val _showWhatsNew = MutableLiveData(false)
    val showWhatsNew: LiveData<Boolean> = _showWhatsNew

    private val _showQuickStartGuideHint = MutableLiveData(false)
    val showQuickStartGuide: LiveData<Boolean> = _showQuickStartGuideHint

    init {
        onboarding.showGuiKeyboardAdFlow.onEach {
            _showGuiKeyboardAd.value = it
        }.launchIn(viewModelScope)

        onboarding.shownQuickStartGuideHint.onEach { shown ->
            _showQuickStartGuideHint.value = !shown
        }.launchIn(viewModelScope)

        onboarding.showOnboardingAfterUpdateHomeScreen.onEach { show ->
            _showWhatsNew.value = show
        }.launchIn(viewModelScope)
    }

    fun shownGuiKeyboardAd() = run { onboarding.shownGuiKeyboardAd() }

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