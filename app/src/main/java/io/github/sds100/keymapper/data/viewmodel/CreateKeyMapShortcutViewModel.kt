package io.github.sds100.keymapper.data.viewmodel

import android.content.Intent
import androidx.lifecycle.*
import io.github.sds100.keymapper.R
import io.github.sds100.keymapper.domain.mappings.keymap.KeyMap
import io.github.sds100.keymapper.domain.mappings.keymap.SaveKeyMapUseCase
import io.github.sds100.keymapper.domain.utils.State
import io.github.sds100.keymapper.framework.adapters.ResourceProvider
import io.github.sds100.keymapper.mappings.keymaps.ListKeyMapsUseCase
import io.github.sds100.keymapper.ui.*
import io.github.sds100.keymapper.ui.dialogs.GetUserResponse
import io.github.sds100.keymapper.ui.mappings.keymap.KeyMapListItem
import io.github.sds100.keymapper.ui.mappings.keymap.KeyMapListItemCreator
import io.github.sds100.keymapper.ui.shortcuts.CreateKeyMapShortcutUseCase
import io.github.sds100.keymapper.ui.utils.SelectionState
import io.github.sds100.keymapper.util.*
import io.github.sds100.keymapper.util.result.FixableError
import io.github.sds100.keymapper.util.result.getFullMessage
import io.github.sds100.keymapper.util.result.onFailure
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * Created by sds100 on 08/09/20.
 */
class CreateKeyMapShortcutViewModel(
    private val saveKeyMapUseCase: SaveKeyMapUseCase,
    private val listUseCase: ListKeyMapsUseCase,
    private val createShortcutUseCase: CreateKeyMapShortcutUseCase,
    resourceProvider: ResourceProvider
) : ViewModel(), UserResponseViewModel by UserResponseViewModelImpl(),
    ResourceProvider by resourceProvider {

    private val listItemCreator = KeyMapListItemCreator(listUseCase, resourceProvider)

    private val _state = MutableStateFlow<ListUiState<KeyMapListItem>>(ListUiState.Loading)
    val state = _state.asStateFlow()

    private val _returnUidResult = MutableSharedFlow<Intent>()
    val returnUidResult = _returnUidResult.asSharedFlow()

    init {
        val rebuildUiState = MutableSharedFlow<State<List<KeyMap>>>(replay = 1)

        viewModelScope.launch {
            rebuildUiState.collectLatest { state ->

                if (state !is State.Data) {
                    _state.value = ListUiState.Loading
                    return@collectLatest
                }

                val selectionUiState =
                    KeyMapListItem.SelectionUiState(isSelected = false, isSelectable = false)

                _state.value = state.data.map {
                    val keyMapListUiState = listItemCreator.map(it)

                    KeyMapListItem(keyMapListUiState, selectionUiState)
                }.createListState()

            }
        }

        viewModelScope.launch {
            listUseCase.keyMapList.collectLatest {
                rebuildUiState.emit(it)
            }
        }

        viewModelScope.launch {
            listUseCase.invalidateErrors.drop(1).collectLatest {
                /*
                Don't get the key maps from the repository because there can be a race condition
                when restoring key maps. This happens because when the activity is resumed the
                key maps in the repository are being updated and this flow is collected
                at the same time.
                 */
                rebuildUiState.emit(rebuildUiState.first())
            }
        }
    }

    fun onKeyMapCardClick(uid: String) {
        viewModelScope.launch {
            val state = listUseCase.keyMapList.first { it is State.Data }

            if (state !is State.Data) return@launch

            val keyMap = state.data.find { it.uid == uid } ?: return@launch

            if (!keyMap.trigger.triggerFromOtherApps) {
                val newKeyMap = keyMap.copy(
                    trigger = keyMap.trigger.copy(triggerFromOtherApps = true)
                )

                saveKeyMapUseCase.invoke(newKeyMap)
            }

            val intent = if (keyMap.actionList.size == 1) {
                createShortcutUseCase.createIntentForSingleAction(keyMap.uid, keyMap.actionList[0])
            } else {

                val key = "create_launcher_shortcut"
                val response = getUserResponse(
                    key,
                    GetUserResponse.Text(
                        getString(R.string.hint_shortcut_name),
                        allowEmpty = false
                    )
                ) ?: return@launch

                createShortcutUseCase.createIntentForMultipleActions(
                    keyMapUid = keyMap.uid,
                    shortcutLabel = response.text
                )
            }

            _returnUidResult.emit(intent)
        }
    }


    fun onActionChipClick(chipModel: ChipUi) {
        if (chipModel is ChipUi.FixableError) {
            showSnackBarAndFixError(chipModel.error)
        }
    }

    fun onTriggerErrorChipClick(chipModel: ChipUi) {
        if (chipModel is ChipUi.FixableError) {
            showSnackBarAndFixError(chipModel.error)
        }
    }

    fun onConstraintsChipClick(chipModel: ChipUi) {
        if (chipModel is ChipUi.FixableError) {
            showSnackBarAndFixError(chipModel.error)
        }
    }

    private fun showSnackBarAndFixError(error: FixableError) {
        viewModelScope.launch {
            val snackBar = GetUserResponse.SnackBar(
                title = error.getFullMessage(this@CreateKeyMapShortcutViewModel),
                actionText = getString(R.string.snackbar_fix)
            )

            getUserResponse("fix_error", snackBar) ?: return@launch

            listUseCase.fixError(error)
        }
    }

    class Factory(
        private val saveKeyMapUseCase: SaveKeyMapUseCase,
        private val listUseCase: ListKeyMapsUseCase,
        private val createShortcutUseCase: CreateKeyMapShortcutUseCase,
        private val resourceProvider: ResourceProvider
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>) =
            CreateKeyMapShortcutViewModel(
                saveKeyMapUseCase, listUseCase, createShortcutUseCase, resourceProvider
            ) as T
    }
}