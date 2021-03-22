package io.github.sds100.keymapper.ui.mappings.fingerprintmap

import io.github.sds100.keymapper.R
import io.github.sds100.keymapper.domain.adapter.InputMethodAdapter
import io.github.sds100.keymapper.domain.mappings.fingerprintmap.FingerprintMapAction
import io.github.sds100.keymapper.framework.adapters.AppInfoAdapter
import io.github.sds100.keymapper.framework.adapters.ResourceProvider
import io.github.sds100.keymapper.ui.actions.BaseActionUiHelper

/**
 * Created by sds100 on 04/03/2021.
 */

class FingerprintMapActionUiHelper(
    appInfoAdapter: AppInfoAdapter,
    inputMethodAdapter: InputMethodAdapter,
    resourceProvider: ResourceProvider
) : BaseActionUiHelper<FingerprintMapAction>(
    appInfoAdapter,
    inputMethodAdapter,
    resourceProvider
) {

    override fun getOptionLabels(action: FingerprintMapAction) = sequence {
        action.options.delayBeforeNextAction.apply {
            if (isAllowed && value != null) {
                yield(getString(R.string.action_title_wait, value))
            }
        }

        action.options.repeatUntilSwipedAgain.apply {
            if (isAllowed && value) {
                yield(getString(R.string.flag_repeat_until_swiped_again))
            }
        }

        action.options.holdDownUntilSwipedAgain.apply {
            if (isAllowed && value) {
                yield(getString(R.string.flag_hold_down_until_swiped_again))
            }
        }

    }.toList()
}