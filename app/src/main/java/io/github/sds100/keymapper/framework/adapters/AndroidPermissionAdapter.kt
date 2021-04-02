package io.github.sds100.keymapper.framework.adapters

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import io.github.sds100.keymapper.Constants
import io.github.sds100.keymapper.domain.adapter.PermissionAdapter
import io.github.sds100.keymapper.domain.preferences.Keys
import io.github.sds100.keymapper.domain.repositories.PreferenceRepository
import io.github.sds100.keymapper.service.DeviceAdmin
import io.github.sds100.keymapper.util.RootUtils
import io.github.sds100.keymapper.util.firstBlocking
import splitties.systemservices.devicePolicyManager
import splitties.systemservices.notificationManager
import splitties.systemservices.powerManager

/**
 * Created by sds100 on 17/03/2021.
 */
class AndroidPermissionAdapter(
    context: Context,
    private val preferenceRepository: PreferenceRepository
) : PermissionAdapter {
    private val ctx = context.applicationContext

    override fun isGranted(permission: String): Boolean {
        val hasRootPermission =
            preferenceRepository.get(Keys.hasRootPermission).firstBlocking() ?: false

        when {
            permission == Manifest.permission.WRITE_SETTINGS &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ->
                return Settings.System.canWrite(ctx)

            permission == Constants.PERMISSION_ROOT ->
                return hasRootPermission

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
}