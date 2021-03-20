package io.github.sds100.keymapper.domain.mappings.keymap

import io.github.sds100.keymapper.data.model.ConstraintEntity
import io.github.sds100.keymapper.data.model.KeyMapEntity
import io.github.sds100.keymapper.domain.actions.canBeHeldDown
import io.github.sds100.keymapper.domain.constraints.Constraint
import io.github.sds100.keymapper.domain.constraints.ConstraintEntityMapper
import io.github.sds100.keymapper.domain.constraints.ConstraintMode
import io.github.sds100.keymapper.domain.mappings.keymap.trigger.KeymapTrigger
import io.github.sds100.keymapper.domain.mappings.keymap.trigger.KeymapTriggerEntityMapper
import io.github.sds100.keymapper.domain.models.Option
import io.github.sds100.keymapper.util.delegate.KeymapDetectionDelegate
import kotlinx.serialization.Serializable
import java.util.*

/**
 * Created by sds100 on 03/03/2021.
 */

@Serializable
data class KeyMap(
    val dbId: Long = NEW_ID,
    val uid: String = UUID.randomUUID().toString(),
    val trigger: KeymapTrigger = KeymapTrigger(),
    val actionDataList: List<KeymapActionData> = emptyList(),
    val constraintList: List<Constraint> = emptyList(),
    val constraintMode: ConstraintMode = ConstraintMode.AND,
    val isEnabled: Boolean = true
) {
    companion object {
        const val NEW_ID = -1L
    }

    val actionList: List<KeymapAction> = actionDataList.map {
        val options = KeymapActionOptions(
            repeat = Option(
                value = it.repeat,
                isAllowed = KeymapDetectionDelegate.performActionOnDown(trigger)
            ),

            holdDown = Option(
                value = it.repeat,
                isAllowed =
                KeymapDetectionDelegate.performActionOnDown(trigger)
                    && it.data.canBeHeldDown()
            ),

            multiplier = Option(
                value = it.multiplier,
                isAllowed = true
            ),

            holdDownDuration = Option(
                value = it.holdDownDuration,
                isAllowed = it.repeat && it.holdDown
            ),

            repeatRate = Option(
                value = it.repeatRate,
                isAllowed = it.repeat
            ),

            repeatDelay = Option(
                value = it.repeatDelay,
                isAllowed = it.repeat
            ),

            stopRepeating = Option(
                value = it.stopRepeating,
                isAllowed = it.repeat
            ),

            stopHoldDown = Option(
                value = it.stopHoldDown,
                isAllowed = it.holdDown && !it.repeat
            ),

            delayBeforeNextAction = Option(
                value = it.delayBeforeNextAction,
                isAllowed = actionDataList.isNotEmpty()
            )
        )

        KeymapAction(it.uid, it.data, options)
    }
}

object KeyMapEntityMapper {
    fun fromEntity(entity: KeyMapEntity): KeyMap {
        val actionList = sequence {
            entity.actionList.forEach { entity ->
                KeymapActionDataEntityMapper.fromEntity(entity)?.let { yield(it) }
            }
        }.toList()

        return KeyMap(
            dbId = entity.id,
            uid = entity.uid,
            trigger = KeymapTriggerEntityMapper.fromEntity(entity.trigger),
            actionDataList = actionList,
            //TODO
        )
    }

    fun toEntity(keymap: KeyMap): KeyMapEntity {

        val actionEntityList = sequence {
            keymap.actionList.forEach { action ->
                KeymapActionDataEntityMapper.toEntity(action)?.let { yield(it) }
            }
        }.toList()

        return KeyMapEntity(
            id = keymap.dbId,
            trigger = KeymapTriggerEntityMapper.toEntity(keymap.trigger),
            actionList = actionEntityList,
            constraintList = keymap.constraintList.map { ConstraintEntityMapper.toEntity(it) },
            constraintMode = when (keymap.constraintMode) {
                ConstraintMode.AND -> ConstraintEntity.MODE_AND
                ConstraintMode.OR -> ConstraintEntity.MODE_OR
            },
            isEnabled = keymap.isEnabled,
            uid = keymap.uid
        )
    }
}