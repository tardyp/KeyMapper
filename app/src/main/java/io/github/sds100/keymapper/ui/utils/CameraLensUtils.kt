package io.github.sds100.keymapper.ui.utils

import io.github.sds100.keymapper.R
import io.github.sds100.keymapper.domain.utils.CameraLens

/**
 * Created by sds100 on 23/03/2021.
 */
object CameraLensUtils {
    fun getLabel(lens: CameraLens) = when (lens) {
        CameraLens.FRONT -> R.string.lens_front
        CameraLens.BACK -> R.string.lens_back
    }
}