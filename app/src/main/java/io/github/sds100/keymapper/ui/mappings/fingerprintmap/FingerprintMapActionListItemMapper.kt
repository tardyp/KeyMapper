package io.github.sds100.keymapper.ui.mappings.fingerprintmap

import io.github.sds100.keymapper.domain.actions.GetActionErrorUseCase
import io.github.sds100.keymapper.domain.mappings.fingerprintmap.FingerprintMapAction
import io.github.sds100.keymapper.framework.adapters.AppInfoAdapter
import io.github.sds100.keymapper.framework.adapters.ResourceProvider
import io.github.sds100.keymapper.ui.actions.BaseActionListItemMapper

/**
 * Created by sds100 on 04/03/2021.
 */

class FingerprintMapActionListItemMapper(
    getActionError: GetActionErrorUseCase,
    appInfoAdapter: AppInfoAdapter,
    resourceProvider: ResourceProvider
) : BaseActionListItemMapper<FingerprintMapAction>(
    getActionError,
    appInfoAdapter,
    resourceProvider
) {

    override fun getOptionStrings(action: FingerprintMapAction): List<String> {
        TODO()
    }
}