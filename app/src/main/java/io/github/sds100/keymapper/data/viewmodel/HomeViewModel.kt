package io.github.sds100.keymapper.data.viewmodel

import androidx.annotation.MenuRes
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

    private val _showQuickStartGuideTapTarget = MutableLiveData(false)
    val showQuickStartGuideTapTarget: LiveData<Boolean> = _showQuickStartGuideTapTarget

    init {
        onboarding.showGuiKeyboardAdFlow.onEach {
            _showGuiKeyboardAd.value = it
        }.launchIn(viewModelScope)

        onboarding.shownQuickStartGuideHint.onEach { shown ->
            _showQuickStartGuideTapTarget.value = !shown
        }.launchIn(viewModelScope)

        onboarding.showOnboardingAfterUpdateHomeScreen.onEach { show ->
            _showWhatsNew.value = show
        }.launchIn(viewModelScope)
    }

    fun shownGuiKeyboardAd() = run { onboarding.shownGuiKeyboardAd() }

    fun shownWhatsNew() = onboarding.showedOnboardingAfterUpdateHomeScreen()

    fun approvedQuickStartGuideTapTarget() = onboarding.shownQuickStartGuideHint()

    fun onNavigationMenuClick() {
        //TODO stop selecting key maps
    }

    fun onBackPressed() {
        //TODO move key maplistviewmodel to here. stop selecting if in selection mode
    }

    fun onDeleteFabPressed() {
        //TODO stop selecting and delete selected keymaps
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val onboardingUseCase: OnboardingUseCase
    ) : ViewModelProvider.NewInstanceFactory() {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return HomeViewModel(onboardingUseCase) as T
        }
    }
}

data class HomeState(
    val enableViewPagingSwiping: Boolean,
    @MenuRes val menuId: Int
)