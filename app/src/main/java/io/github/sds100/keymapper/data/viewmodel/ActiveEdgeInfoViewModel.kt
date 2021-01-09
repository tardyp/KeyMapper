package io.github.sds100.keymapper.data.viewmodel

import androidx.lifecycle.*
import com.hadilq.liveevent.LiveEvent
import io.github.sds100.keymapper.data.model.ActiveEdgeListItemModel
import io.github.sds100.keymapper.data.repository.ActiveEdgeMapRepository
import io.github.sds100.keymapper.data.repository.DeviceInfoRepository
import io.github.sds100.keymapper.util.*
import io.github.sds100.keymapper.util.result.Failure
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class ActiveEdgeInfoViewModel(
    private val mRepository: ActiveEdgeMapRepository,
    private val mDeviceInfoRepository: DeviceInfoRepository
) : ViewModel() {

    private val _model = MutableLiveData<State<ActiveEdgeListItemModel>>(Loading())

    val activeEdgeAvailable = mRepository.activeEdgeAvailable

    val model: LiveData<State<ActiveEdgeListItemModel>> = _model

    private val _eventStream = LiveEvent<Event>().apply {
        addSource(mRepository.activeEdgeMapLiveData) {
            //this is important to prevent events being sent in the wrong order
            postValue(BuildActiveEdgeListModel(it))
        }
    }

    val eventStream: LiveData<Event> = _eventStream

    fun setModels(model: ActiveEdgeListItemModel) {
        _model.value = Data(model)
    }

    fun setEnabled(isEnabled: Boolean) = viewModelScope.launch {
        mRepository.edit {
            it.copy(isEnabled = isEnabled)
        }
    }

    fun rebuildModels() {
        viewModelScope.launch {
            _model.value = Loading()

            mRepository.activeEdgeMap.firstOrNull()?.let {
                _eventStream.postValue(BuildActiveEdgeListModel(it))
            }
        }
    }

    fun fixError(failure: Failure) {
        _eventStream.value = FixFailure(failure)
    }

    fun backupAll() = run { _eventStream.value = BackupActiveEdgeMap() }

    fun requestReset() = run { _eventStream.value = RequestActiveEdgeMapReset() }

    fun reset() {
        viewModelScope.launch {
            mRepository.reset()
        }
    }

    suspend fun getDeviceInfoList() = mDeviceInfoRepository.getAll()

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val mRepository: ActiveEdgeMapRepository,
        private val mDeviceInfoRepository: DeviceInfoRepository
    ) : ViewModelProvider.NewInstanceFactory() {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return ActiveEdgeInfoViewModel(mRepository, mDeviceInfoRepository) as T
        }
    }
}