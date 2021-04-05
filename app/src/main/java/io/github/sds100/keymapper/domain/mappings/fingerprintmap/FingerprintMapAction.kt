package io.github.sds100.keymapper.domain.mappings.fingerprintmap

import io.github.sds100.keymapper.data.model.ActionEntity
import io.github.sds100.keymapper.domain.actions.Action
import io.github.sds100.keymapper.domain.actions.ActionData
import io.github.sds100.keymapper.domain.actions.canBeHeldDown
import kotlinx.serialization.Serializable
import java.util.*

/**
 * Created by sds100 on 09/03/2021.
 */

@Serializable
data class FingerprintMapAction(
    override val uid: String = UUID.randomUUID().toString(),
    override val data: ActionData,
    override val delayBeforeNextAction: Int? = null,
    override val multiplier: Int? = null,
    val repeatUntilSwipedAgain: Boolean = false,
    val repeatRate: Int? = null,
    val holdDownUntilSwipedAgain: Boolean = false,
    val holdDownDuration: Int? = null) : Action {

    fun isRepeatingUntilSwipedAgainAllowed(): Boolean {
        return true
    }

    fun isChangingRepeatRateAllowed(): Boolean {
        return repeatUntilSwipedAgain
    }

    fun isHoldingDownUntilSwipedAgainAllowed(): Boolean {
        return data.canBeHeldDown()
    }

    fun isHoldingDownActionBeforeRepeatingAllowed(): Boolean {
        return repeatUntilSwipedAgain && holdDownUntilSwipedAgain
    }
}

object FingerprintMapActionEntityMapper {
    fun fromEntity(entity: ActionEntity): FingerprintMapAction {
        TODO()
    }

    fun toEntity(action: FingerprintMapAction): ActionEntity {
        TODO()
    }
}