package io.github.sds100.keymapper.system.display

import kotlinx.coroutines.flow.Flow

/**
 * Created by sds100 on 17/04/2021.
 */
interface DisplayAdapter {
    val isScreenOn: Flow<Boolean>
    val orientation: Orientation
}