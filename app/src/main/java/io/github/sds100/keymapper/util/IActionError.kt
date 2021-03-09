package io.github.sds100.keymapper.util

import io.github.sds100.keymapper.data.model.ActionEntity
import io.github.sds100.keymapper.util.result.Result

/**
 * Created by sds100 on 11/06/2020.
 */
interface IActionError {
    fun canActionBePerformed(action: ActionEntity, hasRootPermission: Boolean): Result<ActionEntity>
}