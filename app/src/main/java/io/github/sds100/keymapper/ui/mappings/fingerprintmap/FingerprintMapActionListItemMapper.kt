package io.github.sds100.keymapper.ui.mappings.fingerprintmap

import io.github.sds100.keymapper.R
import io.github.sds100.keymapper.domain.actions.GetActionErrorUseCase
import io.github.sds100.keymapper.domain.adapter.InputMethodAdapter
import io.github.sds100.keymapper.domain.mappings.fingerprintmap.FingerprintMapAction
import io.github.sds100.keymapper.domain.models.Defaultable
import io.github.sds100.keymapper.framework.adapters.AppInfoAdapter
import io.github.sds100.keymapper.framework.adapters.ResourceProvider
import io.github.sds100.keymapper.ui.actions.BaseActionListItemMapper

/**
 * Created by sds100 on 04/03/2021.
 */

class FingerprintMapActionListItemMapper(
    getActionError: GetActionErrorUseCase,
    appInfoAdapter: AppInfoAdapter,
    inputMethodAdapter: InputMethodAdapter,
    resourceProvider: ResourceProvider
) : BaseActionListItemMapper<FingerprintMapAction>(
    getActionError,
    appInfoAdapter,
    inputMethodAdapter,
    resourceProvider
) {

    override fun getOptionLabels(action: FingerprintMapAction) = sequence {
        action.options.delayBeforeNextAction.apply {
            if (isAllowed && value is Defaultable.Custom) {
                yield(getString(R.string.action_title_wait, value.data))
            }
        }

        if (action.options.repeatUntilSwipedAgain.isAllowed) {
            yield(getString(R.string.flag_repeat_until_swiped_again))
        }

        if (action.options.holdDownUntilSwipedAgain.isAllowed) {
            yield(getString(R.string.flag_hold_down_until_swiped_again))
        }
    }.toList()
}