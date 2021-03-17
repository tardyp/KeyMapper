package io.github.sds100.keymapper.domain.adapter

import io.github.sds100.keymapper.domain.utils.CameraLens

/**
 * Created by sds100 on 17/03/2021.
 */
interface CameraAdapter {
    fun hasFlashFacing(lens: CameraLens): Boolean
}