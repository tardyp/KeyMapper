package io.github.sds100.keymapper.domain.mappings.fingerprintmap

import io.github.sds100.keymapper.constraints.ConstraintState
import io.github.sds100.keymapper.domain.actions.ActionData
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

    private fun editFingerprintMap(block: (fingerprintMap: FingerprintMap) -> FingerprintMap) {
        mapping.value.ifIsData { setMapping(block.invoke(it)) }
    }
}

interface ConfigFingerprintMapUseCase : ConfigMappingUseCase<FingerprintMapAction, FingerprintMap>