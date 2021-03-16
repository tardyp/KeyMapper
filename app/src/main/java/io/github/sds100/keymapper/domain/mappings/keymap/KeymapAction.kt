package io.github.sds100.keymapper.domain.mappings.keymap

import io.github.sds100.keymapper.data.model.ActionEntity
import io.github.sds100.keymapper.domain.actions.Action
import io.github.sds100.keymapper.domain.actions.ActionData
import io.github.sds100.keymapper.domain.models.Defaultable
import kotlinx.serialization.Serializable
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

object KeymapActionEntityMapper {
    fun fromEntity(entity: ActionEntity): KeymapAction {
        TODO()
    }

    fun toEntity(action: KeymapAction): ActionEntity {
        TODO()
    }
}