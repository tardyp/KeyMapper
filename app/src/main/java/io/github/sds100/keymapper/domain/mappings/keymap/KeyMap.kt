package io.github.sds100.keymapper.domain.mappings.keymap

import io.github.sds100.keymapper.constraints.ConstraintState
import io.github.sds100.keymapper.data.model.KeyMapEntity
import io.github.sds100.keymapper.domain.actions.canBeHeldDown
import io.github.sds100.keymapper.domain.constraints.ConstraintEntityMapper
import io.github.sds100.keymapper.domain.constraints.ConstraintModeEntityMapper
import io.github.sds100.keymapper.domain.mappings.fingerprintmap.FingerprintMapActionEntityMapper
import io.github.sds100.keymapper.domain.mappings.keymap.trigger.KeyMapTrigger
import io.github.sds100.keymapper.domain.mappings.keymap.trigger.KeymapTriggerEntityMapper
import io.github.sds100.keymapper.mappings.common.Mapping
import io.github.sds100.keymapper.util.delegate.KeymapDetectionDelegate
import kotlinx.serialization.Serializable
import java.util.*

/**
 * Created by sds100 on 03/03/2021.
 */

@Serializable
data class KeyMap(
    val dbId: Long? = null,
    val uid: String = UUID.randomUUID().toString(),
    val trigger: KeyMapTrigger = KeyMapTrigger(),
    override val actionList: List<KeyMapAction> = emptyList(),
    override val constraintState: ConstraintState = ConstraintState(),
    override val isEnabled: Boolean = true
) : Mapping<KeyMapAction> {

    fun isRepeatingActionsAllowed(): Boolean {
        return KeymapDetectionDelegate.performActionOnDown(trigger)
    }

    fun isChangingActionRepeatRateAllowed(action: KeyMapAction): Boolean{
        return action.repeat
    }

    fun isChangingActionRepeatDelayAllowed(action: KeyMapAction): Boolean{
        return action.repeat
    }

    fun isHoldingDownActionAllowed(action: KeyMapAction): Boolean {
        return KeymapDetectionDelegate.performActionOnDown(trigger) && action.data.canBeHeldDown()
    }

    fun isHoldingDownActionBeforeRepeatingAllowed(action: KeyMapAction): Boolean {
        return action.repeat && action.holdDown
    }

    fun isStopRepeatingActionWhenTriggerPressedAgainAllowed(action: KeyMapAction): Boolean {
        return action.repeat
    }

    fun isStopHoldingDownActionWhenTriggerPressedAgainAllowed(action: KeyMapAction): Boolean {
        return action.holdDown && !action.repeat
    }
}

object KeyMapEntityMapper {
    fun fromEntity(entity: KeyMapEntity): KeyMap {
        val actionList = entity.actionList.mapNotNull { KeymapActionEntityMapper.fromEntity(it) }


        val constraintList =
            entity.constraintList.map { ConstraintEntityMapper.fromEntity(it) }.toSet()

        val constraintMode = ConstraintModeEntityMapper.fromEntity(entity.constraintMode)

        return KeyMap(
            dbId = entity.id,
            uid = entity.uid,
            trigger = KeymapTriggerEntityMapper.fromEntity(entity.trigger),
            actionList = actionList,
            constraintState = ConstraintState(constraintList, constraintMode),
            isEnabled = entity.isEnabled
        )
    }

    fun toEntity(keyMap: KeyMap, dbId: Long): KeyMapEntity {

        val actionEntityList = KeymapActionEntityMapper.toEntity(keyMap)

        return KeyMapEntity(
            id = dbId,
            trigger = KeymapTriggerEntityMapper.toEntity(keyMap.trigger),
            actionList = actionEntityList,
            constraintList = keyMap.constraintState.constraints.map {
                ConstraintEntityMapper.toEntity(
                    it
                )
            },
            constraintMode = ConstraintModeEntityMapper.toEntity(keyMap.constraintState.mode),
            isEnabled = keyMap.isEnabled,
            uid = keyMap.uid
        )
    }
}