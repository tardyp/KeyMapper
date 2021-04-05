package io.github.sds100.keymapper.domain.mappings.keymap.trigger

import io.github.sds100.keymapper.domain.adapter.ServiceAdapter
import io.github.sds100.keymapper.util.*
import io.github.sds100.keymapper.util.result.Result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*

/**
 * Created by sds100 on 04/03/2021.
 */
class RecordTriggerController(
    coroutineScope: CoroutineScope,
    private val serviceAdapter: ServiceAdapter
) : RecordTriggerUseCase {
    override val state = MutableStateFlow<RecordTriggerState>(RecordTriggerState.Stopped)

    override val onRecordKey = serviceAdapter.eventReceiver.mapNotNull { event ->
        when (event) {
            is RecordedTriggerKeyEvent -> {
                val device = if (event.isExternal) {
                    TriggerKeyDevice.External(event.deviceDescriptor, event.deviceName)
                } else {
                    TriggerKeyDevice.Internal
                }

                RecordedKey(event.keyCode, device)
            }

            else -> null
        }
    }

    init {
        serviceAdapter.eventReceiver.onEach { event ->
            when (event) {
                is OnStoppedRecordingTrigger -> state.value = RecordTriggerState.Stopped

                is OnIncrementRecordTriggerTimer ->state.value =  RecordTriggerState.CountingDown(event.timeLeft)
            }
        }.launchIn(coroutineScope)
    }

    override fun startRecording() = serviceAdapter.send(StartRecordingTrigger)
    override fun stopRecording() {
        serviceAdapter.send(StopRecordingTrigger)
    }
}

interface RecordTriggerUseCase {
    val state: Flow<RecordTriggerState>
    val onRecordKey: Flow<RecordedKey>

    /**
     * @return Success if started and an Error if failed to start.
     */
    fun startRecording(): Result<Unit>
    fun stopRecording()
}