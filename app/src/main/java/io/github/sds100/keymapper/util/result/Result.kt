package io.github.sds100.keymapper.util.result

import android.Manifest
import android.content.Context
import io.github.sds100.keymapper.Constants
import io.github.sds100.keymapper.R
import io.github.sds100.keymapper.framework.adapters.ResourceProvider
import io.github.sds100.keymapper.util.str

/**
 * Created by sds100 on 26/02/2020.
 */

/**
 * Inspired from @antonyharfield great example!
 */

sealed class Result<out T>

data class Success<T>(val value: T) : Result<T>()

sealed class Error : Result<Nothing>() {
    object FileAccessDenied : Error()
    data class GenericError(val exception: Exception) : Error()
    object EmptyJson : Error()
    object OptionsNotRequired : Error()
    data class SystemFeatureNotSupported(val feature: String) : Error()
    object ConstraintNotFound : Error()
    data class ExtraNotFound(val extraId: String) : Error()
    object NoActionData : Error()
    data class SdkVersionTooLow(val sdkVersion: Int) : Error()
    data class SdkVersionTooHigh(val sdkVersion: Int) : Error()
    data class FeatureUnavailable(val feature: String) : Error()
    data class SystemActionNotFound(val id: String) : Error()
    object KeyMapperImeNotFound : Error()
    data class InputMethodNotFound(val id: String) : Error()
    data class OptionLabelNotFound(val id: String) : Error()
    object NoEnabledInputMethods : Error()
    object NoVoiceAssistant : Error()
    object FrontFlashNotFound : Error()
    object BackFlashNotFound : Error()
    data class ImeNotFound(val id: String) : Error()
    object DownloadFailed : Error()
    object FileNotCached : Error()
    object SSLHandshakeError : Error()
    data class DeviceNotFound(val descriptor: String) : Error()
    data class FailedToSplitString(val string: String) : Error()
    object InvalidNumber : Error()
    data class NumberTooBig(val max: Int) : Error()
    data class NumberTooSmall(val min: Int) : Error()
    object CantBeEmpty : Error()
    object NoIncompatibleKeyboardsInstalled : Error()
    object NoMediaSessions : Error()
    data class UnknownFileLocation(val path: String) : Error()
    object BackupVersionTooNew : Error()
    object CorruptJsonFile : Error()
    object CorruptActionError : Error()
    object Duplicate : Error()
    data class ImeNotFoundForPackage(val packageName: String) : Error()
}

sealed class RecoverableError : Error() {
    data class AppNotFound(val packageName: String) : RecoverableError()
    data class AppDisabled(val packageName: String) : RecoverableError()
    object NoCompatibleImeEnabled : RecoverableError()
    object NoCompatibleImeChosen : RecoverableError()
    object AccessibilityServiceDisabled : RecoverableError()

    data class PermissionDenied(val permission: String) : RecoverableError() {
        companion object {
            //TODO remove
            fun getMessageForPermission(ctx: Context, permission: String): String {
                val resId = when (permission) {
                    Manifest.permission.WRITE_SETTINGS -> R.string.error_action_requires_write_settings_permission
                    Manifest.permission.CAMERA -> R.string.error_action_requires_camera_permission
                    Manifest.permission.BIND_DEVICE_ADMIN -> R.string.error_need_to_enable_device_admin
                    Manifest.permission.READ_PHONE_STATE -> R.string.error_action_requires_read_phone_state_permission
                    Manifest.permission.ACCESS_NOTIFICATION_POLICY -> R.string.error_action_notification_policy_permission
                    Manifest.permission.WRITE_SECURE_SETTINGS -> R.string.error_need_write_secure_settings_permission
                    Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE -> R.string.error_denied_notification_listener_service_permission
                    Manifest.permission.CALL_PHONE -> R.string.error_denied_call_phone_permission
                    Constants.PERMISSION_ROOT -> R.string.error_requires_root

                    else -> throw Exception("Couldn't find permission description for $permission")
                }

                return ctx.str(resId)
            }

            fun getMessageForPermission(
                resourceProvider: ResourceProvider,
                permission: String
            ): String {
                val resId = when (permission) {
                    Manifest.permission.WRITE_SETTINGS -> R.string.error_action_requires_write_settings_permission
                    Manifest.permission.CAMERA -> R.string.error_action_requires_camera_permission
                    Manifest.permission.BIND_DEVICE_ADMIN -> R.string.error_need_to_enable_device_admin
                    Manifest.permission.READ_PHONE_STATE -> R.string.error_action_requires_read_phone_state_permission
                    Manifest.permission.ACCESS_NOTIFICATION_POLICY -> R.string.error_action_notification_policy_permission
                    Manifest.permission.WRITE_SECURE_SETTINGS -> R.string.error_need_write_secure_settings_permission
                    Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE -> R.string.error_denied_notification_listener_service_permission
                    Manifest.permission.CALL_PHONE -> R.string.error_denied_call_phone_permission
                    Constants.PERMISSION_ROOT -> R.string.error_requires_root

                    else -> throw Exception("Couldn't find permission description for $permission")
                }

                return resourceProvider.getString(resId)
            }
        }
    }
}

fun <T1, T2> combineOnSuccess(
    result1: Result<T1>,
    result2: Result<T2>,
    onSuccess: (value1: T1, value2: T2) -> Unit
) {
    if (result1 is Success && result2 is Success) {
        onSuccess.invoke(result1.value, result2.value)
    }
}

inline fun <T> Result<T>.onSuccess(f: (T) -> Unit): Result<T> {
    if (this is Success) {
        f(this.value)
    }

    return this
}

infix fun <T, U> Result<T>.onFailure(f: (error: Error) -> U): Result<T> {
    if (this is Error) {
        f(this)
    }

    return this
}

infix fun <T, U> Result<T>.then(f: (T) -> Result<U>) =
    when (this) {
        is Success -> f(this.value)
        is Error -> this
    }

suspend infix fun <T, U> Result<T>.suspendThen(f: suspend (T) -> Result<U>) =
    when (this) {
        is Success -> f(this.value)
        is Error -> this
    }

infix fun <T> Result<T>.otherwise(f: (error: Error) -> Result<T>) =
    when (this) {
        is Success -> this
        is Error -> f(this)
    }

fun <T> Result<T>.errorOrNull(): Error? {
    when (this) {
        is Error -> return this
    }

    return null
}

fun <T> Result<T>.valueOrNull(): T? {
    when (this) {
        is Success -> return this.value
    }

    return null
}

val <T> Result<T>.isError: Boolean
    get() = this is Error

val <T> Result<T>.isSuccess: Boolean
    get() = this is Success

fun <T, U> Result<T>.handle(onSuccess: (value: T) -> U, onError: (error: Error) -> U): U {
    return when (this) {
        is Success -> onSuccess(value)
        is Error -> onError(this)
    }
}

suspend fun <T, U> Result<T>.handleAsync(
    onSuccess: suspend (value: T) -> U,
    onFailure: suspend (error: Error) -> U
): U {
    return when (this) {
        is Success -> onSuccess(value)
        is Error -> onFailure(this)
    }
}


fun <T> T.success() = Success(this)