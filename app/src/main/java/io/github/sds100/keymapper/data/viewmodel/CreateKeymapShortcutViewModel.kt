package io.github.sds100.keymapper.data.viewmodel

import androidx.lifecycle.*
import com.hadilq.liveevent.LiveEvent
import io.github.sds100.keymapper.data.model.KeymapListItemModel
import io.github.sds100.keymapper.data.model.Trigger
import io.github.sds100.keymapper.data.usecase.CreateKeymapShortcutUseCase
import io.github.sds100.keymapper.domain.usecases.ShowActionsUseCase
import io.github.sds100.keymapper.util.*
import io.github.sds100.keymapper.util.delegate.IModelState
import io.github.sds100.keymapper.util.result.Failure
import kotlinx.coroutines.launch
import splitties.bitflags.withFlag

/**
 * Created by sds100 on 08/09/20.
 */
class CreateKeymapShortcutViewModel(
    private val keymapRepository: CreateKeymapShortcutUseCase,
    private val showActionsUseCase: ShowActionsUseCase
) : ViewModel(), IModelState<List<KeymapListItemModel>> {

    private val _model: MutableLiveData<DataState<List<KeymapListItemModel>>> =
        MutableLiveData(Loading())

    override val model = _model
    override val viewState = MutableLiveData<ViewState>(ViewLoading())

    private val _eventStream = LiveEvent<Event>().apply {
        addSource(keymapRepository.keymapList) {
            viewModelScope.launch {
                postValue(
                    BuildKeymapListModels(
                        it,
                        showActionsUseCase.getDeviceInfo(),
                        showActionsUseCase.hasRootPermission,
                        showActionsUseCase.showDeviceDescriptors
                    )
                )
            }
        }
    }

    val eventStream: LiveData<Event> = _eventStream

    fun rebuildModels() {
        viewModelScope.launch {

            if (keymapRepository.keymapList.value == null) return@launch

            if (keymapRepository.keymapList.value?.isEmpty() == true) {
                _model.value = Empty()
                return@launch
            }

            _model.value = Loading()

            _eventStream.postValue(
                BuildKeymapListModels(
                    keymapRepository.keymapList.value ?: emptyList(),
                    showActionsUseCase.getDeviceInfo(),
                    showActionsUseCase.hasRootPermission,
                    showActionsUseCase.showDeviceDescriptors
                )
            )
        }
    }

    fun setModelList(list: List<KeymapListItemModel>) {
        _model.value = when {
            list.isEmpty() -> Empty()
            else -> Data(list)
        }
    }

    fun chooseKeymap(uid: String) {
        keymapRepository.keymapList.value
            ?.find { it.uid == uid }
            ?.let {
                val newTriggerFlags =
                    it.trigger.flags.withFlag(Trigger.TRIGGER_FLAG_FROM_OTHER_APPS)

                val newKeymap = it.copy(trigger = it.trigger.copy(flags = newTriggerFlags))

                viewModelScope.launch {
                    keymapRepository.updateKeymap(newKeymap)

                    _eventStream.value = CreateKeymapShortcutEvent(
                        uid,
                        newKeymap.actionList,
                        showActionsUseCase.getDeviceInfo(),
                        showActionsUseCase.showDeviceDescriptors
                    )
                }
            }
    }

    fun fixError(failure: Failure) {
        _eventStream.value = FixFailure(failure)
    }

    class Factory(
        private val keymapRepository: CreateKeymapShortcutUseCase,
        private val showActionsUseCase: ShowActionsUseCase
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>) =
            CreateKeymapShortcutViewModel(
                keymapRepository,
                showActionsUseCase
            ) as T
    }
}