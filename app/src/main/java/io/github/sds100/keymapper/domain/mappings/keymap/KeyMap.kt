package io.github.sds100.keymapper.domain.mappings.keymap

import io.github.sds100.keymapper.data.model.KeyMapEntity
import io.github.sds100.keymapper.domain.adapter.ExternalDeviceAdapter
import io.github.sds100.keymapper.domain.models.Constraint
import io.github.sds100.keymapper.domain.models.ConstraintMode
import io.github.sds100.keymapper.domain.models.Option
import io.github.sds100.keymapper.ui.actions.ActionUtils
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
                    && ActionUtils.canBeHeldDown(it.data)
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
    fun fromEntity(entity: KeyMapEntity, deviceAdapter: ExternalDeviceAdapter): KeyMap {
        return KeyMap(
            dbId = entity.id,
            uid = entity.uid,
            trigger = KeymapTriggerEntityMapper.fromEntity(entity.trigger, deviceAdapter),
            //TODO finish
        )
    }

    fun toEntity(model: KeyMap): KeyMapEntity {
        TODO()
    }
}