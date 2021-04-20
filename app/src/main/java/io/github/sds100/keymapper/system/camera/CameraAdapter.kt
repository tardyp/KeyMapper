package io.github.sds100.keymapper.system.camera

/**
 * Created by sds100 on 17/03/2021.
 */
interface CameraAdapter {
    fun hasFlashFacing(lens: CameraLens): Boolean
}