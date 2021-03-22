package io.github.sds100.keymapper.data.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.github.sds100.keymapper.R
import io.github.sds100.keymapper.domain.actions.GetActionErrorUseCase
import io.github.sds100.keymapper.domain.constraints.GetConstraintErrorUseCase
import io.github.sds100.keymapper.domain.mappings.keymap.*
import io.github.sds100.keymapper.domain.settings.GetSettingsUseCase
import io.github.sds100.keymapper.domain.usecases.OnboardingUseCase
import io.github.sds100.keymapper.framework.adapters.ResourceProvider
import io.github.sds100.keymapper.ui.actions.ActionUiHelper
import io.github.sds100.keymapper.ui.constraints.ConstraintUiHelper
import io.github.sds100.keymapper.ui.utils.SelectionState
import io.github.sds100.keymapper.util.MultiSelectProvider
import io.github.sds100.keymapper.util.MultiSelectProviderImpl
import io.github.sds100.keymapper.util.result.RecoverableError
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * Created by sds100 on 18/01/21.
 */
class HomeViewModel(
    private val onboarding: OnboardingUseCase,
    listKeymapsUseCase: ListKeymapsUseCase,
    private val deleteKeymaps: DeleteKeymapsUseCase,
    private val enableDisableKeymaps: EnableDisableKeymapsUseCase,
    private val duplicateKeymaps: DuplicateKeymapsUseCase,
    getActionError: GetActionErrorUseCase,
    actionUiHelper: ActionUiHelper<KeymapAction>,
    constraintUiHelper: ConstraintUiHelper,
    getConstraintErrorUseCase: GetConstraintErrorUseCase,
    private val settings: GetSettingsUseCase,
    resourceProvider: ResourceProvider
) : ViewModel(), ResourceProvider by resourceProvider {

    private val multiSelectProvider: MultiSelectProvider = MultiSelectProviderImpl()

    val keymapListViewModel = KeymapListViewModel(
        viewModelScope,
        listKeymapsUseCase,
        getActionError,
        actionUiHelper,
        constraintUiHelper,
        getConstraintErrorUseCase,
        resourceProvider,
        multiSelectProvider
    )

    private val rebuildState = MutableSharedFlow<Unit>()

    private val _state = MutableStateFlow(
        buildState(
            hideHomeScreenAlerts = false,
            areKeymapsSelectable = false,
            selectedKeymapsCount = 0,
            showGuiKeyboardAd = false,
            showWhatsNew = false,
            showQuickStartGuideTapTarget = false
        )
    )
    val state = _state.asStateFlow()

    private val _showMenu = MutableSharedFlow<Unit>()
    val showMenu = _showMenu.asSharedFlow()

    private val _navigateToCreateKeymapScreen = MutableSharedFlow<Unit>()
    val navigateToCreateKeymapScreen = _navigateToCreateKeymapScreen.asSharedFlow()

    private val _closeKeyMapper = MutableSharedFlow<Unit>()
    val closeKeyMapper = _closeKeyMapper.asSharedFlow()

    //TODO this in keymaplistviewmodel and fingerprintmaplistviewmodel
    private val _fixFailure = MutableSharedFlow<RecoverableError>()
    val fixFailure = _fixFailure.asSharedFlow()

    init {

        viewModelScope.launch {
            val onboardingStateFlow = combine(
                onboarding.showGuiKeyboardAdFlow,
                onboarding.showOnboardingAfterUpdateHomeScreen,
                onboarding.showQuickStartGuideHint
            ) { showGuiKeyboardAd, showWhatsNew, showQuickStartGuideHint ->
                OnboardingState(showGuiKeyboardAd, showWhatsNew, showQuickStartGuideHint)
            }

            combine(
                settings.hideHomeScreenAlerts,
                multiSelectProvider.state,
                onboardingStateFlow
            ) { hideHomeScreenAlerts, selectionState, onboardingState ->
                buildState(
                    hideHomeScreenAlerts,
                    areKeymapsSelectable = selectionState is SelectionState.Selecting,
                    selectedKeymapsCount = (selectionState as? SelectionState.Selecting)?.selectedIds?.size
                        ?: 0,
                    showGuiKeyboardAd = onboardingState.showGuiKeyboardAd,
                    showWhatsNew = onboardingState.showWhatsNew,
                    showQuickStartGuideTapTarget = onboardingState.showQuickStartGuideTapTarget
                )
            }.collectLatest {
                _state.value = it
            }
        }
    }

    fun approvedGuiKeyboardAd() = run { onboarding.shownGuiKeyboardAd() }

    fun approvedWhatsNew() = onboarding.showedOnboardingAfterUpdateHomeScreen()

    fun approvedQuickStartGuideTapTarget() = onboarding.shownQuickStartGuideHint()

    fun onAppBarNavigationButtonClick() {
        viewModelScope.launch {
            if (state.value.multiSelecting) {
                multiSelectProvider.stopSelecting()
            } else {
                _showMenu.emit(Unit)
            }
        }
    }

    fun onBackPressed() {
        viewModelScope.launch {
            if (multiSelectProvider.state.value is SelectionState.Selecting) {
                multiSelectProvider.stopSelecting()
            } else {
                _closeKeyMapper.emit(Unit)
            }
        }
    }

    fun onFabPressed() {
        viewModelScope.launch {
            if (state.value.multiSelecting) {
                multiSelectProvider.state.value.apply {
                    if (this is SelectionState.Selecting) {
                        deleteKeymaps.invoke(*selectedIds.toLongArray())
                    }
                }
                multiSelectProvider.stopSelecting()
            } else {
                _navigateToCreateKeymapScreen.emit(Unit)
            }
        }
    }

    fun onSelectAllClick() {
        keymapListViewModel.selectAll()
    }

    fun onEnableSelectedKeymapsClick() {
        multiSelectProvider.state.value.apply {
            if (this !is SelectionState.Selecting) return
            enableDisableKeymaps.enable(*selectedIds.toLongArray())
        }
    }

    fun onDisableSelectedKeymapsClick() {
        multiSelectProvider.state.value.apply {
            if (this !is SelectionState.Selecting) return
            enableDisableKeymaps.disable(*selectedIds.toLongArray())
        }
    }

    fun onDuplicateSelectedKeymapsClick() {
        multiSelectProvider.state.value.apply {
            if (this !is SelectionState.Selecting) return
            duplicateKeymaps.invoke(*selectedIds.toLongArray())
        }
    }

    fun onBackupSelectedKeymapsClick() {
        //TODO
    }

    fun rebuildUiState() {
        runBlocking { rebuildState.emit(Unit) }
    }

    private fun buildState(
        hideHomeScreenAlerts: Boolean,
        areKeymapsSelectable: Boolean,
        selectedKeymapsCount: Int,
        showGuiKeyboardAd: Boolean,
        showWhatsNew: Boolean,
        showQuickStartGuideTapTarget: Boolean
    ): HomeState {

        return HomeState(
            enableViewPagerSwiping = true, //TODO
            selectedCountString = getString(R.string.selection_count, selectedKeymapsCount),
            showTabs = !areKeymapsSelectable,
            hideAlerts = hideHomeScreenAlerts,
            showGuiKeyboardAd = showGuiKeyboardAd,
            showWhatsNew = showWhatsNew,
            showQuickStartGuideTapTarget = showQuickStartGuideTapTarget,
            multiSelecting = areKeymapsSelectable
        )
    }

    private data class OnboardingState(
        val showGuiKeyboardAd: Boolean,
        val showWhatsNew: Boolean,
        val showQuickStartGuideTapTarget: Boolean
    )

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val onboardingUseCase: OnboardingUseCase,
        private val listKeymapsUseCase: ListKeymapsUseCase,
        private val deleteKeymaps: DeleteKeymapsUseCase,
        private val enableDisableKeymaps: EnableDisableKeymapsUseCase,
        private val duplicateKeymapsUseCase: DuplicateKeymapsUseCase,
        private val getActionError: GetActionErrorUseCase,
        private val keymapActionUiHelper: ActionUiHelper<KeymapAction>,
        private val constraintUiHelper: ConstraintUiHelper,
        private val getConstraintErrorUseCase: GetConstraintErrorUseCase,
        private val resourceProvider: ResourceProvider,
        private val settingsUseCase: GetSettingsUseCase
    ) : ViewModelProvider.NewInstanceFactory() {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return HomeViewModel(
                onboardingUseCase,
                listKeymapsUseCase,
                deleteKeymaps,
                enableDisableKeymaps,
                duplicateKeymapsUseCase,
                getActionError,
                keymapActionUiHelper,
                constraintUiHelper,
                getConstraintErrorUseCase,
                settingsUseCase,
                resourceProvider
            ) as T
        }
    }
}

data class HomeState(
    val enableViewPagerSwiping: Boolean,
    val showTabs: Boolean,
    val selectedCountString: String,
    val hideAlerts: Boolean,
    val showGuiKeyboardAd: Boolean,
    val multiSelecting: Boolean,
    val showWhatsNew: Boolean,
    val showQuickStartGuideTapTarget: Boolean
)