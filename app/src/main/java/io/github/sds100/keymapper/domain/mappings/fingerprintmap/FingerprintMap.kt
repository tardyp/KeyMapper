package io.github.sds100.keymapper.domain.mappings.fingerprintmap

import io.github.sds100.keymapper.data.model.FingerprintMapEntity
import io.github.sds100.keymapper.domain.actions.canBeHeldDown
import io.github.sds100.keymapper.domain.constraints.Constraint
import io.github.sds100.keymapper.domain.constraints.ConstraintMode
import io.github.sds100.keymapper.domain.models.Defaultable
import io.github.sds100.keymapper.domain.models.Option
import kotlinx.serialization.Serializable

/**
 * Created by sds100 on 03/03/2021.
 */

@Serializable
data class FingerprintMap(
    val id: FingerprintMapId,
    val actionDataList: List<FingerprintMapActionData> = emptyList(),
    val constraintList: Set<Constraint> = emptySet(),
    val constraintMode: ConstraintMode = ConstraintMode.AND,
    val isEnabled: Boolean = true,
    val vibrate: Boolean = false,
    val vibrateDuration: Defaultable<Int> = Defaultable.Default(),
    val showToast: Boolean = false
) {
    val options = FingerprintMapOptions(
        vibrate = Option(
            value = vibrate,
            isAllowed = true
        ),

        vibrateDuration = Option(
            value = vibrateDuration,
            isAllowed = vibrate
        ),

        showToast = Option(
            value = showToast,
            isAllowed = true
        )
    )

    val actionList: List<FingerprintMapAction> = actionDataList.map {
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
    }.toList()
}

object FingerprintMapEntityMapper {
    fun fromEntity(
        id: FingerprintMapId,
        entity: FingerprintMapEntity,
    ): FingerprintMap {
        val actionList = entity.actionList
            .map { FingerprintMapActionEntityMapper.fromEntity(it) }
        return FingerprintMap(
            id = id,
            actionDataList = actionList,
            TODO()
        )
    }

    fun toEntity(model: FingerprintMap): FingerprintMapEntity {
        TODO()
    }
}