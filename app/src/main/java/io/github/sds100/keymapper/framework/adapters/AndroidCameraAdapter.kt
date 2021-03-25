package io.github.sds100.keymapper.framework.adapters

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import androidx.core.content.getSystemService
import io.github.sds100.keymapper.domain.adapter.CameraAdapter
import io.github.sds100.keymapper.domain.utils.CameraLens

/**
 * Created by sds100 on 17/03/2021.
 */
class AndroidCameraAdapter(context: Context) : CameraAdapter {
    private val ctx = context.applicationContext

    override fun hasFlashFacing(lens: CameraLens): Boolean {
        ctx.getSystemService<CameraManager>()?.apply {
            return cameraIdList.toList().any { cameraId ->
                val camera = getCameraCharacteristics(cameraId)
                val hasFlash =
                    camera.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) ?: return false

                val lensToCompareSdkValue = when (lens) {
                    CameraLens.FRONT -> CameraCharacteristics.LENS_FACING_FRONT
                    CameraLens.BACK -> CameraCharacteristics.LENS_FACING_BACK
                }

                return hasFlash && camera.get(CameraCharacteristics.LENS_FACING) == lensToCompareSdkValue
            }
        }

        return false
    }
}