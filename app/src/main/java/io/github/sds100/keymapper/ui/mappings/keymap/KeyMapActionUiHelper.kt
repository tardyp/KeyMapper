package io.github.sds100.keymapper.ui.mappings.keymap

import io.github.sds100.keymapper.R
import io.github.sds100.keymapper.domain.mappings.keymap.KeyMap
import io.github.sds100.keymapper.domain.mappings.keymap.KeyMapAction
import io.github.sds100.keymapper.framework.adapters.ResourceProvider
import io.github.sds100.keymapper.mappings.common.DisplayActionUseCase
import io.github.sds100.keymapper.ui.actions.BaseActionUiHelper

/**
 * Created by sds100 on 04/03/2021.
 */

class KeyMapActionUiHelper(
    displayActionUseCase: DisplayActionUseCase,
    resourceProvider: ResourceProvider
) : BaseActionUiHelper<KeyMap, KeyMapAction>(displayActionUseCase, resourceProvider) {

    override fun getOptionLabels(mapping: KeyMap, action: KeyMapAction) = sequence {
        if (mapping.isRepeatingActionsAllowed() && action.repeat){
            yield(getString(R.string.flag_repeat_actions))
        }

        if (mapping.isHoldingDownActionAllowed(action) && action.holdDown){
            yield(getString(R.string.flag_hold_down))
        }

    }.toList()
}