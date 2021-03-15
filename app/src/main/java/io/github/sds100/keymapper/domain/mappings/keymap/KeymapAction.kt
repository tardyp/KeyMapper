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
) : Action

@Serializable
data class KeymapActionData(
    val uid: String = UUID.randomUUID().toString(),
    val data: ActionData,
    private val repeat: Boolean = false,
    private val holdDown: Boolean = false,
    private val stopRepeating: StopRepeating = StopRepeating.TRIGGER_RELEASED,
    private val stopHoldDown: StopHoldDown = StopHoldDown.TRIGGER_RELEASED,
    private val repeatRate: Defaultable<Int> = Defaultable.Default(),
    private val repeatDelay: Defaultable<Int> = Defaultable.Default(),
    private val holdDownDuration: Defaultable<Int> = Defaultable.Default(),
    private val delayBeforeNextAction: Defaultable<Int> = Defaultable.Default(),
    private val multiplier: Defaultable<Int> = Defaultable.Default()
)

object KeymapActionEntityMapper {
    fun fromEntity(entity: ActionEntity): KeymapAction {
        TODO()
    }

    fun toEntity(action: KeymapAction): ActionEntity {
        TODO()
    }
}