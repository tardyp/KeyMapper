package io.github.sds100.keymapper.domain.mappings.fingerprintmap

import io.github.sds100.keymapper.constraints.ConstraintState
import io.github.sds100.keymapper.data.model.Extra
import io.github.sds100.keymapper.data.model.FingerprintMapEntity
import io.github.sds100.keymapper.data.model.getData
import io.github.sds100.keymapper.domain.constraints.ConstraintEntityMapper
import io.github.sds100.keymapper.domain.constraints.ConstraintModeEntityMapper
import io.github.sds100.keymapper.mappings.common.Mapping
import io.github.sds100.keymapper.util.result.valueOrNull
import kotlinx.serialization.Serializable
import splitties.bitflags.hasFlag
import splitties.bitflags.withFlag

/**
 * Created by sds100 on 03/03/2021.
 */

@Serializable
data class FingerprintMap(
    override val actionList: List<FingerprintMapAction> = emptyList(),
    override val constraintState: ConstraintState = ConstraintState(),
    override val isEnabled: Boolean = true,
    val vibrate: Boolean = false,
    val vibrateDuration: Int? = null,
    val showToast: Boolean = false
) : Mapping<FingerprintMapAction> {

    fun isVibrateAllowed(): Boolean {
        return true
    }

    fun isChangingVibrationDurationAllowed(): Boolean {
        return vibrate
    }
}

object FingerprintMapIdEntityMapper {
    fun toEntity(id: FingerprintMapId): String {
        return when (id) {
            FingerprintMapId.SWIPE_DOWN -> FingerprintMapEntity.ID_SWIPE_DOWN
            FingerprintMapId.SWIPE_UP -> FingerprintMapEntity.ID_SWIPE_UP
            FingerprintMapId.SWIPE_LEFT -> FingerprintMapEntity.ID_SWIPE_LEFT
            FingerprintMapId.SWIPE_RIGHT -> FingerprintMapEntity.ID_SWIPE_RIGHT
        }
    }

    fun fromEntity(id: String): FingerprintMapId {
        return when (id) {
            FingerprintMapEntity.ID_SWIPE_DOWN -> FingerprintMapId.SWIPE_DOWN
            FingerprintMapEntity.ID_SWIPE_UP -> FingerprintMapId.SWIPE_UP
            FingerprintMapEntity.ID_SWIPE_LEFT -> FingerprintMapId.SWIPE_LEFT
            FingerprintMapEntity.ID_SWIPE_RIGHT -> FingerprintMapId.SWIPE_RIGHT

            else -> throw IllegalArgumentException("Don't know how to get fingerprint map with id $id")
        }
    }
}

object FingerprintMapEntityMapper {
    fun fromEntity(
        entity: FingerprintMapEntity,
    ): FingerprintMap {
        val actionList = entity.actionList.map { FingerprintMapActionEntityMapper.fromEntity(it) }

        val constraintList =
            entity.constraintList.map { ConstraintEntityMapper.fromEntity(it) }.toSet()

        val constraintMode = ConstraintModeEntityMapper.fromEntity(entity.constraintMode)

        return FingerprintMap(
            actionList = actionList,
            constraintState = ConstraintState(constraintList, constraintMode),
            isEnabled = entity.isEnabled,
            vibrate = entity.flags.hasFlag(FingerprintMapEntity.FLAG_VIBRATE),
            vibrateDuration = entity.extras.getData(FingerprintMapEntity.EXTRA_VIBRATION_DURATION)
                .valueOrNull()?.toIntOrNull(),
            showToast = entity.flags.hasFlag(FingerprintMapEntity.FLAG_SHOW_TOAST)
        )
    }

    fun toEntity(model: FingerprintMap): FingerprintMapEntity {
        val extras: List<Extra> = sequence {
            if (model.isChangingVibrationDurationAllowed() && model.vibrateDuration != null) {
                yield(Extra(FingerprintMapEntity.EXTRA_VIBRATION_DURATION, model.vibrateDuration.toString()))
            }
        }.toList()

        var flags = 0

        if (model.isVibrateAllowed() && model.vibrate) {
            flags = flags.withFlag(FingerprintMapEntity.FLAG_VIBRATE)
        }

        if (model.showToast) {
            flags = flags.withFlag(FingerprintMapEntity.FLAG_SHOW_TOAST)
        }

        return FingerprintMapEntity(
            version = FingerprintMapEntity.CURRENT_VERSION,
            actionList = model.actionList.map { FingerprintMapActionEntityMapper.toEntity(it) },
            constraintList = model.constraintState.constraints.map { ConstraintEntityMapper.toEntity(it) },
            constraintMode = ConstraintModeEntityMapper.toEntity(model.constraintState.mode),
            extras = extras,
            flags = flags,
            isEnabled = model.isEnabled
        )
    }
}