package io.github.sds100.keymapper.domain.mappings.keymap

import io.github.sds100.keymapper.data.model.KeyMapEntity
import io.github.sds100.keymapper.domain.actions.canBeHeldDown
import io.github.sds100.keymapper.domain.constraints.Constraint
import io.github.sds100.keymapper.domain.constraints.ConstraintEntityMapper
import io.github.sds100.keymapper.domain.constraints.ConstraintMode
import io.github.sds100.keymapper.domain.constraints.ConstraintModeEntityMapper
import io.github.sds100.keymapper.domain.mappings.keymap.trigger.KeymapTrigger
import io.github.sds100.keymapper.domain.mappings.keymap.trigger.KeymapTriggerEntityMapper
import io.github.sds100.keymapper.domain.models.Option
import io.github.sds100.keymapper.domain.utils.Defaultable
import io.github.sds100.keymapper.util.delegate.KeymapDetectionDelegate
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
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
    val constraintList: Set<Constraint> = emptySet(),
    val constraintMode: ConstraintMode = ConstraintMode.AND,
    val isEnabled: Boolean = true
) {

    companion object {
        const val NEW_ID = -1L
    }

    @Transient
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
                value = Defaultable.create(it.multiplier),
                isAllowed = true
            ),

            holdDownDuration = Option(
                value = Defaultable.create(it.holdDownDuration),
                isAllowed = it.repeat && it.holdDown
            ),

            repeatRate = Option(
                value = Defaultable.create(it.repeatRate),
                isAllowed = it.repeat
            ),

            repeatDelay = Option(
                value = Defaultable.create(it.repeatDelay),
                isAllowed = it.repeat
            ),

            stopRepeating = Option(
                value = it.stopRepeating,
                isAllowed = it.repeat
            ),

            stopHoldDown = Option(
                value = Defaultable.create(it.stopHoldDown),
                isAllowed = it.holdDown && !it.repeat
            ),

            delayBeforeNextAction = Option(
                value = Defaultable.create(it.delayBeforeNextAction),
                isAllowed = actionDataList.isNotEmpty()
            )
        )

        KeymapAction(it.uid, it.data, options)
    }.toList()
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
            constraintList = entity.constraintList.map { ConstraintEntityMapper.fromEntity(it) }
                .toSet(),
            constraintMode = ConstraintModeEntityMapper.fromEntity(entity.constraintMode),
            isEnabled = entity.isEnabled
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
            constraintMode = ConstraintModeEntityMapper.toEntity(keymap.constraintMode),
            isEnabled = keymap.isEnabled,
            uid = keymap.uid
        )
    }
}