package io.github.sds100.keymapper.ui.mappings.keymap

import io.github.sds100.keymapper.domain.models.KeymapActionOptions
import io.github.sds100.keymapper.domain.actions.GetActionErrorUseCase
import io.github.sds100.keymapper.domain.devices.ShowDeviceInfoUseCase
import io.github.sds100.keymapper.framework.adapters.AppInfoAdapter
import io.github.sds100.keymapper.framework.adapters.ResourceProvider
import io.github.sds100.keymapper.ui.actions.BaseActionListItemMapper

/**
 * Created by sds100 on 04/03/2021.
 */

class KeymapActionListItemMapper(
    getActionError: GetActionErrorUseCase,
    showDeviceInfoUseCase: ShowDeviceInfoUseCase,
    appInfoAdapter: AppInfoAdapter,
    resourceProvider: ResourceProvider
) :
    BaseActionListItemMapper<KeymapActionOptions>(
        getActionError,
        showDeviceInfoUseCase,
        appInfoAdapter,
        resourceProvider
    ) {

    override fun <O> getOptionStrings(options: O): List<String> {
        TODO("Not yet implemented")
    }
}