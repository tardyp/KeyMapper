package io.github.sds100.keymapper.data.viewmodel

import androidx.lifecycle.*
import com.hadilq.liveevent.LiveEvent
import io.github.sds100.keymapper.data.model.FingerprintMapListItemModel
import io.github.sds100.keymapper.data.repository.FingerprintMapRepository
import io.github.sds100.keymapper.domain.usecases.ListFingerprintMapsUseCase
import io.github.sds100.keymapper.domain.usecases.ShowDeviceInfoUseCase
import io.github.sds100.keymapper.util.*
import io.github.sds100.keymapper.util.delegate.IModelState
import io.github.sds100.keymapper.util.result.Failure
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class FingerprintMapListViewModel(
    private val repository: FingerprintMapRepository,
    private val showDeviceInfoUseCase: ShowDeviceInfoUseCase,
    private val listUseCase: ListFingerprintMapsUseCase
) : ViewModel(), IModelState<List<FingerprintMapListItemModel>> {

    private val fingerprintGestureMaps =
        combine(
            repository.swipeDown,
            repository.swipeUp,
            repository.swipeLeft,
            repository.swipeRight
        ) { swipeDown, swipeUp, swipeLeft, swipeRight ->
            mapOf(
                FingerprintMapUtils.SWIPE_DOWN to swipeDown,
                FingerprintMapUtils.SWIPE_UP to swipeUp,
                FingerprintMapUtils.SWIPE_LEFT to swipeLeft,
                FingerprintMapUtils.SWIPE_RIGHT to swipeRight
            )
        }

    private val _fingerprintGesturesAvailable = MutableLiveData<Boolean>()
    val fingerprintGesturesAvailable: LiveData<Boolean> = _fingerprintGesturesAvailable

    private val _eventStream = LiveEvent<Event>().apply {
        addSource(fingerprintGestureMaps.asLiveData()) {
            //this is important to prevent events being sent in the wrong order
            viewModelScope.launch {
                postValue(
                    BuildFingerprintMapModels(
                        it,
                        showDeviceInfoUseCase.getAll(),
                        listUseCase.hasRootPermission,
                        showDeviceInfoUseCase.showDeviceDescriptors
                    )
                )
            }
        }
    }

    val eventStream: LiveData<Event> = _eventStream

    private val _model = MutableLiveData<DataState<List<FingerprintMapListItemModel>>>()
    override val model = _model
    override val viewState = MutableLiveData<ViewState>(ViewLoading())

    init {
        viewModelScope.launch {
            repository.fingerprintGesturesAvailable.collect {
                _fingerprintGesturesAvailable.value = it
            }
        }
    }

    fun setModels(models: List<FingerprintMapListItemModel>) {
        _model.value = Data(models)
    }

    fun setEnabled(id: String, isEnabled: Boolean) = repository.updateGesture(id) {
        it.copy(isEnabled = isEnabled)
    }

    fun rebuildModels() {
        _model.value = Loading()

        viewModelScope.launch {
            fingerprintGestureMaps.firstOrNull()?.let {
                _eventStream.postValue(
                    BuildFingerprintMapModels(
                        it,
                        showDeviceInfoUseCase.getAll(),
                        listUseCase.hasRootPermission,
                        showDeviceInfoUseCase.showDeviceDescriptors
                    )
                )
            }
        }
    }

    fun fixError(failure: Failure) {
        _eventStream.value = FixFailure(failure)
    }

    fun backupAll() = run { _eventStream.value = BackupFingerprintMaps() }

    fun requestReset() = run { _eventStream.value = RequestFingerprintMapReset() }

    fun reset() = repository.reset()

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val repository: FingerprintMapRepository,
        private val showDeviceInfoUseCase: ShowDeviceInfoUseCase,
        private val listUseCase: ListFingerprintMapsUseCase
    ) : ViewModelProvider.NewInstanceFactory() {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return FingerprintMapListViewModel(repository, showDeviceInfoUseCase, listUseCase) as T
        }
    }
}