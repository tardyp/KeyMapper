package io.github.sds100.keymapper.util.delegate

import io.github.sds100.keymapper.data.model.FingerprintMapEntity
import io.github.sds100.keymapper.domain.usecases.PerformActionsUseCase
import io.github.sds100.keymapper.util.FingerprintMapUtils
import io.github.sds100.keymapper.util.IActionError
import io.github.sds100.keymapper.util.IConstraintDelegate
import kotlinx.coroutines.CoroutineScope
import splitties.bitflags.hasFlag

/**
 * Created by sds100 on 11/12/20.
 */
class FingerprintGestureMapController(
    coroutineScope: CoroutineScope,
    performActionsUseCase: PerformActionsUseCase,
    iConstraintDelegate: IConstraintDelegate,
    iActionError: IActionError
) : SimpleMappingController(
    coroutineScope,
    performActionsUseCase,
    iConstraintDelegate,
    iActionError
) {

    var fingerprintMaps: Map<String, FingerprintMapEntity> = emptyMap()
        set(value) {
            reset()

            field = value
        }

    fun onGesture(sdkGestureId: Int) {
        val keyMapperId = FingerprintMapUtils.SDK_ID_TO_KEY_MAPPER_ID[sdkGestureId] ?: return

        fingerprintMaps[keyMapperId]?.apply {
            onDetected(
                keyMapperId,
                actionList,
                constraintList,
                constraintMode,
                isEnabled,
                extras,
                flags.hasFlag(FingerprintMapEntity.FLAG_VIBRATE),
                flags.hasFlag(FingerprintMapEntity.FLAG_SHOW_TOAST)
            )
        }
    }
}