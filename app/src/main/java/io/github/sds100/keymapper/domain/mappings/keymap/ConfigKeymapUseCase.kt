package io.github.sds100.keymapper.domain.mappings.keymap

import io.github.sds100.keymapper.data.repository.KeymapRepository
import io.github.sds100.keymapper.domain.actions.ActionWithOptions
import io.github.sds100.keymapper.domain.actions.ConfigActionsUseCaseImpl
import io.github.sds100.keymapper.domain.constraints.ConfigConstraintUseCaseImpl
import io.github.sds100.keymapper.domain.models.Action
import io.github.sds100.keymapper.domain.models.KeyMap
import io.github.sds100.keymapper.domain.models.KeymapActionOptions
import io.github.sds100.keymapper.domain.models.TriggerKey
import io.github.sds100.keymapper.domain.trigger.TriggerMode
import io.github.sds100.keymapper.util.Data
import io.github.sds100.keymapper.util.DataState
import io.github.sds100.keymapper.util.mapData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.*

/**
 * Created by sds100 on 16/02/2021.
 */
class ConfigKeymapUseCaseImpl(
    private val keymapRepository: KeymapRepository
) :
    ConfigKeymapUseCase {

    private var dbId: Long = KeyMap.NEW_ID

    override val uid = MutableStateFlow(UUID.randomUUID().toString())

    override val isEnabled = MutableStateFlow(true)

    val configActions =
        ConfigActionsUseCaseImpl(::invalidateActionOptions, ::getDefaultActionOptions)

    val configTrigger = ConfigKeymapTriggerUseCaseImpl()

    val configConstraints =
        ConfigConstraintUseCaseImpl()

    private fun invalidateActionOptions(
        actionList: List<ActionWithOptions<KeymapActionOptions>>
    ) = configTrigger.getTrigger().let { state ->
        when (state) {
            is Data -> actionList.map { action ->
                invalidateActionOption(state.data.keys, state.data.mode, action)
            }

            else -> actionList
        }
    }

    private fun invalidateActionOption(
        triggerKeys: List<TriggerKey>,
        triggerMode: TriggerMode,
        action: ActionWithOptions<KeymapActionOptions>
    ) = ActionWithOptions<KeymapActionOptions>(
        action = action.action
    )

    private fun getDefaultActionOptions(action: Action): KeymapActionOptions {

    }

    override fun loadBlankKeymap() {
        setKeymap(KeyMap())
    }

    override fun setKeymap(keymap: KeyMap) {
        dbId = keymap.dbId

        /* this must be before everything else because action options might be deselected if
        * there is no trigger. issue #593 */
        configTrigger.setTrigger(keymap.trigger)
        configActions.setActionList(keymap.actionList)

        //TODO constraint list
        isEnabled.value = keymap.isEnabled
        uid.value = keymap.uid
    }

    override fun getKeymap(): DataState<KeyMap> =
        configTrigger.getTrigger().mapData { trigger ->
            KeyMap(
                dbId = dbId,
                uid = uid.value,
                trigger = trigger,
                actionList = configActions.actionList.value,
                constraintList = configConstraints.constraintList.value,
                constraintMode = configConstraints.mode.value,
                isEnabled = isEnabled.value
            )
        }
}

interface ConfigKeymapUseCase : GetKeymapUidUseCase {
    override val uid: StateFlow<String>

    val isEnabled: StateFlow<Boolean>
    fun setEnabled(enabled: Boolean)

    fun loadBlankKeymap()

    fun setKeymap(keymap: KeyMap)
    fun getKeymap(): DataState<KeyMap>
}