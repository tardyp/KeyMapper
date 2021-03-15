package io.github.sds100.keymapper.data.viewmodel

import androidx.lifecycle.*
import com.hadilq.liveevent.LiveEvent
import io.github.sds100.keymapper.data.model.KeymapListItemModel
import io.github.sds100.keymapper.data.usecase.KeymapListUseCase
import io.github.sds100.keymapper.domain.actions.GetActionErrorUseCase
import io.github.sds100.keymapper.util.*
import io.github.sds100.keymapper.util.delegate.ModelState
import io.github.sds100.keymapper.util.result.Error
import kotlinx.coroutines.launch

class KeymapListViewModel internal constructor(
    private val keymapRepository: KeymapListUseCase,
    private val showActionsUseCase: GetActionErrorUseCase,
) : ViewModel(), ModelState<List<KeymapListItemModel>> {

    private val _model: MutableLiveData<DataState<List<KeymapListItemModel>>> =
        MutableLiveData(Loading())

    override val model = _model
    override val viewState = MutableLiveData<ViewState>(ViewLoading())

    val selectionProvider: ISelectionProvider = SelectionProvider()

    private val _eventStream = LiveEvent<Event>().apply {
        addSource(keymapRepository.keymapList) {
            viewModelScope.launch {
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

    fun duplicate(vararg id: Long) = keymapRepository.duplicateKeymap(*id)
    fun delete(vararg id: Long) = keymapRepository.deleteKeymap(*id)
    fun enableSelectedKeymaps() = keymapRepository.enableKeymapById(*selectionProvider.selectedIds)
    fun disableSelectedKeymaps() =
        keymapRepository.disableKeymapById(*selectionProvider.selectedIds)

    fun enableAll() = keymapRepository.enableAll()
    fun disableAll() = keymapRepository.disableAll()

    fun rebuildModels() {
        if (keymapRepository.keymapList.value == null) return

        if (keymapRepository.keymapList.value?.isEmpty() == true) {
            _model.value = Empty()
            return
        }

        _model.value = Loading()

        viewModelScope.launch {
//            _eventStream.postValue(
//                BuildKeymapListModels(
//                    keymapRepository.keymapList.value ?: emptyList(),
//                    showActionsUseCase.getDeviceInfo(),
//                    showActionsUseCase.hasRootPermission,
//                    showActionsUseCase.showDeviceDescriptors
//                )
//            )
        }
    }

    fun setModelList(list: List<KeymapListItemModel>) {
        selectionProvider.updateIds(list.map { it.id }.toLongArray())

        _model.value = when {
            list.isEmpty() -> Empty()
            else -> Data(list)
        }
    }

    fun requestBackupSelectedKeymaps() = run { _eventStream.value = RequestBackupSelectedKeymaps() }

    fun fixError(error: Error) {
        _eventStream.value = FixFailure(error)
    }

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val keymapListUseCase: KeymapListUseCase,
        private val showActionsUseCase: GetActionErrorUseCase
    ) : ViewModelProvider.NewInstanceFactory() {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return KeymapListViewModel(
                keymapListUseCase,
                showActionsUseCase
            ) as T
        }
    }
}