package io.github.sds100.keymapper.ui.mappings.fingerprintmap

import io.github.sds100.keymapper.R
import io.github.sds100.keymapper.domain.mappings.fingerprintmap.FingerprintMap
import io.github.sds100.keymapper.domain.mappings.fingerprintmap.FingerprintMapAction
import io.github.sds100.keymapper.domain.mappings.fingerprintmap.FingerprintMapId
import io.github.sds100.keymapper.framework.adapters.ResourceProvider
import io.github.sds100.keymapper.mappings.common.DisplaySimpleMappingUseCase
import io.github.sds100.keymapper.ui.mappings.common.BaseMappingListItemCreator

/**
 * Created by sds100 on 19/03/2021.
 */
class FingerprintMapListItemCreator(
    private val  display: DisplaySimpleMappingUseCase,
    resourceProvider: ResourceProvider
) : BaseMappingListItemCreator<FingerprintMap, FingerprintMapAction>(
    display,
    FingerprintMapActionUiHelper(display, resourceProvider),
    resourceProvider
) {

    fun create(id: FingerprintMapId, fingerprintMap: FingerprintMap): FingerprintMapListItem {
        val header = when (id) {
            FingerprintMapId.SWIPE_DOWN -> getString(R.string.header_fingerprint_gesture_down)
            FingerprintMapId.SWIPE_UP -> getString(R.string.header_fingerprint_gesture_up)
            FingerprintMapId.SWIPE_LEFT -> getString(R.string.header_fingerprint_gesture_left)
            FingerprintMapId.SWIPE_RIGHT -> getString(R.string.header_fingerprint_gesture_right)
        }

        val midDot = getString(R.string.middot)

        val optionsDescription = buildString {
            getOptionLabels(fingerprintMap).forEachIndexed { index, label ->
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

            if (fingerprintMap.actionList.any { display.getActionError(it.data) != null }) {
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

        return FingerprintMapListItem(
            id = id,
            header = header,
            chipList = getChipList(fingerprintMap),
            optionsDescription = optionsDescription,
            isEnabled = fingerprintMap.isEnabled,
            extraInfo = extraInfo
        )
    }

    private fun getOptionLabels(fingerprintMap: FingerprintMap): List<String> {
        val labels = mutableListOf<String>()

        if (fingerprintMap.isVibrateAllowed() && fingerprintMap.vibrate) {
            labels.add(getString(R.string.flag_vibrate))
        }

        if (fingerprintMap.showToast) {
            labels.add(getString(R.string.flag_show_toast))
        }

        return labels
    }
}