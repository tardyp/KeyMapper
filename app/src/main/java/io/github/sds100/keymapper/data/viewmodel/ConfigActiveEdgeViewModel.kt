package io.github.sds100.keymapper.data.viewmodel

import android.os.Bundle
import androidx.lifecycle.*
import com.hadilq.liveevent.LiveEvent
import io.github.sds100.keymapper.data.IPreferenceDataStore
import io.github.sds100.keymapper.data.model.*
import io.github.sds100.keymapper.data.model.options.ActiveEdgeActionOptions
import io.github.sds100.keymapper.data.model.options.ActiveEdgeOptions
import io.github.sds100.keymapper.data.repository.ActiveEdgeMapRepository
import io.github.sds100.keymapper.data.repository.DeviceInfoRepository
import io.github.sds100.keymapper.util.EnableAccessibilityServicePrompt
import io.github.sds100.keymapper.util.Event
import io.github.sds100.keymapper.util.FixFailure
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * Created by sds100 on 08/11/20.
 */

class ConfigActiveEdgeViewModel(private val mActiveEdgeMapRepository: ActiveEdgeMapRepository,
                                private val mDeviceInfoRepository: DeviceInfoRepository,
                                preferenceDataStore: IPreferenceDataStore
) : ViewModel(), IPreferenceDataStore by preferenceDataStore {

    companion object {
        private const val MAP_STATE_KEY = "config_active_edge"
    }

    val actionListViewModel = object : ActionListViewModel<ActiveEdgeActionOptions>(
        viewModelScope, mDeviceInfoRepository) {

        override val stateKey = "active_edge_action_list_view_model"

        override fun getActionOptions(action: Action): ActiveEdgeActionOptions {
            return ActiveEdgeActionOptions(
                action,
                actionList.value!!.size
            )
        }
    }

    val optionsViewModel = ActiveEdgeOptionsViewModel()

    val constraintListViewModel = ConstraintListViewModel(viewModelScope)

    val isEnabled = MutableLiveData(true)

    private val _eventStream = LiveEvent<Event>().apply {
        addSource(constraintListViewModel.eventStream) {
            when (it) {
                is FixFailure -> value = it
            }
        }

        addSource(actionListViewModel.eventStream) {
            when (it) {
                is FixFailure, is EnableAccessibilityServicePrompt -> value = it
            }
        }
    }

    val eventStream: LiveData<Event> = _eventStream

    fun save(scope: CoroutineScope) {
        scope.launch {
            val map = createActiveEdgeMap()
            mActiveEdgeMapRepository.edit { map }
        }
    }

    private fun createActiveEdgeMap(): ActiveEdgeMap {
        return ActiveEdgeMap(
            actionList = actionListViewModel.actionList.value ?: listOf(),
            constraintList = constraintListViewModel.constraintList.value ?: listOf(),
            constraintMode = constraintListViewModel.getConstraintMode(),
            isEnabled = isEnabled.value ?: true
        ).let {
            optionsViewModel.options.value?.apply(it) ?: it
        }
    }

    fun loadActiveEdgeMap() {
        viewModelScope.launch {
            mActiveEdgeMapRepository.activeEdgeMap.collect {
                loadActiveEdgeMap(it)
                return@collect
            }
        }
    }

    private fun loadActiveEdgeMap(map: ActiveEdgeMap) {
        actionListViewModel.setActionList(map.actionList)
        constraintListViewModel.setConstraintList(map.constraintList, map.constraintMode)
        isEnabled.value = map.isEnabled
        optionsViewModel.setOptions(ActiveEdgeOptions(map))
    }

    fun saveState(outState: Bundle) {
        outState.putParcelable(MAP_STATE_KEY, createActiveEdgeMap())
    }

    @Suppress("UNCHECKED_CAST")
    fun restoreState(state: Bundle) {
        val map = state.getParcelable<ActiveEdgeMap>(MAP_STATE_KEY) ?: return

        loadActiveEdgeMap(map)
    }

    class Factory(
        private val mActiveEdgeMapRepository: ActiveEdgeMapRepository,
        private val mDeviceInfoRepository: DeviceInfoRepository,
        private val mIPreferenceDataStore: IPreferenceDataStore) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>) =
            ConfigActiveEdgeViewModel(
                mActiveEdgeMapRepository,
                mDeviceInfoRepository,
                mIPreferenceDataStore
            ) as T
    }
}