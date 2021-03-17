package io.github.sds100.keymapper.domain.mappings.fingerprintmap

import io.github.sds100.keymapper.data.model.FingerprintMapEntity
import io.github.sds100.keymapper.domain.actions.canBeHeldDown
import io.github.sds100.keymapper.domain.models.Constraint
import io.github.sds100.keymapper.domain.models.ConstraintMode
import io.github.sds100.keymapper.domain.models.Option
import kotlinx.serialization.Serializable

/**
 * Created by sds100 on 03/03/2021.
 */

@Serializable
data class FingerprintMap(
    val id: FingerprintMapId,
    val actionDataList: List<FingerprintMapActionData> = emptyList(),
    val constraintList: List<Constraint> = emptyList(),
    val constraintMode: ConstraintMode = ConstraintMode.AND,
    val isEnabled: Boolean = true
) {
    val actionList = actionDataList.map {
        val options = FingerprintMapActionOptions(
            delayBeforeNextAction = Option(
                value = it.delayBeforeNextAction,
                isAllowed = actionDataList.isNotEmpty()
            ),

            multiplier = Option(
                value = it.multiplier,
                true
            ),

            repeatUntilSwipedAgain = Option(
                value = it.repeatUntilSwipedAgain,
                isAllowed = true
            ),

            repeatRate = Option(
                value = it.repeatRate,
                isAllowed = it.repeatUntilSwipedAgain
            ),

            holdDownUntilSwipedAgain = Option(
                value = it.holdDownUntilSwipedAgain,
                isAllowed = it.data.canBeHeldDown()
            ),

            holdDownDuration = Option(
                value = it.holdDownDuration,
                isAllowed = it.holdDownUntilSwipedAgain
            )
        )

        FingerprintMapAction(it.uid, it.data, options)
    }
}

object FingerprintMapEntityMapper {
    fun fromEntity(
        id: FingerprintMapId,
        entity: FingerprintMapEntity,
    ): FingerprintMap {
        return FingerprintMap(
            id = id,
            actionDataList = entity.actionList.map { FingerprintMapActionEntityMapper.fromEntity(it) },
            //TODO
        )
    }

    fun toEntity(model: FingerprintMap): FingerprintMapEntity {
        TODO()
    }
}