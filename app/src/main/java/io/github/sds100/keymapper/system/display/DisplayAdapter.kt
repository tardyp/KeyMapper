package io.github.sds100.keymapper.system.display

import io.github.sds100.keymapper.util.Result
import kotlinx.coroutines.flow.Flow

/**
 * Created by sds100 on 17/04/2021.
 */
interface DisplayAdapter {
    val isScreenOn: Flow<Boolean>
    val orientation: Orientation

    fun enableAutoRotate(): Result<*>
    fun toggleAutoRotate(): Result<*>
    fun disableAutoRotate(): Result<*>
    fun setOrientation(orientation: Orientation): Result<*>

    fun increaseBrightness():Result<*>
    fun decreaseBrightness():Result<*>
    fun enableAutoBrightness():Result<*>
    fun disableAutoBrightness():Result<*>
    fun toggleAutoBrightness():Result<*>
}