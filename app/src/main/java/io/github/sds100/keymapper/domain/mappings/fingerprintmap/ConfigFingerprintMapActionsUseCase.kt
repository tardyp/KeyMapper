package io.github.sds100.keymapper.domain.mappings.fingerprintmap

import io.github.sds100.keymapper.domain.actions.ActionData
import io.github.sds100.keymapper.domain.actions.ConfigActionsUseCase
import io.github.sds100.keymapper.domain.utils.State
import io.github.sds100.keymapper.domain.utils.ifIsData
import io.github.sds100.keymapper.domain.utils.moveElement
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map

/**
 * Created by sds100 on 20/02/2021.
 */

class ConfigFingerprintMapActionsUseCaseImpl(
    private val fingerprintMapFlow: StateFlow<State<FingerprintMap>>,
    val setFingerprintMap: (keymap: FingerprintMap) -> Unit
) : ConfigFingerprintMapActionsUseCase {

    override val actionList = fingerprintMapFlow.map { state ->
        if (state is State.Data) {
            State.Data(state.data.actionList)
        } else {
            State.Loading()
        }
    }

    override fun addAction(action: ActionData) =
        fingerprintMapFlow.value.ifIsData { fingerprintMap ->
            fingerprintMap.actionDataList.toMutableList().apply {
                add(createAction(action))
                setFingerprintMap(fingerprintMap.copy(actionDataList = this))
            }
        }

    override fun moveAction(fromIndex: Int, toIndex: Int) =
        fingerprintMapFlow.value.ifIsData { keymap ->
            keymap.actionDataList.toMutableList().apply {
                moveElement(fromIndex, toIndex)
                setFingerprintMap(keymap.copy(actionDataList = this))
            }
        }

    override fun removeAction(uid: String) = fingerprintMapFlow.value.ifIsData { keymap ->
        keymap.actionDataList.toMutableList().apply {
            removeAll { it.uid == uid }
            setFingerprintMap(keymap.copy(actionDataList = this))
        }
    }

    private fun createAction(actionData: ActionData) = FingerprintMapActionData(data = actionData)

    private fun setActionOption(
        uid: String,
        block: (action: FingerprintMapActionData) -> FingerprintMapActionData
    ) {
        fingerprintMapFlow.value.ifIsData { fingerprintMap ->
            fingerprintMap.actionDataList.map {
                if (it.uid == uid) {
                    block.invoke(it)
                } else {
                    it
                }
            }.let {
                setFingerprintMap.invoke(fingerprintMap.copy(actionDataList = it))
            }
        }
    }
}

interface ConfigFingerprintMapActionsUseCase : ConfigActionsUseCase<FingerprintMapAction>