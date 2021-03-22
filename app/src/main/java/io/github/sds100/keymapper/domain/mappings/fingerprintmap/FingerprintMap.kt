package io.github.sds100.keymapper.domain.mappings.fingerprintmap

import io.github.sds100.keymapper.data.model.FingerprintMapEntity
import io.github.sds100.keymapper.domain.actions.canBeHeldDown
import io.github.sds100.keymapper.domain.constraints.Constraint
import io.github.sds100.keymapper.domain.constraints.ConstraintMode
import io.github.sds100.keymapper.domain.models.Option
import io.github.sds100.keymapper.domain.utils.defaultable.Defaultable
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

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
    private val vibrate: Boolean = false,
    private val vibrateDuration: Int? = null,
    private val showToast: Boolean = false
) {
    @Transient
    val options = FingerprintMapOptions(
        vibrate = Option(
            value = vibrate,
            isAllowed = true
        ),

        vibrateDuration = Option(
            value = Defaultable.create(vibrateDuration),
            isAllowed = vibrate
        ),

        showToast = Option(
            value = showToast,
            isAllowed = true
        )
    )

    @Transient
    val actionList: List<FingerprintMapAction> = actionDataList.map {
        val options = FingerprintMapActionOptions(
            delayBeforeNextAction = Option(
                value = Defaultable.create(it.delayBeforeNextAction),
                isAllowed = actionDataList.isNotEmpty()
            ),

            multiplier = Option(
                value = Defaultable.create(it.multiplier),
                true
            ),

            repeatUntilSwipedAgain = Option(
                value = it.repeatUntilSwipedAgain,
                isAllowed = true
            ),

            repeatRate = Option(
                value = Defaultable.create(it.repeatRate),
                isAllowed = it.repeatUntilSwipedAgain
            ),

            holdDownUntilSwipedAgain = Option(
                value = it.holdDownUntilSwipedAgain,
                isAllowed = it.data.canBeHeldDown()
            ),

            holdDownDuration = Option(
                value = Defaultable.create(it.holdDownDuration),
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