package io.github.sds100.keymapper.ui.constraints

import io.github.sds100.keymapper.R
import io.github.sds100.keymapper.domain.constraints.Constraint
import io.github.sds100.keymapper.domain.utils.Orientation
import io.github.sds100.keymapper.framework.adapters.AppInfoAdapter
import io.github.sds100.keymapper.framework.adapters.ResourceProvider
import io.github.sds100.keymapper.ui.IconInfo
import io.github.sds100.keymapper.util.TintType
import io.github.sds100.keymapper.util.firstBlocking
import io.github.sds100.keymapper.util.result.Result
import io.github.sds100.keymapper.util.result.success
import kotlinx.coroutines.flow.map

/**
 * Created by sds100 on 18/03/2021.
 */

class ConstraintUiHelperImpl(
    private val appInfoAdapter: AppInfoAdapter,
    resourceProvider: ResourceProvider
) : ConstraintUiHelper, ResourceProvider by resourceProvider {

    override fun getTitle(constraint: Constraint): Result<String> = when (constraint) {
        is Constraint.AppInForeground -> getAppNameTitle(constraint.packageName).success()
        is Constraint.AppNotInForeground -> getAppNameTitle(constraint.packageName).success()
        is Constraint.AppPlayingMedia -> getAppNameTitle(constraint.packageName).success()

        is Constraint.BtDeviceConnected ->
            getString(
                R.string.constraint_bt_device_connected_description,
                constraint.deviceName
            ).success()

        is Constraint.BtDeviceDisconnected ->
            getString(
                R.string.constraint_bt_device_disconnected_description,
                constraint.deviceName
            ).success()

        is Constraint.OrientationCustom -> {
            val resId = when (constraint.orientation) {
                Orientation.ORIENTATION_0 -> R.string.constraint_choose_orientation_0
                Orientation.ORIENTATION_90 -> R.string.constraint_choose_orientation_90
                Orientation.ORIENTATION_180 -> R.string.constraint_choose_orientation_180
                Orientation.ORIENTATION_270 -> R.string.constraint_choose_orientation_270
            }

            getString(resId).success()
        }

        Constraint.OrientationLandscape ->
            getString(R.string.constraint_choose_orientation_landscape).success()

        Constraint.OrientationPortrait ->
            getString(R.string.constraint_choose_orientation_landscape).success()

        Constraint.ScreenOff ->
            getString(R.string.constraint_screen_off_description).success()

        Constraint.ScreenOn ->
            getString(R.string.constraint_screen_on_description).success()
    }

    override fun getIcon(constraint: Constraint): Result<IconInfo> = when (constraint) {
        is Constraint.AppInForeground -> TODO()
        is Constraint.AppNotInForeground -> TODO()
        is Constraint.AppPlayingMedia -> TODO()
        is Constraint.BtDeviceConnected -> IconInfo(
            drawable = getDrawable(R.drawable.ic_outline_bluetooth_connected_24),
            tintType = TintType.ON_SURFACE
        ).success()

        is Constraint.BtDeviceDisconnected -> IconInfo(
            drawable = getDrawable(R.drawable.ic_outline_bluetooth_disabled_24),
            tintType = TintType.ON_SURFACE
        ).success()

        is Constraint.OrientationCustom -> {
            val resId = when (constraint.orientation) {
                Orientation.ORIENTATION_0 -> R.drawable.ic_outline_stay_current_portrait_24
                Orientation.ORIENTATION_90 -> R.drawable.ic_outline_stay_current_landscape_24
                Orientation.ORIENTATION_180 -> R.drawable.ic_outline_stay_current_portrait_24
                Orientation.ORIENTATION_270 -> R.drawable.ic_outline_stay_current_landscape_24
            }

            IconInfo(
                drawable = getDrawable(resId),
                tintType = TintType.ON_SURFACE
            ).success()
        }

        Constraint.OrientationLandscape -> IconInfo(
            drawable = getDrawable(R.drawable.ic_outline_stay_current_landscape_24),
            tintType = TintType.ON_SURFACE
        ).success()

        Constraint.OrientationPortrait -> IconInfo(
            drawable = getDrawable(R.drawable.ic_outline_stay_current_portrait_24),
            tintType = TintType.ON_SURFACE
        ).success()

        Constraint.ScreenOff -> IconInfo(
            drawable = getDrawable(R.drawable.ic_outline_stay_current_portrait_24),
            tintType = TintType.ON_SURFACE
        ).success()

        Constraint.ScreenOn -> IconInfo(
            drawable = getDrawable(R.drawable.ic_baseline_mobile_off_24),
            tintType = TintType.ON_SURFACE
        ).success()
    }

    private fun getAppIcon(packageName: String): IconInfo {
        return appInfoAdapter.getAppIcon(packageName).map {
            IconInfo(it, TintType.NONE)
        }.firstBlocking()
    }

    private fun getAppNameTitle(packageName: String): String {
        return appInfoAdapter.getAppName(packageName).map { appName ->
            getString(R.string.description_open_app, appName)
        }.firstBlocking()
    }
}

interface ConstraintUiHelper {
    fun getTitle(constraint: Constraint): Result<String>
    fun getIcon(constraint: Constraint): Result<IconInfo>
}