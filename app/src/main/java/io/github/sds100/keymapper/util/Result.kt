package io.github.sds100.keymapper.util.result

import io.github.sds100.keymapper.R
import io.github.sds100.keymapper.util.ui.ResourceProvider
import io.github.sds100.keymapper.system.permissions.Permission

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
    data class SdkVersionTooLow(val minSdk: Int) : Error()
    data class SdkVersionTooHigh(val maxSdk: Int) : Error()
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
    data class CorruptJsonFile(val reason: String) : Error()
    object CorruptActionError : Error()
    object Duplicate : Error()
    data class ImeNotFoundForPackage(val packageName: String) : Error()
    object LauncherShortcutsNotSupported: Error()
}

//TODO move these to Error and create isFixable extension function for Error.
sealed class FixableError : Error() {
    data class AppNotFound(val packageName: String) : FixableError()
    data class AppDisabled(val packageName: String) : FixableError()
    object NoCompatibleImeEnabled : FixableError()
    object NoCompatibleImeChosen : FixableError()

    object AccessibilityServiceDisabled : FixableError()

    data class PermissionDenied(val permission: Permission) : FixableError() {
        companion object {

            fun getMessageForPermission(
                resourceProvider: ResourceProvider,
                permission: Permission
            ): String {
                val resId = when (permission) {
                    Permission.WRITE_SETTINGS -> R.string.error_action_requires_write_settings_permission
                    Permission.CAMERA -> R.string.error_action_requires_camera_permission
                    Permission.DEVICE_ADMIN -> R.string.error_need_to_enable_device_admin
                    Permission.READ_PHONE_STATE -> R.string.error_action_requires_read_phone_state_permission
                    Permission.ACCESS_NOTIFICATION_POLICY -> R.string.error_action_notification_policy_permission
                    Permission.WRITE_SECURE_SETTINGS -> R.string.error_need_write_secure_settings_permission
                    Permission.NOTIFICATION_LISTENER -> R.string.error_denied_notification_listener_service_permission
                    Permission.CALL_PHONE -> R.string.error_denied_call_phone_permission
                    Permission.ROOT -> R.string.error_requires_root
                    Permission.IGNORE_BATTERY_OPTIMISATION -> R.string.error_battery_optimisation_enabled
                }

                return resourceProvider.getString(resId)
            }
        }
    }
}

inline fun <T> Result<T>.onSuccess(f: (T) -> Unit): Result<T> {
    if (this is Success) {
        f(this.value)
    }

    return this
}

inline fun <T, U> Result<T>.onFailure(f: (error: Error) -> U): Result<T> {
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