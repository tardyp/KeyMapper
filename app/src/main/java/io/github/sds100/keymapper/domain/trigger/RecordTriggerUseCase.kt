package io.github.sds100.keymapper.domain.trigger

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Created by sds100 on 04/03/2021.
 */
class RecordTriggerUseCaseImpl() :RecordTriggerUseCase{
    override val state = MutableStateFlow(RecordTriggerState.Stopped
    )
    override val onRecordKey = MutableSharedFlow<RecordedKey>()

    override fun record() {

    }

    override fun stopRecording() {

    }
}

interface RecordTriggerUseCase {
    val state: StateFlow<RecordTriggerState>
    val onRecordKey: SharedFlow<RecordedKey>
    fun record()
    fun stopRecording()
}