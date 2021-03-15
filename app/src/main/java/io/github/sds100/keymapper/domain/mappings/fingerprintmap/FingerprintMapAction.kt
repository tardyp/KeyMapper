package io.github.sds100.keymapper.domain.mappings.fingerprintmap

import io.github.sds100.keymapper.data.model.ActionEntity
import io.github.sds100.keymapper.domain.actions.Action
import io.github.sds100.keymapper.domain.actions.ActionData
import io.github.sds100.keymapper.domain.models.Defaultable
import java.util.*

/**
 * Created by sds100 on 09/03/2021.
 */

@Serializable
data class FingerprintMapAction(
    override val uid: String = UUID.randomUUID().toString(),
    override val data: ActionData,
    val options: FingerprintMapActionOptions
) : Action

@Serializable
data class FingerprintMapActionData(
    override val uid: String = UUID.randomUUID().toString(),
    override val data: ActionData,
    val delayBeforeNextAction: Defaultable<Int> = Defaultable.Default(),
    val multiplier: Defaultable<Int> = Defaultable.Default()
) : Action

object FingerprintMapActionEntityMapper {
    fun fromEntity(entity: ActionEntity): FingerprintMapActionData {
        TODO()
    }

    fun toEntity(action: FingerprintMapAction): ActionEntity {
        TODO()
    }
}