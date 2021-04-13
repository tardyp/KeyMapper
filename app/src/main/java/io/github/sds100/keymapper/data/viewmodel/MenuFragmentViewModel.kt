package io.github.sds100.keymapper.data.viewmodel

import androidx.lifecycle.*
import com.hadilq.liveevent.LiveEvent
import io.github.sds100.keymapper.data.repository.FingerprintMapRepository
import io.github.sds100.keymapper.data.usecase.MenuKeymapUseCase
import io.github.sds100.keymapper.domain.usecases.ControlKeymapsPausedState
import io.github.sds100.keymapper.framework.adapters.ResourceProvider
import io.github.sds100.keymapper.home.HomeScreenUseCase
import io.github.sds100.keymapper.util.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * Created by sds100 on 17/11/20.
 */
class MenuFragmentViewModel(
    private val keymapUseCase: MenuKeymapUseCase,
    private val fingerprintMapRepository: FingerprintMapRepository,
    private val controlKeymapsPausedState: ControlKeymapsPausedState
) : ViewModel() {

    private val _keymapsPaused = MutableLiveData(false)
    val keymapsPaused: LiveData<Boolean> = _keymapsPaused
    val accessibilityServiceEnabled = MutableLiveData(false)

    private val _eventStream = LiveEvent<Event>()
    val eventStream: LiveData<Event> = _eventStream

    init {
        controlKeymapsPausedState.keymapsPaused.onEach {
            _keymapsPaused.value = it
        }.launchIn(viewModelScope)
    }

    fun enableAll() {
        keymapUseCase.enableAll()

        //TODO fingerprint
//        FingerprintMapUtils.GESTURES.forEach { gestureId ->
        //TODO
//            fingerprintMapRepository.updateGesture(gestureId) {
//                it.copy(isEnabled = true)
//            }
//        }
    }

    fun disableAll() {
        //TODO move this a use case called EnableDisableMappingsUseCase
        keymapUseCase.disableAll()

//        FingerprintMapUtils.GESTURES.forEach { gestureId ->
        //TODO
//            fingerprintMapRepository.updateGesture(gestureId) {
//                it.copy(isEnabled = false)
//            }
//        }
    }

    fun chooseKeyboard() = run { _eventStream.value = ChooseKeyboard() }
    fun openSettings() = run { _eventStream.value = OpenSettings() }
    fun openAbout() = run { _eventStream.value = OpenAbout() }
    fun sendFeedback() = run { _eventStream.value = SendFeedback() }
    fun backupAll() = run { _eventStream.value = RequestBackupAll() }
    fun restore() = run { _eventStream.value = RequestRestore() }
    fun resumeKeymaps() = run { controlKeymapsPausedState.resumeKeymaps() }
    fun pauseKeymaps() = run { controlKeymapsPausedState.pauseKeymaps() }
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