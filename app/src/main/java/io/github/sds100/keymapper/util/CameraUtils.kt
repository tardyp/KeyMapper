package io.github.sds100.keymapper.util

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraCharacteristics
import android.os.Build
import androidx.annotation.RequiresApi
import io.github.sds100.keymapper.Constants
import io.github.sds100.keymapper.data.model.Option
import io.github.sds100.keymapper.data.model.SamsungCameraMode
import splitties.systemservices.cameraManager

/**
 * Created by sds100 on 24/12/2019.
 */

object CameraUtils {
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun hasFlashFacing(face: Int): Boolean {
        cameraManager.apply {
            return cameraIdList.toList().any { cameraId ->
                val camera = getCameraCharacteristics(cameraId)
                val hasFlash = camera.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) ?: return false

                return hasFlash && camera.get(CameraCharacteristics.LENS_FACING) == face
            }
        }
    }

    /**
     * [mode] the Option Id of the camera mode
     */
    fun openSamsungCameraMode(ctx: Context, @SamsungCameraMode mode: String) = ctx.apply {

        var component = ComponentName(Constants.SAMSUNG_CAMERA_PACKAGE_NAME,
            "com.sec.android.app.camera.Camera")
        var activityName: String? = null

        //some modes require a Long Extra "profile" with a value of -1
        var requiresProfileExtra = false

        //only one mode has a different component. they all have a different activity name
        when (mode) {
            Option.SAMSUNG_PRO_MODE -> {
                activityName = "com.sec.android.app.camera.shootingmode.pro"
                requiresProfileExtra = true
            }

            Option.SAMSUNG_LIVE_BROADCAST -> {
                component = ComponentName("${Constants.SAMSUNG_CAMERA_PACKAGE_NAME}.plb",
                    "com.sec.android.app.camera.Camera")
                activityName = "com.sec.android.app.camera.plb.Camera"
            }

            Option.SAMSUNG_VIRTUAL_SHOT -> {
                activityName = "com.sec.android.app.camera.shootingmode.virtualshot"
            }

            Option.SAMSUNG_VIDEO_COLLAGE -> {
                activityName = "com.sec.android.app.camera.shootingmode.videocollage"
            }

            Option.SAMSUNG_SLOW_MOTION -> {
                activityName = "com.sec.android.app.camera.shootingmode.slowmotion"
            }

            Option.SAMSUNG_SELECTIVE_FOCUS -> {
                activityName = "com.sec.android.app.camera.shootingmode.selectivefocus"
            }

            Option.SAMSUNG_PANORAMA -> {
                activityName = "com.sec.android.app.camera.shootingmode.panorama"
            }

            Option.SAMSUNG_FAST_MOTION -> {
                activityName = "com.sec.android.app.camera.shootingmode.fastmotion"
            }

            Option.SAMSUNG_AUTO -> {
                activityName = "com.sec.android.app.camera.shootingmode.auto"
            }

            Option.SAMSUNG_CAMERA_MODE -> {
                //this mode has no activity name
                activityName = null
                requiresProfileExtra = true
            }
        }

        Intent().apply {
            action = Intent.ACTION_MAIN
            addCategory(Intent.CATEGORY_LAUNCHER)

            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED

            this.component = component

            if (activityName != null) {
                putExtra("activity_name", activityName)
            }

            if (requiresProfileExtra) {
                putExtra("profile", -1L)
            }

            startActivity(this)
        }
    }
}