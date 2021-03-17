package io.github.sds100.keymapper.framework.adapters

import android.content.Context
import io.github.sds100.keymapper.domain.adapter.CameraAdapter
import io.github.sds100.keymapper.domain.utils.CameraLens

/**
 * Created by sds100 on 17/03/2021.
 */
class AndroidCameraAdapter(context: Context) : CameraAdapter {
    private val ctx = context.applicationContext

    override fun hasFlashFacing(lens: CameraLens): Boolean {
        TODO("Not yet implemented")
    }
}