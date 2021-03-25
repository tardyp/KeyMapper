package io.github.sds100.keymapper.data.viewmodel

import androidx.lifecycle.*
import com.hadilq.liveevent.LiveEvent
import io.github.sds100.keymapper.data.model.TriggerEntity
import io.github.sds100.keymapper.data.usecase.CreateKeymapShortcutUseCase
import io.github.sds100.keymapper.domain.actions.GetActionErrorUseCase
import io.github.sds100.keymapper.ui.mappings.keymap.KeymapListItem
import io.github.sds100.keymapper.util.*
import io.github.sds100.keymapper.util.delegate.ModelState
import io.github.sds100.keymapper.util.result.Error
import kotlinx.coroutines.launch
import splitties.bitflags.withFlag

/**
 * Created by sds100 on 08/09/20.
 */
class CreateKeymapShortcutViewModel(
    private val keymapRepository: CreateKeymapShortcutUseCase,
    private val showActionsUseCase: GetActionErrorUseCase
) : ViewModel(), ModelState<List<KeymapListItem>> {

    private val _model: MutableLiveData<OldDataState<List<KeymapListItem>>> =
        MutableLiveData(Loading())

    override val model = _model
    override val viewState = MutableLiveData<ViewState>(ViewLoading())

    private val _eventStream = LiveEvent<Event>().apply {
        addSource(keymapRepository.keymapList) {
            viewModelScope.launch {
                //TODO
//                postValue(
//                    BuildKeymapListModels(
//                        it,
//                        showActionsUseCase.getDeviceInfo(),
//                        showActionsUseCase.hasRootPermission,
//                        showActionsUseCase.showDeviceDescriptors
//                    )
//                )
            }
        }
    }

    val eventStream: LiveData<Event> = _eventStream

  override fun rebuildModels() {
      viewModelScope.launch {

          if (keymapRepository.keymapList.value == null) return@launch

          if (keymapRepository.keymapList.value?.isEmpty() == true) {
              _model.value = Empty()
              return@launch
          }

          _model.value = Loading()

//            _eventStream.postValue(
                //TODO
//                BuildKeymapListModels(
//                    keymapRepository.keymapList.value ?: emptyList(),
//                    showActionsUseCase.getDeviceInfo(),
//                    showActionsUseCase.hasRootPermission,
//                    showActionsUseCase.showDeviceDescriptors
//                )
//            )
        }
    }

    fun setModelList(list: List<KeymapListItem>) {
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
                    it.trigger.flags.withFlag(TriggerEntity.TRIGGER_FLAG_FROM_OTHER_APPS)

                val newKeymap = it.copy(trigger = it.trigger.copy(flags = newTriggerFlags))

                viewModelScope.launch {
                    keymapRepository.updateKeymap(newKeymap)

                    //TODO
//                    _eventStream.value = CreateKeymapShortcutEvent(
//                        uid,
//                        newKeymap.actionList,
//                        showActionsUseCase.getDeviceInfo(),
//                        showActionsUseCase.showDeviceDescriptors
//                    )
                }
            }
    }

    fun fixError(error: Error) {
        _eventStream.value = FixFailure(error)
    }

    class Factory(
        private val keymapRepository: CreateKeymapShortcutUseCase,
        private val showActionsUseCase: GetActionErrorUseCase
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>) =
            CreateKeymapShortcutViewModel(
                keymapRepository,
                showActionsUseCase
            ) as T
    }
}