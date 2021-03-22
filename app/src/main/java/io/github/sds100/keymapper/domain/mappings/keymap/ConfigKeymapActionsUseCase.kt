package io.github.sds100.keymapper.domain.mappings.keymap

import io.github.sds100.keymapper.domain.actions.ActionData
import io.github.sds100.keymapper.domain.actions.ConfigActionsUseCase
import io.github.sds100.keymapper.domain.actions.KeyEventAction
import io.github.sds100.keymapper.domain.utils.State
import io.github.sds100.keymapper.domain.utils.ifIsData
import io.github.sds100.keymapper.util.KeyEventUtils
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map

/**
 * Created by sds100 on 20/02/2021.
 */

class ConfigKeymapActionsUseCaseImpl(
    private val keymap: StateFlow<State<KeyMap>>,
    val setKeymap: (keymap: KeyMap) -> Unit
) : ConfigKeymapActionsUseCase {

    override val actionList = keymap.map { state ->
        if (state is State.Data) {
            State.Data(state.data.actionList)
        } else {
            State.Loading()
        }
    }

    override fun addAction(action: ActionData) = keymap.value.ifIsData { keymap ->
        setKeymap(keymap.addAction(action))
    }

    override fun moveAction(fromIndex: Int, toIndex: Int) = keymap.value.ifIsData { keymap ->
        setKeymap(keymap.moveAction(fromIndex, toIndex))
    }

    override fun removeAction(uid: String) = keymap.value.ifIsData { keymap ->
        setKeymap(keymap.removeAction(uid))
    }

    override fun setRepeatEnabled(uid: String, enabled: Boolean) = keymap.value.ifIsData { keymap ->
        setKeymap(keymap.setRepeatEnabled(uid, enabled))
    }

    //TODO
    private fun createAction(actionData: ActionData): KeymapActionData {
        var holdDown = false
        var repeat = false

        if (actionData is KeyEventAction) {
            if (KeyEventUtils.isModifierKey(actionData.keyCode)) {
                holdDown = true
                repeat = true
            } else {
                repeat = true
            }
        }

        return KeymapActionData(
            data = actionData,
            repeat = repeat,
            holdDown = holdDown
        )
    }

}

interface ConfigKeymapActionsUseCase : ConfigActionsUseCase<KeymapAction> {
    fun setRepeatEnabled(uid: String, enabled: Boolean)
}