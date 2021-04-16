package io.github.sds100.keymapper.ui.mappings.fingerprintmap

import io.github.sds100.keymapper.R
import io.github.sds100.keymapper.domain.mappings.fingerprintmap.FingerprintMap
import io.github.sds100.keymapper.domain.mappings.fingerprintmap.FingerprintMapAction
import io.github.sds100.keymapper.framework.adapters.ResourceProvider
import io.github.sds100.keymapper.mappings.DisplayActionUseCase
import io.github.sds100.keymapper.ui.actions.BaseActionUiHelper

/**
 * Created by sds100 on 04/03/2021.
 */

class FingerprintMapActionUiHelper(
    displayActionUseCase: DisplayActionUseCase,
    resourceProvider: ResourceProvider
) : BaseActionUiHelper<FingerprintMap, FingerprintMapAction>(
    displayActionUseCase,
    resourceProvider
) {

    override fun getOptionLabels(mapping: FingerprintMap, action: FingerprintMapAction): List<String> = sequence {

        if (mapping.isRepeatingActionUntilSwipedAgainAllowed() && action.repeatUntilSwipedAgain) {
            yield(getString(R.string.flag_repeat_until_swiped_again))
        }

         if (mapping.isHoldingDownActionUntilSwipedAgainAllowed(action) && action.holdDownUntilSwipedAgain) {
            yield(getString(R.string.flag_hold_down_until_swiped_again))
        }

    }.toList()
}