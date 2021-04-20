package io.github.sds100.keymapper.system.permissions

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import io.github.sds100.keymapper.Constants
import io.github.sds100.keymapper.ServiceLocator
import io.github.sds100.keymapper.data.Keys
import io.github.sds100.keymapper.system.DeviceAdmin
import io.github.sds100.keymapper.system.root.RootUtils
import io.github.sds100.keymapper.util.firstBlocking
import splitties.systemservices.devicePolicyManager
import splitties.systemservices.notificationManager

/**
 * Created by sds100 on 02/04/2020.
 */

object PermissionUtils {

    //TODO delete
    fun isPermissionGranted(ctx: Context, permission: String): Boolean {
        val hasRootPermission =
            ServiceLocator.preferenceRepository(ctx)
                .get(Keys.hasRootPermission)
                .firstBlocking() ?: false

        when {
            permission == Manifest.permission.WRITE_SETTINGS &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ->
                return Settings.System.canWrite(ctx)

            permission == Manifest.permission.BIND_DEVICE_ADMIN -> {
                return devicePolicyManager?.isAdminActive(
                    ComponentName(
                        ctx,
                        DeviceAdmin::class.java
                    )
                ) == true
            }

            permission == Manifest.permission.ACCESS_NOTIFICATION_POLICY ->
                return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    notificationManager.isNotificationPolicyAccessGranted
                } else {
                    true
                }

            permission == Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE -> {
                return NotificationManagerCompat.getEnabledListenerPackages(ctx)
                    .contains(Constants.PACKAGE_NAME)
            }

            permission == Manifest.permission.WRITE_SECURE_SETTINGS && hasRootPermission -> {
                RootUtils.executeRootCommand("pm grant ${Constants.PACKAGE_NAME} ${Manifest.permission.WRITE_SECURE_SETTINGS}")
            }
        }

        return ContextCompat.checkSelfPermission(ctx, permission) == PERMISSION_GRANTED
    }

    fun haveWriteSettingsPermission(ctx: Context) =
        isPermissionGranted(ctx, Manifest.permission.WRITE_SETTINGS)

    fun haveWriteSecureSettingsPermission(ctx: Context) =
        isPermissionGranted(ctx, Manifest.permission.WRITE_SECURE_SETTINGS)
}