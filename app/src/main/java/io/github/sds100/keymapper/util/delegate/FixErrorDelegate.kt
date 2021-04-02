package io.github.sds100.keymapper.util.delegate

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavController
import io.github.sds100.keymapper.Constants
import io.github.sds100.keymapper.R
import io.github.sds100.keymapper.util.*
import io.github.sds100.keymapper.util.result.FixableError
import splitties.alertdialog.appcompat.*
import splitties.alertdialog.material.materialAlertDialog
import splitties.toast.longToast

/**
 * Created by sds100 on 22/10/20.
 */

class FixErrorDelegate(
    keyPrefix: String,
    resultRegistry: ActivityResultRegistry,
    lifecycleOwner: LifecycleOwner,
    private val onSuccessfulRecover: () -> Unit
) {

    private val startActivityForResultLauncher =
        resultRegistry.register(
            "$keyPrefix.start_activity",
            lifecycleOwner,
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == Activity.RESULT_OK) {
                onSuccessfulRecover.invoke()
            }
        }

    private val requestPermissionLauncher =
        resultRegistry.register(
            "$keyPrefix.request_permission",
            lifecycleOwner,
            ActivityResultContracts.RequestPermission()
        ) {
            if (it) {
                onSuccessfulRecover.invoke()
            }
        }

    fun recover(ctx: Context, failure: FixableError, navController: NavController) {
        when (failure) {
            is FixableError.PermissionDenied -> {
                when (failure.permission) {
                    Manifest.permission.WRITE_SETTINGS ->
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            PermissionUtils.requestWriteSettings(ctx)
                        }

                    Manifest.permission.CAMERA ->
                        PermissionUtils.requestStandardPermission(
                            requestPermissionLauncher,
                            Manifest.permission.CAMERA
                        )

                    Manifest.permission.BIND_DEVICE_ADMIN ->
                        PermissionUtils.requestDeviceAdmin(ctx, startActivityForResultLauncher)

                    Manifest.permission.READ_PHONE_STATE ->
                        PermissionUtils.requestStandardPermission(
                            requestPermissionLauncher,
                            Manifest.permission.READ_PHONE_STATE
                        )

                    Manifest.permission.ACCESS_NOTIFICATION_POLICY ->
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            PermissionUtils.requestAccessNotificationPolicy(
                                startActivityForResultLauncher
                            )
                        }

                    Manifest.permission.WRITE_SECURE_SETTINGS ->
                        PermissionUtils.requestWriteSecureSettingsPermission(ctx, navController)

                    Constants.PERMISSION_ROOT -> PermissionUtils.requestRootPermission(
                        ctx,
                        navController
                    )

                    Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE ->
                        PermissionUtils.requestNotificationListenerAccess(
                            startActivityForResultLauncher
                        )

                    Manifest.permission.CALL_PHONE ->
                        PermissionUtils.requestStandardPermission(
                            requestPermissionLauncher,
                            Manifest.permission.CALL_PHONE
                        )

                    else -> throw Exception("Don't know how to ask for permission ${failure.permission}")
                }
            }

            is FixableError.AppNotFound -> PackageUtils.viewAppOnline(ctx, failure.packageName)

            is FixableError.AppDisabled -> {
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:${failure.packageName}")
                    flags = Intent.FLAG_ACTIVITY_NO_HISTORY

                    ctx.startActivity(this)
                }
            }

            is FixableError.NoCompatibleImeEnabled -> KeyboardUtils.enableCompatibleInputMethods(
                ctx
            )
            is FixableError.NoCompatibleImeChosen -> KeyboardUtils.chooseCompatibleInputMethod(
                ctx
            )

            is FixableError.AccessibilityServiceDisabled ->{
                AccessibilityUtils.enableService(ctx)
            }

            is FixableError.IsBatteryOptimised -> {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return

                ctx.materialAlertDialog {
                    messageResource = R.string.dialog_message_disable_battery_optimisation

                    positiveButton(R.string.pos_turn_off) {
                        try {
                            val intent =
                                Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                            ctx.startActivity(intent)
                        } catch (e: ActivityNotFoundException) {
                            longToast(R.string.error_battery_optimisation_activity_not_found)
                        }
                    }

                    cancelButton()

                    neutralButton(R.string.neutral_go_to_dont_kill_my_app) {
                        UrlUtils.openUrl(ctx, ctx.str(R.string.url_dont_kill_my_app))
                    }

                    show()
                }
            }
        }
    }
}