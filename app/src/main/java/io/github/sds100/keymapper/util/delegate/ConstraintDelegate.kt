package io.github.sds100.keymapper.util.delegate

import android.view.Surface
import io.github.sds100.keymapper.data.model.ConstraintEntity
import io.github.sds100.keymapper.data.model.getData
import io.github.sds100.keymapper.util.IConstraintDelegate
import io.github.sds100.keymapper.util.IConstraintState
import io.github.sds100.keymapper.util.ScreenRotationUtils
import io.github.sds100.keymapper.util.result.valueOrNull

/**
 * Created by sds100 on 13/12/20.
 */
class ConstraintDelegate(
    constraintState: IConstraintState
) : IConstraintState by constraintState, IConstraintDelegate {

    override fun Array<ConstraintEntity>.constraintsSatisfied(mode: Int): Boolean {
        if (this.isEmpty()) return true

        return if (mode == ConstraintEntity.MODE_AND) {
            all { constraintSatisfied(it) }
        } else {
            any { constraintSatisfied(it) }
        }
    }

    private fun constraintSatisfied(constraint: ConstraintEntity): Boolean {
        val data = when (constraint.type) {
            ConstraintEntity.APP_FOREGROUND, ConstraintEntity.APP_NOT_FOREGROUND, ConstraintEntity.APP_PLAYING_MEDIA ->
                constraint.extras.getData(ConstraintEntity.EXTRA_PACKAGE_NAME).valueOrNull()

            ConstraintEntity.BT_DEVICE_CONNECTED, ConstraintEntity.BT_DEVICE_DISCONNECTED ->
                constraint.extras.getData(ConstraintEntity.EXTRA_BT_ADDRESS).valueOrNull()

            ConstraintEntity.SCREEN_ON,
            ConstraintEntity.SCREEN_OFF,
            in ConstraintEntity.ORIENTATION_CONSTRAINTS -> ""

            else -> throw Exception(
                "Don't know how to get the relevant data from this Constraint! ${constraint.type}"
            )
        } ?: return false

        return constraintSatisfied(constraint.type, data)
    }

    private fun constraintSatisfied(id: String, data: String): Boolean {
        return when (id) {
            ConstraintEntity.APP_FOREGROUND -> data == currentPackageName
            ConstraintEntity.APP_NOT_FOREGROUND -> data != currentPackageName
            ConstraintEntity.APP_PLAYING_MEDIA -> data == highestPriorityPackagePlayingMedia

            ConstraintEntity.BT_DEVICE_CONNECTED -> isBluetoothDeviceConnected(data)
            ConstraintEntity.BT_DEVICE_DISCONNECTED -> !isBluetoothDeviceConnected(data)

            ConstraintEntity.SCREEN_ON -> isScreenOn
            ConstraintEntity.SCREEN_OFF -> !isScreenOn

            ConstraintEntity.ORIENTATION_PORTRAIT -> orientation?.let {
                ScreenRotationUtils.isPortrait(
                    it
                )
            }
                ?: false
            ConstraintEntity.ORIENTATION_LANDSCAPE -> orientation?.let {
                ScreenRotationUtils.isLandscape(
                    it
                )
            }
                ?: false
            ConstraintEntity.ORIENTATION_0 -> orientation?.let { it == Surface.ROTATION_0 }
                ?: false
            ConstraintEntity.ORIENTATION_90 -> orientation?.let { it == Surface.ROTATION_90 }
                ?: false
            ConstraintEntity.ORIENTATION_180 -> orientation?.let { it == Surface.ROTATION_180 }
                ?: false
            ConstraintEntity.ORIENTATION_270 -> orientation?.let { it == Surface.ROTATION_270 }
                ?: false

            else -> true
        }
    }
}