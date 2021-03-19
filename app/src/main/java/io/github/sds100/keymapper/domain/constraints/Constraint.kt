package io.github.sds100.keymapper.domain.constraints

import io.github.sds100.keymapper.data.model.ConstraintEntity
import io.github.sds100.keymapper.data.model.Extra
import io.github.sds100.keymapper.domain.utils.Orientation
import kotlinx.serialization.Serializable

/**
 * Created by sds100 on 03/03/2021.
 */

@Serializable
sealed class Constraint {

    data class AppInForeground(val packageName: String) : Constraint()
    data class AppNotInForeground(val packageName: String) : Constraint()
    data class AppPlayingMedia(val packageName: String) : Constraint()

    data class BtDeviceConnected(val bluetoothAddress: String, val deviceName: String) :
        Constraint()

    data class BtDeviceDisconnected(val bluetoothAddress: String, val deviceName: String) :
        Constraint()

    object ScreenOn : Constraint()
    object ScreenOff : Constraint()

    object OrientationPortrait : Constraint()
    object OrientationLandscape : Constraint()
    data class OrientationCustom(val orientation: Orientation) : Constraint()
}

object ConstraintEntityMapper {
    fun toEntity(constraint: Constraint): ConstraintEntity = when (constraint) {
        is Constraint.AppInForeground -> ConstraintEntity(
            type = ConstraintEntity.APP_FOREGROUND,
            extras = listOf(Extra(ConstraintEntity.EXTRA_PACKAGE_NAME, constraint.packageName))
        )

        is Constraint.AppNotInForeground -> ConstraintEntity(
            type = ConstraintEntity.APP_NOT_FOREGROUND,
            extras = listOf(Extra(ConstraintEntity.EXTRA_PACKAGE_NAME, constraint.packageName))
        )

        is Constraint.AppPlayingMedia -> ConstraintEntity(
            type = ConstraintEntity.APP_PLAYING_MEDIA,
            extras = listOf(Extra(ConstraintEntity.EXTRA_PACKAGE_NAME, constraint.packageName))
        )

        is Constraint.BtDeviceConnected -> ConstraintEntity(
            type = ConstraintEntity.BT_DEVICE_CONNECTED,
            extras = listOf(
                Extra(ConstraintEntity.EXTRA_BT_ADDRESS, constraint.bluetoothAddress),
                Extra(ConstraintEntity.EXTRA_BT_NAME, constraint.deviceName),
            )
        )

        is Constraint.BtDeviceDisconnected -> ConstraintEntity(
            type = ConstraintEntity.BT_DEVICE_DISCONNECTED,
            extras = listOf(
                Extra(ConstraintEntity.EXTRA_BT_ADDRESS, constraint.bluetoothAddress),
                Extra(ConstraintEntity.EXTRA_BT_NAME, constraint.deviceName),
            )
        )

        is Constraint.OrientationCustom -> when (constraint.orientation) {
            Orientation.ORIENTATION_0 -> ConstraintEntity(ConstraintEntity.ORIENTATION_0)
            Orientation.ORIENTATION_90 -> ConstraintEntity(ConstraintEntity.ORIENTATION_90)
            Orientation.ORIENTATION_180 -> ConstraintEntity(ConstraintEntity.ORIENTATION_180)
            Orientation.ORIENTATION_270 -> ConstraintEntity(ConstraintEntity.ORIENTATION_270)
        }

        Constraint.OrientationLandscape -> ConstraintEntity(ConstraintEntity.ORIENTATION_LANDSCAPE)
        Constraint.OrientationPortrait -> ConstraintEntity(ConstraintEntity.ORIENTATION_PORTRAIT)
        Constraint.ScreenOff -> ConstraintEntity(ConstraintEntity.SCREEN_OFF)
        Constraint.ScreenOn -> ConstraintEntity(ConstraintEntity.SCREEN_ON)
    }
}