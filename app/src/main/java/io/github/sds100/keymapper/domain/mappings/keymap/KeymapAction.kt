package io.github.sds100.keymapper.domain.mappings.keymap

import io.github.sds100.keymapper.data.model.ActionEntity
import io.github.sds100.keymapper.data.model.Extra
import io.github.sds100.keymapper.data.model.getData
import io.github.sds100.keymapper.domain.actions.Action
import io.github.sds100.keymapper.domain.actions.ActionData
import io.github.sds100.keymapper.domain.actions.ActionDataEntityMapper
import io.github.sds100.keymapper.domain.models.Defaultable
import io.github.sds100.keymapper.domain.models.createDefaultable
import io.github.sds100.keymapper.domain.models.ifIsAllowed
import io.github.sds100.keymapper.util.result.success
import io.github.sds100.keymapper.util.result.then
import io.github.sds100.keymapper.util.result.valueOrNull
import kotlinx.serialization.Serializable
import splitties.bitflags.hasFlag
import splitties.bitflags.withFlag
import java.util.*

/**
 * Created by sds100 on 09/03/2021.
 */

@Serializable
data class KeymapAction(
    override val uid: String = UUID.randomUUID().toString(),
    override val data: ActionData,
    val options: KeymapActionOptions
) : Action {
    override val multiplier = options.multiplier
    override val delayBeforeNextAction = options.delayBeforeNextAction
}

@Serializable
data class KeymapActionData(
    val uid: String = UUID.randomUUID().toString(),
    val data: ActionData,
    val repeat: Boolean = false,
    val holdDown: Boolean = false,
    val stopRepeating: StopRepeating = StopRepeating.TRIGGER_RELEASED,
    val stopHoldDown: Defaultable<StopHoldDown> = Defaultable.Default(),
    val repeatRate: Defaultable<Int> = Defaultable.Default(),
    val repeatDelay: Defaultable<Int> = Defaultable.Default(),
    val holdDownDuration: Defaultable<Int> = Defaultable.Default(),
    val delayBeforeNextAction: Defaultable<Int> = Defaultable.Default(),
    val multiplier: Defaultable<Int> = Defaultable.Default()
)

object KeymapActionDataEntityMapper {
    fun fromEntity(entity: ActionEntity): KeymapActionData? {
        val data = ActionDataEntityMapper.fromEntity(entity) ?: return null

        val stopRepeating =
            entity.extras.getData(ActionEntity.EXTRA_CUSTOM_STOP_REPEAT_BEHAVIOUR).then {
                if (it == ActionEntity.STOP_REPEAT_BEHAVIOUR_TRIGGER_PRESSED_AGAIN.toString()) {
                    StopRepeating.TRIGGER_PRESSED_AGAIN
                } else {
                    StopRepeating.TRIGGER_RELEASED
                }.success()
            }.valueOrNull()

        val stopHoldDown =
            entity.extras.getData(ActionEntity.EXTRA_CUSTOM_HOLD_DOWN_BEHAVIOUR).then {
                if (it == ActionEntity.STOP_HOLD_DOWN_BEHAVIOR_TRIGGER_PRESSED_AGAIN.toString()) {
                    StopHoldDown.TRIGGER_PRESSED_AGAIN
                } else {
                    StopHoldDown.TRIGGER_RELEASED
                }.success()
            }.valueOrNull()

        val repeatRate =
            entity.extras.getData(ActionEntity.EXTRA_REPEAT_RATE).valueOrNull()?.toIntOrNull()

        val repeatDelay =
            entity.extras.getData(ActionEntity.EXTRA_REPEAT_DELAY).valueOrNull()?.toIntOrNull()

        val holdDownDuration =
            entity.extras
                .getData(ActionEntity.EXTRA_HOLD_DOWN_DURATION)
                .valueOrNull()
                ?.toIntOrNull()

        val delayBeforeNextAction =
            entity.extras
                .getData(ActionEntity.EXTRA_DELAY_BEFORE_NEXT_ACTION)
                .valueOrNull()
                ?.toIntOrNull()

        val multiplier =
            entity.extras
                .getData(ActionEntity.EXTRA_MULTIPLIER)
                .valueOrNull()
                ?.toIntOrNull()

        return KeymapActionData(
            uid = entity.uid,
            data = data,
            repeat = entity.flags.hasFlag(ActionEntity.ACTION_FLAG_REPEAT),
            holdDown = entity.flags.hasFlag(ActionEntity.ACTION_FLAG_HOLD_DOWN),
            stopRepeating = stopRepeating ?: StopRepeating.TRIGGER_RELEASED,
            stopHoldDown = stopHoldDown.createDefaultable(),
            repeatRate = repeatRate.createDefaultable(),
            repeatDelay = repeatDelay.createDefaultable(),
            holdDownDuration = holdDownDuration.createDefaultable(),
            delayBeforeNextAction = delayBeforeNextAction.createDefaultable(),
            multiplier = multiplier.createDefaultable()
        )
    }

    fun toEntity(action: KeymapAction): ActionEntity? {
        val base = ActionDataEntityMapper.toEntity(action.data) ?: return null

        val extras = mutableListOf<Extra>().apply {
            action.options.delayBeforeNextAction.ifIsAllowed {
                if (it is Defaultable.Custom) {
                    add(Extra(ActionEntity.EXTRA_DELAY_BEFORE_NEXT_ACTION, it.data.toString()))
                }
            }

            action.options.multiplier.ifIsAllowed {
                if (it is Defaultable.Custom) {
                    add(Extra(ActionEntity.EXTRA_MULTIPLIER, it.data.toString()))
                }
            }

            action.options.holdDownDuration.ifIsAllowed {
                if (it is Defaultable.Custom) {
                    add(Extra(ActionEntity.EXTRA_HOLD_DOWN_DURATION, it.data.toString()))
                }
            }

            action.options.repeatRate.ifIsAllowed {
                if (it is Defaultable.Custom) {
                    add(Extra(ActionEntity.EXTRA_REPEAT_RATE, it.data.toString()))
                }
            }

            action.options.repeatDelay.ifIsAllowed {
                if (it is Defaultable.Custom) {
                    add(Extra(ActionEntity.EXTRA_REPEAT_DELAY, it.data.toString()))
                }
            }

            action.options.stopRepeating.ifIsAllowed {
                if (it == StopRepeating.TRIGGER_PRESSED_AGAIN) {
                    add(
                        Extra(
                            ActionEntity.EXTRA_CUSTOM_STOP_REPEAT_BEHAVIOUR,
                            ActionEntity.STOP_REPEAT_BEHAVIOUR_TRIGGER_PRESSED_AGAIN.toString()
                        )
                    )
                }
            }

            action.options.stopHoldDown.ifIsAllowed {
                if (it is Defaultable.Custom && it.data == StopHoldDown.TRIGGER_PRESSED_AGAIN) {
                    add(
                        Extra(
                            ActionEntity.EXTRA_CUSTOM_HOLD_DOWN_BEHAVIOUR,
                            ActionEntity.STOP_HOLD_DOWN_BEHAVIOR_TRIGGER_PRESSED_AGAIN.toString()
                        )
                    )
                }
            }
        }

        var flags = 0

        action.options.repeat.ifIsAllowed {
            if (it) {
                flags = flags.withFlag(ActionEntity.ACTION_FLAG_REPEAT)
            }
        }

        action.options.holdDown.ifIsAllowed {
            if (it) {
                flags = flags.withFlag(ActionEntity.ACTION_FLAG_HOLD_DOWN)
            }
        }

        return ActionEntity(
            type = base.type,
            data = base.data,
            extras = base.extras.plus(extras),
            flags = base.flags.withFlag(flags),
            uid = action.uid
        )
    }
}