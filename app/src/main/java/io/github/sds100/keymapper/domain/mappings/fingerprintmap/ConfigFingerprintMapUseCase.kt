package io.github.sds100.keymapper.domain.mappings.fingerprintmap

import io.github.sds100.keymapper.constraints.ConstraintState
import io.github.sds100.keymapper.domain.actions.ActionData
import io.github.sds100.keymapper.domain.mappings.keymap.KeyMapAction
import io.github.sds100.keymapper.domain.utils.ifIsData
import io.github.sds100.keymapper.mappings.common.BaseConfigMappingUseCase
import io.github.sds100.keymapper.mappings.common.ConfigMappingUseCase

/**
 * Created by sds100 on 16/02/2021.
 */
class ConfigFingerprintMapUseCaseImpl : BaseConfigMappingUseCase<FingerprintMapAction, FingerprintMap>(), ConfigFingerprintMapUseCase {

    override fun setEnabled(enabled: Boolean) = editFingerprintMap { it.copy(isEnabled = enabled) }

    override fun createAction(data: ActionData): FingerprintMapAction {
        return FingerprintMapAction(data = data)
    }

    override fun setActionList(actionList: List<FingerprintMapAction>) {
        editFingerprintMap { it.copy(actionList = actionList) }
    }

    override fun setConstraintState(constraintState: ConstraintState) {
        editFingerprintMap { it.copy(constraintState = constraintState) }
    }

    override fun setActionMultiplier(uid: String, multiplier: Int?) {
        setActionOption(uid){it.copy(multiplier = multiplier)}
    }

    override fun setDelayBeforeNextAction(uid: String, delay: Int?) {
        setActionOption(uid){it.copy(delayBeforeNextAction = delay)}
    }

    private fun editFingerprintMap(block: (fingerprintMap: FingerprintMap) -> FingerprintMap) {
        mapping.value.ifIsData { setMapping(block.invoke(it)) }
    }

    private fun setActionOption(
        uid: String,
        block: (action: FingerprintMapAction) -> FingerprintMapAction
    ) {
        editFingerprintMap { fingerprintMap ->
            val newActionList = fingerprintMap.actionList.map { action ->
                if (action.uid == uid){
                    block.invoke(action)
                }else{
                    action
                }
            }

            fingerprintMap.copy(
                actionList = newActionList
            )
        }
    }
}

interface ConfigFingerprintMapUseCase : ConfigMappingUseCase<FingerprintMapAction, FingerprintMap>