package io.github.sds100.keymapper.ui.mappings.fingerprintmap

import io.github.sds100.keymapper.R
import io.github.sds100.keymapper.domain.actions.GetActionErrorUseCase
import io.github.sds100.keymapper.domain.constraints.GetConstraintErrorUseCase
import io.github.sds100.keymapper.domain.mappings.fingerprintmap.FingerprintMap
import io.github.sds100.keymapper.domain.mappings.fingerprintmap.FingerprintMapAction
import io.github.sds100.keymapper.domain.mappings.fingerprintmap.FingerprintMapId
import io.github.sds100.keymapper.domain.mappings.fingerprintmap.FingerprintMapOptions
import io.github.sds100.keymapper.domain.models.ifIsAllowed
import io.github.sds100.keymapper.framework.adapters.ResourceProvider
import io.github.sds100.keymapper.ui.actions.ActionUiHelper
import io.github.sds100.keymapper.ui.constraints.ConstraintUiHelper
import io.github.sds100.keymapper.ui.mappings.common.BaseMappingListItemCreator

/**
 * Created by sds100 on 19/03/2021.
 */
class FingerprintMapListItemCreator(
    private val getActionError: GetActionErrorUseCase,
    actionUiHelper: ActionUiHelper<FingerprintMapAction>,
    constraintUiHelper: ConstraintUiHelper,
    getConstraintErrorUseCase: GetConstraintErrorUseCase,
    resourceProvider: ResourceProvider
) : BaseMappingListItemCreator<FingerprintMapAction>(
    getActionError,
    actionUiHelper,
    getConstraintErrorUseCase,
    constraintUiHelper,
    resourceProvider
) {

    fun map(fingerprintMap: FingerprintMap): FingerprintMapListItemModel {
        val header = when (fingerprintMap.id) {
            FingerprintMapId.SWIPE_DOWN -> getString(R.string.header_fingerprint_gesture_down)
            FingerprintMapId.SWIPE_UP -> getString(R.string.header_fingerprint_gesture_up)
            FingerprintMapId.SWIPE_LEFT -> getString(R.string.header_fingerprint_gesture_left)
            FingerprintMapId.SWIPE_RIGHT -> getString(R.string.header_fingerprint_gesture_right)
        }

        val midDot = getString(R.string.middot)

        val optionsDescription = buildString {
            getOptionLabels(fingerprintMap.options).forEachIndexed { index, label ->
                if (index != 0) {
                    append(" $midDot ")
                }

                append(label)
            }
        }

        val extraInfo = buildString {
            if (!fingerprintMap.isEnabled) {
                append(getString(R.string.disabled))
            }

            if (fingerprintMap.actionList.any { getActionError.getError(it.data) != null }) {
                if (this.isNotEmpty()) {
                    append(" $midDot ")
                }

                append(getString(R.string.tap_actions_to_fix))
            }

            if (fingerprintMap.actionList.isEmpty()) {
                if (this.isNotEmpty()) {
                    append(" $midDot ")
                }

                append(getString(R.string.no_actions))
            }
        }

        return FingerprintMapListItemModel(
            id = fingerprintMap.id,
            header = header,
            chipList = getChipList(
                fingerprintMap.actionList,
                fingerprintMap.constraintList,
                fingerprintMap.constraintMode
            ),
            optionsDescription = optionsDescription,
            isEnabled = fingerprintMap.isEnabled,
            extraInfo = extraInfo
        )
    }

    private fun getOptionLabels(options: FingerprintMapOptions): List<String> {
        val labels = mutableListOf<String>()

        options.vibrate.ifIsAllowed {
            if (it) {
                labels.add(getString(R.string.flag_vibrate))
            }
        }

        options.showToast.ifIsAllowed {
            if (it) {
                labels.add(getString(R.string.flag_show_toast))
            }
        }

        return labels
    }
}