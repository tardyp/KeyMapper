package io.github.sds100.keymapper.ui.mappings.keymap

import io.github.sds100.keymapper.R
import io.github.sds100.keymapper.domain.actions.GetActionErrorUseCase
import io.github.sds100.keymapper.domain.adapter.InputMethodAdapter
import io.github.sds100.keymapper.domain.mappings.keymap.KeymapAction
import io.github.sds100.keymapper.framework.adapters.AppInfoAdapter
import io.github.sds100.keymapper.framework.adapters.ResourceProvider
import io.github.sds100.keymapper.ui.actions.BaseActionListItemMapper

/**
 * Created by sds100 on 04/03/2021.
 */

class KeymapActionListItemMapper(
    getActionError: GetActionErrorUseCase,
    appInfoAdapter: AppInfoAdapter,
    inputMethodAdapter: InputMethodAdapter,
    resourceProvider: ResourceProvider
) : BaseActionListItemMapper<KeymapAction>(
    getActionError,
    appInfoAdapter,
    inputMethodAdapter,
    resourceProvider
) {

    override fun getOptionLabels(action: KeymapAction) = sequence {

        if (action.options.repeat.isAllowed) {
            yield(getString(R.string.flag_repeat_actions))
        }

        if (action.options.holdDown.isAllowed) {
            yield(getString(R.string.flag_hold_down))
        }

    }.toList()
}