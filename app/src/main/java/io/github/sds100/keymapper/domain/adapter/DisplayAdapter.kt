package io.github.sds100.keymapper.domain.adapter

import io.github.sds100.keymapper.domain.utils.Orientation
import kotlinx.coroutines.flow.Flow

/**
 * Created by sds100 on 17/04/2021.
 */
interface DisplayAdapter {
    val isScreenOn: Flow<Boolean>
    val orientation: Orientation
}