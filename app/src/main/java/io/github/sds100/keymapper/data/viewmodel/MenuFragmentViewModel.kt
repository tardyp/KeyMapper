package io.github.sds100.keymapper.data.viewmodel

import androidx.lifecycle.*
import com.hadilq.liveevent.LiveEvent
import io.github.sds100.keymapper.data.repository.FingerprintMapRepository
import io.github.sds100.keymapper.data.usecase.MenuKeymapUseCase
import io.github.sds100.keymapper.domain.usecases.ControlKeymapsPausedState
import io.github.sds100.keymapper.util.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * Created by sds100 on 17/11/20.
 */
class MenuFragmentViewModel(
    private val keymapUseCase: MenuKeymapUseCase,
    private val fingerprintMapRepository: FingerprintMapRepository,
    private val manageKeymapsUseCase: ControlKeymapsPausedState
) : ViewModel() {

    private val _keymapsPaused = MutableStateFlow(false)
    val keymapsPaused: StateFlow<Boolean> = _keymapsPaused
    val accessibilityServiceEnabled = MutableLiveData(false)

    private val _eventStream = LiveEvent<Event>()
    val eventStream: LiveData<Event> = _eventStream

    init {
        viewModelScope.launch {
            manageKeymapsUseCase.keymapsPaused.collect {
                _keymapsPaused.value = it
            }
        }
    }

    fun enableAll() {
        keymapUseCase.enableAll()

        FingerprintMapUtils.GESTURES.forEach { gestureId ->
            fingerprintMapRepository.updateGesture(gestureId) {
                it.copy(isEnabled = true)
            }
        }
    }

    fun disableAll() {
        keymapUseCase.disableAll()

        FingerprintMapUtils.GESTURES.forEach { gestureId ->
            fingerprintMapRepository.updateGesture(gestureId) {
                it.copy(isEnabled = false)
            }
        }
    }

    fun chooseKeyboard() = run { _eventStream.value = ChooseKeyboard() }
    fun openSettings() = run { _eventStream.value = OpenSettings() }
    fun openAbout() = run { _eventStream.value = OpenAbout() }
    fun sendFeedback() = run { _eventStream.value = SendFeedback() }
    fun backupAll() = run { _eventStream.value = RequestBackupAll() }
    fun restore() = run { _eventStream.value = RequestRestore() }
    fun resumeKeymaps() = run { manageKeymapsUseCase.resumeKeymaps() }
    fun pauseKeymaps() = run { manageKeymapsUseCase.pauseKeymaps() }
    fun enableAccessibilityService() = run { _eventStream.value = EnableAccessibilityService() }

    @Suppress("UNCHECKED_CAST")
    class Factory(
        private val keymapUseCase: MenuKeymapUseCase,
        private val fingerprintMapRepository: FingerprintMapRepository,
        private val manageKeymapsUseCase: ControlKeymapsPausedState
    ) : ViewModelProvider.NewInstanceFactory() {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return MenuFragmentViewModel(
                keymapUseCase,
                fingerprintMapRepository,
                manageKeymapsUseCase
            ) as T
        }
    }
}