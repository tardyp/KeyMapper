package io.github.sds100.keymapper.ui.mappings.keymap

import io.github.sds100.keymapper.domain.actions.GetActionErrorUseCase
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
    resourceProvider: ResourceProvider
) : BaseActionListItemMapper<KeymapAction>(
    getActionError,
    appInfoAdapter,
    resourceProvider
) {

    override fun getOptionStrings(action: KeymapAction): List<String> {
        TODO()
    }
}