package io.github.sds100.keymapper.data.viewmodel

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.github.sds100.keymapper.Constants
import io.github.sds100.keymapper.R
import io.github.sds100.keymapper.data.model.SystemActionListItem
import io.github.sds100.keymapper.domain.actions.*
import io.github.sds100.keymapper.domain.ime.GetEnabledInputMethodsUseCase
import io.github.sds100.keymapper.domain.packages.GetPackagesUseCase
import io.github.sds100.keymapper.domain.utils.*
import io.github.sds100.keymapper.framework.adapters.AppUiAdapter
import io.github.sds100.keymapper.framework.adapters.ResourceProvider
import io.github.sds100.keymapper.ui.*
import io.github.sds100.keymapper.ui.dialogs.DialogUi
import io.github.sds100.keymapper.ui.utils.*
import io.github.sds100.keymapper.util.SystemActionUtils
import io.github.sds100.keymapper.util.containsQuery
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/**
 * Created by sds100 on 31/03/2020.
 */
class SystemActionListViewModel(
    resourceProvider: ResourceProvider,
    private val isSystemActionSupported: IsSystemActionSupportedUseCase,
    private val getInputMethods: GetEnabledInputMethodsUseCase,
    private val getInstalledPackages: GetPackagesUseCase,
    private val appUiAdapter: AppUiAdapter
) : ViewModel(), ResourceProvider by resourceProvider, DialogViewModel by DialogViewModelImpl() {

    val searchQuery = MutableStateFlow<String?>(null)

    private val rebuildUiState = MutableSharedFlow<Unit>()

    private val _state = MutableStateFlow(SystemActionListState(ListUiState.Loading, false))
    val state = _state.asStateFlow()

    private val _returnResult = MutableSharedFlow<SystemAction>()
    val returnResult = _returnResult.asSharedFlow()

    private var createSystemActionJob: Job? = null

    init {
        viewModelScope.launch {
            combine(
                searchQuery,
                rebuildUiState
            ) { query, _ ->
                query
            }.collectLatest { query ->
                _state.value = SystemActionListState(
                    ListUiState.Loading,
                    state.value.showUnsupportedActionsMessage
                )

                _state.value = withContext(Dispatchers.Default) {
                    buildState(query)
                }
            }
        }
    }

    fun onSystemActionClick(id: SystemActionId) {
        createSystemActionJob?.cancel()

        createSystemActionJob = viewModelScope.launch {
            showMessageForSystemAction(id)

            when (id) {
                SystemActionId.SWITCH_KEYBOARD -> {
                    val inputMethods = getInputMethods()
                    val items = inputMethods.map {
                        it.id to it.label
                    }

                    val imeId = showDialog("choose_ime", DialogUi.SingleChoice(items)).item
                    val imeName = inputMethods.single { it.id == imeId }.label

                    _returnResult.emit(SwitchKeyboardSystemAction(imeId, imeName))
                }

                SystemActionId.PLAY_PAUSE_MEDIA_PACKAGE,
                SystemActionId.PLAY_MEDIA_PACKAGE,
                SystemActionId.PAUSE_MEDIA_PACKAGE,
                SystemActionId.NEXT_TRACK_PACKAGE,
                SystemActionId.PREVIOUS_TRACK_PACKAGE,
                SystemActionId.FAST_FORWARD_PACKAGE,
                SystemActionId.REWIND_PACKAGE,
                -> {
                    val items = withContext(Dispatchers.Default) {
                        val packages = getInstalledPackages.installedPackages.first {
                            it is State.Data
                        } as State.Data

                        packages.data
                            .filter { it.canBeLaunched }
                            .map {
                                it.packageName to appUiAdapter.getAppName(it.packageName).first()
                            }
                            .sortedBy { it.second }
                    }

                    val packageName =
                        showDialog("choose_package", DialogUi.SingleChoice(items)).item

                    val action = when (id) {
                        SystemActionId.PAUSE_MEDIA_PACKAGE ->
                            ControlMediaForAppSystemAction.Pause(packageName)
                        SystemActionId.PLAY_MEDIA_PACKAGE ->
                            ControlMediaForAppSystemAction.Play(packageName)
                        SystemActionId.PLAY_PAUSE_MEDIA_PACKAGE ->
                            ControlMediaForAppSystemAction.PlayPause(packageName)
                        SystemActionId.NEXT_TRACK_PACKAGE ->
                            ControlMediaForAppSystemAction.NextTrack(packageName)
                        SystemActionId.PREVIOUS_TRACK_PACKAGE ->
                            ControlMediaForAppSystemAction.PreviousTrack(packageName)
                        SystemActionId.FAST_FORWARD_PACKAGE ->
                            ControlMediaForAppSystemAction.FastForward(packageName)
                        SystemActionId.REWIND_PACKAGE ->
                            ControlMediaForAppSystemAction.Rewind(packageName)
                        else -> throw Exception("don't know how to create system action for $id")
                    }

                    _returnResult.emit(action)
                }

                SystemActionId.VOLUME_INCREASE_STREAM,
                SystemActionId.VOLUME_DECREASE_STREAM -> {
                    val items = VolumeStream.values()
                        .map { it to getString(VolumeStreamUtils.getLabel(it)) }

                    val stream = showDialog("pick_volume_stream", DialogUi.SingleChoice(items)).item

                    val action = when (id) {
                        SystemActionId.VOLUME_INCREASE_STREAM ->
                            VolumeSystemAction.Stream.Increase(showVolumeUi = false, stream)

                        SystemActionId.VOLUME_DECREASE_STREAM ->
                            VolumeSystemAction.Stream.Decrease(showVolumeUi = false, stream)

                        else -> throw Exception("don't know how to create system action for $id")
                    }

                    _returnResult.emit(action)
                }

                SystemActionId.CHANGE_RINGER_MODE -> {
                    val items = RingerMode.values()
                        .map { it to getString(RingerModeUtils.getLabel(it)) }

                    val ringerMode =
                        showDialog("pick_ringer_mode", DialogUi.SingleChoice(items)).item

                    _returnResult.emit(ChangeRingerModeSystemAction(ringerMode))
                }

                //don't need to show options for disabling do not disturb
                SystemActionId.TOGGLE_DND_MODE,
                SystemActionId.ENABLE_DND_MODE -> {
                    val items = DndMode.values()
                        .map { it to getString(DndModeUtils.getLabel(it)) }

                    val dndMode = showDialog("pick_dnd_mode", DialogUi.SingleChoice(items)).item

                    val action = when (id) {
                        SystemActionId.TOGGLE_DND_MODE ->
                            ChangeDndModeSystemAction.Toggle(dndMode)

                        SystemActionId.ENABLE_DND_MODE ->
                            ChangeDndModeSystemAction.Enable(dndMode)

                        else -> throw Exception("don't know how to create system action for $id")
                    }

                    _returnResult.emit(action)
                }

                SystemActionId.CYCLE_ROTATIONS -> {
                    val items = Orientation.values()
                        .map { it to getString(OrientationUtils.getLabel(it)) }

                    val orientations =
                        showDialog("pick_orientations", DialogUi.MultiChoice(items)).items

                    _returnResult.emit(CycleRotationsSystemAction(orientations))
                }

                SystemActionId.TOGGLE_FLASHLIGHT,
                SystemActionId.ENABLE_FLASHLIGHT,
                SystemActionId.DISABLE_FLASHLIGHT -> {
                    val items = CameraLens.values().map {
                        it to getString(CameraLensUtils.getLabel(it))
                    }

                    val lens = showDialog("pick_lens", DialogUi.SingleChoice(items)).item

                    val action = when (id) {
                        SystemActionId.TOGGLE_FLASHLIGHT -> FlashlightSystemAction.Toggle(lens)
                        SystemActionId.ENABLE_FLASHLIGHT -> FlashlightSystemAction.Enable(lens)
                        SystemActionId.DISABLE_FLASHLIGHT -> FlashlightSystemAction.Disable(lens)
                        else -> throw Exception("don't know how to create system action for $id")
                    }

                    _returnResult.emit(action)
                }

                else -> _returnResult.emit(SimpleSystemAction(id))
            }
        }
    }

    fun rebuildUiState() {
        runBlocking { rebuildUiState.emit(Unit) }
    }

    override fun onCleared() {
        createSystemActionJob?.cancel()
        createSystemActionJob = null
        super.onCleared()
    }

    private suspend fun showMessageForSystemAction(id: SystemActionId) {
        @StringRes val messageToShow: Int? = when (id) {
            SystemActionId.FAST_FORWARD_PACKAGE,
            SystemActionId.FAST_FORWARD -> R.string.action_fast_forward_message

            SystemActionId.REWIND_PACKAGE,
            SystemActionId.REWIND -> R.string.action_rewind_message

            SystemActionId.MOVE_CURSOR_TO_END -> R.string.action_move_to_end_of_text_message

            SystemActionId.TOGGLE_KEYBOARD,
            SystemActionId.SHOW_KEYBOARD,
            SystemActionId.HIDE_KEYBOARD -> R.string.action_toggle_keyboard_message

            SystemActionId.SECURE_LOCK_DEVICE -> R.string.action_secure_lock_device_message
            SystemActionId.POWER_ON_OFF_DEVICE -> R.string.action_power_on_off_device_message

            else -> null
        }

        if (messageToShow != null) {
            showDialog(
                "show_system_action_message",
                DialogUi.OkMessage(message = getString(messageToShow))
            )
        }
    }

    private fun buildState(query: String?): SystemActionListState {
        val groupedModels = SystemActionId.values().groupBy { SystemActionUtils.getCategory(it) }
        var unsupportedActions = false

        val listItems = sequence {
            groupedModels.forEach { (category, children) ->
                val childrenListItems = mutableListOf<SystemActionListItem>()

                for (systemActionId in children) {
                    if (isSystemActionSupported.invoke(systemActionId) != null) {
                        unsupportedActions = true
                        continue
                    }

                    val title = getString(SystemActionUtils.getTitle(systemActionId))

                    if (!title.containsQuery(query)) {
                        continue
                    }

                    val icon = SystemActionUtils.getIcon(systemActionId)?.let {
                        getDrawable(it)
                    }

                    val requiresRoot = SystemActionUtils.getRequiredPermissions(systemActionId)
                        .contains(Constants.PERMISSION_ROOT)

                    childrenListItems.add(
                        SystemActionListItem(
                            systemActionId = systemActionId,
                            title = title,
                            icon = icon,
                            showRequiresRootMessage = requiresRoot
                        )
                    )
                }

                if (childrenListItems.isNotEmpty()) {

                    val sectionHeader = SectionHeaderListItem(
                        id = category.toString(),
                        text = getString(SystemActionUtils.getCategoryLabel(category))
                    )

                    yield(sectionHeader)
                    yieldAll(childrenListItems)
                }
            }
        }.toList()

        return SystemActionListState(listItems.createListState(), unsupportedActions)
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val resourceProvider: ResourceProvider,
        private val isSystemActionSupported: IsSystemActionSupportedUseCase,
        private val getInputMethods: GetEnabledInputMethodsUseCase,
        private val getInstalledPackages: GetPackagesUseCase,
        private val appUiAdapter: AppUiAdapter

    ) :
        ViewModelProvider.NewInstanceFactory() {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return SystemActionListViewModel(
                resourceProvider,
                isSystemActionSupported,
                getInputMethods,
                getInstalledPackages,
                appUiAdapter
            ) as T
        }
    }
}

data class SystemActionListState(
    val listItems: ListUiState<ListItem>,
    val showUnsupportedActionsMessage: Boolean
)