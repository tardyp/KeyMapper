package io.github.sds100.keymapper.domain.mappings.fingerprintmap

import io.github.sds100.keymapper.constraints.ConstraintState
import io.github.sds100.keymapper.domain.actions.ActionData
import io.github.sds100.keymapper.domain.utils.Defaultable
import io.github.sds100.keymapper.domain.utils.ifIsData
import io.github.sds100.keymapper.mappings.BaseConfigMappingUseCase
import io.github.sds100.keymapper.mappings.ConfigMappingUseCase

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

    override fun setVibrateEnabled(enabled: Boolean) = editFingerprintMap { it.copy(vibrate = enabled) }

    override fun setVibrationDuration(duration: Defaultable<Int>) =
        editFingerprintMap { it.copy(vibrateDuration = duration.nullIfDefault()) }

    override fun setShowToastEnabled(enabled: Boolean) {
        editFingerprintMap { it.copy(showToast = enabled) }
    }

    override fun setActionMultiplier(uid: String, multiplier: Int?) {
        setActionOption(uid){it.copy(multiplier = multiplier)}
    }

    override fun setDelayBeforeNextAction(uid: String, delay: Int?) {
        setActionOption(uid){it.copy(delayBeforeNextAction = delay)}
    }

    override fun setActionRepeatEnabled(uid: String, repeat: Boolean) =
        setActionOption(uid) { it.copy(repeatUntilSwipedAgain = repeat) }

    override fun setActionRepeatRate(uid: String, repeatRate: Int?) =
        setActionOption(uid) { it.copy(repeatRate = repeatRate) }

    override fun setActionHoldDownEnabled(uid: String, holdDown: Boolean) =
        setActionOption(uid) { it.copy(holdDownUntilSwipedAgain = holdDown) }

    override fun setActionHoldDownDuration(uid: String, holdDownDuration: Int?) =
        setActionOption(uid) { it.copy(holdDownDuration = holdDownDuration) }

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

interface ConfigFingerprintMapUseCase : ConfigMappingUseCase<FingerprintMapAction, FingerprintMap> {
    fun setVibrateEnabled(enabled: Boolean)
    fun setVibrationDuration(duration: Defaultable<Int>)
    fun setShowToastEnabled(enabled: Boolean)

    fun setActionRepeatEnabled(uid: String, repeat: Boolean)
    fun setActionRepeatRate(uid: String, repeatRate: Int?)
    fun setActionHoldDownEnabled(uid: String, holdDown: Boolean)
    fun setActionHoldDownDuration(uid: String, holdDownDuration: Int?)
}