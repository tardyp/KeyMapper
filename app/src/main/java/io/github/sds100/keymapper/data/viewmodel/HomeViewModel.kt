package io.github.sds100.keymapper.data.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.github.sds100.keymapper.R
import io.github.sds100.keymapper.domain.actions.GetActionErrorUseCase
import io.github.sds100.keymapper.domain.constraints.GetConstraintErrorUseCase
import io.github.sds100.keymapper.domain.mappings.keymap.*
import io.github.sds100.keymapper.domain.permissions.IsAccessibilityServiceEnabledUseCase
import io.github.sds100.keymapper.domain.permissions.IsBatteryOptimisedUseCase
import io.github.sds100.keymapper.domain.settings.GetSettingsUseCase
import io.github.sds100.keymapper.domain.usecases.OnboardingUseCase
import io.github.sds100.keymapper.framework.adapters.ResourceProvider
import io.github.sds100.keymapper.ui.ListItem
import io.github.sds100.keymapper.ui.TextListItem
import io.github.sds100.keymapper.ui.actions.ActionUiHelper
import io.github.sds100.keymapper.ui.constraints.ConstraintUiHelper
import io.github.sds100.keymapper.ui.utils.SelectionState
import io.github.sds100.keymapper.util.MultiSelectProvider
import io.github.sds100.keymapper.util.MultiSelectProviderImpl
import io.github.sds100.keymapper.util.result.RecoverableError
import kotlinx.coroutines.Dispatchers
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
    private val isServiceEnabled: IsAccessibilityServiceEnabledUseCase,
    private val isBatteryOptimised: IsBatteryOptimisedUseCase,
    resourceProvider: ResourceProvider
) : ViewModel(), ResourceProvider by resourceProvider {

    private companion object {
        const val ID_ACCESSIBILITY_SERVICE_LIST_ITEM = "accessibility_service"
        const val ID_BATTERY_OPTIMISATION_LIST_ITEM = "battery_optimised"
    }

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

    val onboardingState = combine(
        onboarding.showGuiKeyboardAdFlow,
        onboarding.showOnboardingAfterUpdateHomeScreen,
        onboarding.showQuickStartGuideHint
    ) { showGuiKeyboardAd, showWhatsNew, showQuickStartGuideHint ->
        HomeOnboardingState(showGuiKeyboardAd, showWhatsNew, showQuickStartGuideHint)
    }.stateIn(viewModelScope, SharingStarted.Lazily, HomeOnboardingState())

    val selectionCountViewState = multiSelectProvider.state.map {
        when (it) {
            SelectionState.NotSelecting -> SelectionCountViewState(
                isVisible = false,
                text = ""
            )
            is SelectionState.Selecting -> SelectionCountViewState(
                isVisible = true,
                text = getString(R.string.selection_count, it.selectedIds.size)
            )
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        SelectionCountViewState(
            isVisible = false,
            text = ""
        )
    )

    val tabsState = multiSelectProvider.state.map {
        when (it) {
            SelectionState.NotSelecting ->
                HomeTabsState(
                    enableViewPagerSwiping = true,
                    showTabs = true
                )
            is SelectionState.Selecting -> HomeTabsState(
                enableViewPagerSwiping = false,
                showTabs = true
            )
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        HomeTabsState(
            enableViewPagerSwiping = true,
            showTabs = true
        )
    )

    val appBarState = multiSelectProvider.state.map {
        when (it) {
            SelectionState.NotSelecting -> HomeAppBarState.NORMAL

            is SelectionState.Selecting -> HomeAppBarState.MULTI_SELECTING
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        HomeAppBarState.NORMAL
    )

    val errorListState = combine(
        isServiceEnabled.isEnabled,
        settings.hideHomeScreenAlerts,
        rebuildState
    ) { isServiceEnabled, isHidden, _ ->
        val listItems = sequence {
            if (isServiceEnabled) {
                yield(
                    TextListItem.Success(
                        ID_ACCESSIBILITY_SERVICE_LIST_ITEM,
                        getString(R.string.home_success_accessibility_service_is_enabled)
                    )
                )
            } else {
                yield(
                    TextListItem.Error(
                        ID_ACCESSIBILITY_SERVICE_LIST_ITEM,
                        getString(R.string.home_error_accessibility_service_is_disabled)
                    )
                )
            }

            if (isBatteryOptimised()) {
                yield(
                    TextListItem.Error(
                        ID_BATTERY_OPTIMISATION_LIST_ITEM,
                        getString(R.string.home_error_is_battery_optimised)
                    )
                )
            } // don't show a success message for this
        }.toList()
        HomeErrorListState(listItems, !isHidden)
    }.flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.Eagerly, HomeErrorListState(emptyList(), false))

    private val _showMenu = MutableSharedFlow<Unit>()
    val showMenu = _showMenu.asSharedFlow()

    private val _navigateToCreateKeymapScreen = MutableSharedFlow<Unit>()
    val navigateToCreateKeymapScreen = _navigateToCreateKeymapScreen.asSharedFlow()

    private val _closeKeyMapper = MutableSharedFlow<Unit>()
    val closeKeyMapper = _closeKeyMapper.asSharedFlow()

    //TODO this in keymaplistviewmodel and fingerprintmaplistviewmodel
    private val _fixError = MutableSharedFlow<RecoverableError>()
    val fixError = _fixError.asSharedFlow()

    fun approvedGuiKeyboardAd() = run { onboarding.shownGuiKeyboardAd() }

    fun approvedWhatsNew() = onboarding.showedOnboardingAfterUpdateHomeScreen()

    fun approvedQuickStartGuideTapTarget() = onboarding.shownQuickStartGuideHint()

    fun onAppBarNavigationButtonClick() {
        viewModelScope.launch {
            if (multiSelectProvider.state.value is SelectionState.Selecting) {
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
            if (multiSelectProvider.state.value is SelectionState.Selecting) {
                multiSelectProvider.state.value.apply {
                    if (this is SelectionState.Selecting) {
                        deleteKeymaps.invoke(*selectedIds.toTypedArray())
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
            enableDisableKeymaps.enable(*selectedIds.toTypedArray())
        }
    }

    fun onDisableSelectedKeymapsClick() {
        multiSelectProvider.state.value.apply {
            if (this !is SelectionState.Selecting) return
            enableDisableKeymaps.disable(*selectedIds.toTypedArray())
        }
    }

    fun onDuplicateSelectedKeymapsClick() {
        multiSelectProvider.state.value.apply {
            if (this !is SelectionState.Selecting) return
            duplicateKeymaps.invoke(*selectedIds.toTypedArray())
        }
    }

    fun onBackupSelectedKeymapsClick() {
        //TODO
    }

    fun onFixErrorListItemClick(id: String) {
        viewModelScope.launch {
            when (id) {
                ID_ACCESSIBILITY_SERVICE_LIST_ITEM -> _fixError.emit(RecoverableError.AccessibilityServiceDisabled)
                ID_BATTERY_OPTIMISATION_LIST_ITEM -> _fixError.emit(RecoverableError.IsBatteryOptimised)
            }
        }
    }

    fun rebuildUiState() {
        runBlocking { rebuildState.emit(Unit) }
    }

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
        private val settingsUseCase: GetSettingsUseCase,
        private val isServiceEnabled: IsAccessibilityServiceEnabledUseCase,
        private val isBatteryOptimised: IsBatteryOptimisedUseCase,
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
                isServiceEnabled,
                isBatteryOptimised,
                resourceProvider,
            ) as T
        }
    }
}

data class SelectionCountViewState(
    val isVisible: Boolean,
    val text: String
)

enum class HomeAppBarState {
    NORMAL, MULTI_SELECTING
}

data class HomeOnboardingState(
    val showGuiKeyboardAd: Boolean = false,
    val showWhatsNew: Boolean = false,
    val showQuickStartGuideTapTarget: Boolean = false,
)

data class HomeTabsState(
    val enableViewPagerSwiping: Boolean = true,
    val showTabs: Boolean = false,
)

data class HomeErrorListState(
    val listItems: List<ListItem>,
    val isVisible: Boolean
)