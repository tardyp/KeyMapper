package io.github.sds100.keymapper.domain.actions

import io.github.sds100.keymapper.data.model.SystemActionDef

/**
 * Created by sds100 on 19/02/2021.
 */
class ListSystemActionsUseCaseImpl {

}

interface ListSystemActionsUseCase {
    val supportedSystemActions: List<SystemActionDef>
    val unsupportedSystemActions: List<SystemActionDef>
}