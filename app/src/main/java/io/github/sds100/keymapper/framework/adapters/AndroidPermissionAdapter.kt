package io.github.sds100.keymapper.framework.adapters

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import io.github.sds100.keymapper.Constants
import io.github.sds100.keymapper.domain.adapter.PermissionAdapter
import io.github.sds100.keymapper.domain.preferences.Keys
import io.github.sds100.keymapper.domain.repositories.PreferenceRepository
import io.github.sds100.keymapper.permissions.Permission
import io.github.sds100.keymapper.service.DeviceAdmin
import io.github.sds100.keymapper.util.RootUtils
import io.github.sds100.keymapper.util.firstBlocking
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import splitties.systemservices.devicePolicyManager
import splitties.systemservices.notificationManager

/**
 * Created by sds100 on 17/03/2021.
 */
class AndroidPermissionAdapter(
    context: Context,
    private val coroutineScope: CoroutineScope,
    private val preferenceRepository: PreferenceRepository
) : PermissionAdapter {
    private val ctx = context.applicationContext

    override val onPermissionsUpdate = MutableSharedFlow<Unit>()

    private val _request = MutableSharedFlow<Permission>()
    val request = _request.asSharedFlow()

    init {
        coroutineScope.launch {
            preferenceRepository.get(Keys.hasRootPermission)
                .drop(1) //drop the first value when collecting initially
                .collectLatest {
                    onPermissionsUpdate.emit(Unit)
                }
        }
    }

    override fun request(permission: Permission) {
        runBlocking { _request.emit(permission) }
    }

    override fun isGranted(permission: Permission): Boolean {

        return when (permission) {
            Permission.WRITE_SETTINGS ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Settings.System.canWrite(ctx)
                } else {
                    true
                }

            Permission.CAMERA ->
                ContextCompat.checkSelfPermission(
                    ctx,
                    Manifest.permission.CAMERA
                ) == PERMISSION_GRANTED

            Permission.DEVICE_ADMIN -> devicePolicyManager?.isAdminActive(
                ComponentName(
                    ctx,
                    DeviceAdmin::class.java
                )
            ) == true

            Permission.READ_PHONE_STATE -> ContextCompat.checkSelfPermission(
                ctx,
                Manifest.permission.READ_PHONE_STATE
            ) == PERMISSION_GRANTED

            Permission.ACCESS_NOTIFICATION_POLICY ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    notificationManager.isNotificationPolicyAccessGranted
                } else {
                    true
                }

            Permission.WRITE_SECURE_SETTINGS -> {
                if (isGranted(Permission.ROOT)) {
                    RootUtils.executeRootCommand("pm grant ${Constants.PACKAGE_NAME} ${Manifest.permission.WRITE_SECURE_SETTINGS}")
                }

                ContextCompat.checkSelfPermission(
                    ctx,
                    Manifest.permission.WRITE_SECURE_SETTINGS
                ) == PERMISSION_GRANTED
            }

            Permission.NOTIFICATION_LISTENER ->
                NotificationManagerCompat.getEnabledListenerPackages(ctx)
                    .contains(Constants.PACKAGE_NAME)

            Permission.CALL_PHONE ->
                ContextCompat.checkSelfPermission(
                    ctx,
                    Manifest.permission.CALL_PHONE
                ) == PERMISSION_GRANTED

            Permission.ROOT ->
                preferenceRepository.get(Keys.hasRootPermission).firstBlocking()
                    ?: false

            Permission.IGNORE_BATTERY_OPTIMISATION ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    ctx.getSystemService<PowerManager>()
                        ?.isIgnoringBatteryOptimizations(Constants.PACKAGE_NAME) ?: false
                } else {
                    true
                }
        }
    }

    fun onPermissionsChanged() {
        coroutineScope.launch {
            onPermissionsUpdate.emit(Unit)
        }
    }
}