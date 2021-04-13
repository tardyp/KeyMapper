package io.github.sds100.keymapper.data.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.github.sds100.keymapper.R
import io.github.sds100.keymapper.domain.usecases.OnboardingUseCase
import io.github.sds100.keymapper.framework.adapters.ResourceProvider
import io.github.sds100.keymapper.home.HomeScreenUseCase
import io.github.sds100.keymapper.ui.*
import io.github.sds100.keymapper.ui.dialogs.DialogResponse
import io.github.sds100.keymapper.ui.dialogs.GetUserResponse
import io.github.sds100.keymapper.ui.home.HomeTab
import io.github.sds100.keymapper.ui.utils.SelectionState
import io.github.sds100.keymapper.util.MultiSelectProvider
import io.github.sds100.keymapper.util.MultiSelectProviderImpl
import io.github.sds100.keymapper.util.result.FixableError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * Created by sds100 on 18/01/21.
 */
class HomeViewModel(
    private val useCase: HomeScreenUseCase,
    private val onboarding: OnboardingUseCase,
    resourceProvider: ResourceProvider,
) : ViewModel(),
    ResourceProvider by resourceProvider,
    UserResponseViewModel by UserResponseViewModelImpl() {

    private companion object {
        const val ID_ACCESSIBILITY_SERVICE_LIST_ITEM = "accessibility_service"
        const val ID_BATTERY_OPTIMISATION_LIST_ITEM = "battery_optimised"
    }

    private val multiSelectProvider: MultiSelectProvider = MultiSelectProviderImpl()

    val keymapListViewModel = KeymapListViewModel(
        viewModelScope,
        useCase,
        resourceProvider,
        multiSelectProvider
    )

    val fingerprintMapListViewModel = FingerprintMapListViewModel(
        viewModelScope,
        useCase,
        resourceProvider
    )

    private val _openUrl = MutableSharedFlow<String>()
    val openUrl = _openUrl.asSharedFlow()

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

    val tabsState =
        combine(
            multiSelectProvider.state,
            useCase.areFingerprintGesturesSupported
        ) { selectionState, areFingerprintGesturesSupported ->

            val tabs = sequence {
                yield(HomeTab.KEY_EVENTS)

                if (areFingerprintGesturesSupported) {
                    yield(HomeTab.FINGERPRINT_MAPS)
                }
            }.toSet()

            val showTabs = when {
                tabs.size == 1 -> false
                selectionState is SelectionState.Selecting -> false
                else -> true
            }

            HomeTabsState(
                enableViewPagerSwiping = showTabs,
                showTabs = showTabs,
                tabs = tabs
            )

        }.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            HomeTabsState(
                enableViewPagerSwiping = false,
                showTabs = false,
                emptySet()
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
        useCase.isAccessibilityServiceEnabled,
        useCase.hideHomeScreenAlerts,
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

            if (useCase.isBatteryOptimised()) {
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
                        useCase.deleteKeyMap(*selectedIds.toTypedArray())
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
            useCase.enableKeyMap(*selectedIds.toTypedArray())
        }
    }

    fun onDisableSelectedKeymapsClick() {
        multiSelectProvider.state.value.apply {
            if (this !is SelectionState.Selecting) return
            useCase.disableKeyMap(*selectedIds.toTypedArray())
        }
    }

    fun onDuplicateSelectedKeymapsClick() {
        multiSelectProvider.state.value.apply {
            if (this !is SelectionState.Selecting) return
            useCase.duplicateKeyMap(*selectedIds.toTypedArray())
        }
    }

    fun onBackupSelectedKeymapsClick() {
        //TODO
    }

    fun onFixErrorListItemClick(id: String) {
        viewModelScope.launch {
            when (id) {
                ID_ACCESSIBILITY_SERVICE_LIST_ITEM -> useCase.enableAccessibilityService()
                ID_BATTERY_OPTIMISATION_LIST_ITEM -> useCase.ignoreBatteryOptimisation()
            }
        }
    }

    fun rebuildUiState() {
        runBlocking { rebuildState.emit(Unit) }
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val useCase: HomeScreenUseCase,
        private val onboarding: OnboardingUseCase,
        private val resourceProvider: ResourceProvider,
    ) : ViewModelProvider.NewInstanceFactory() {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return HomeViewModel(useCase, onboarding, resourceProvider) as T
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
    val tabs: Set<HomeTab>
)

data class HomeErrorListState(
    val listItems: List<ListItem>,
    val isVisible: Boolean
)