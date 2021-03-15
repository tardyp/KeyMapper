package io.github.sds100.keymapper.util.result

import android.Manifest
import android.content.Context
import io.github.sds100.keymapper.Constants
import io.github.sds100.keymapper.R
import io.github.sds100.keymapper.framework.adapters.ResourceProvider
import io.github.sds100.keymapper.util.BuildUtils
import io.github.sds100.keymapper.util.str

/**
 * Created by sds100 on 29/02/2020.
 */

//TODO remove
fun Error.getFullMessage(ctx: Context) = when (this) {
    is PermissionDenied -> PermissionDenied.getMessageForPermission(ctx, permission)
    is AppNotFound -> ctx.str(R.string.error_app_isnt_installed, packageName)
    is AppDisabled -> ctx.str(R.string.error_app_is_disabled)
    is NoCompatibleImeEnabled -> ctx.str(R.string.error_ime_service_disabled)
    is NoCompatibleImeChosen -> ctx.str(R.string.error_ime_must_be_chosen)
    is OptionsNotRequired -> ctx.str(R.string.error_options_not_required)
    is SystemFeatureNotSupported -> ctx.str(R.string.error_feature_not_available, feature)
    is ConstraintNotFound -> ctx.str(R.string.error_constraint_not_found)
    is ExtraNotFound -> ctx.str(R.string.error_extra_not_found, extraId)
    is NoActionData -> ctx.str(R.string.error_no_action_data)
    is SdkVersionTooLow -> ctx.str(R.string.error_sdk_version_too_low, BuildUtils.getSdkVersionName(sdkVersion))
    is SdkVersionTooHigh -> ctx.str(R.string.error_sdk_version_too_high, BuildUtils.getSdkVersionName(sdkVersion))
    is FeatureUnavailable -> ctx.str(R.string.error_feature_not_available, feature)
    is SystemActionNotFound -> ctx.str(R.string.error_system_action_not_found, id)
    is KeyMapperImeNotFound -> ctx.str(R.string.error_key_mapper_ime_not_found)
    is InputMethodNotFound -> ctx.str(R.string.error_ime_not_found, id)
    is OptionLabelNotFound -> ctx.str(R.string.error_cant_find_option_label, id)
    is NoEnabledInputMethods -> ctx.str(R.string.error_no_enabled_imes)
    is GoogleAppNotFound -> ctx.str(R.string.error_google_app_not_installed)
    is FrontFlashNotFound -> ctx.str(R.string.error_front_flash_not_found)
    is BackFlashNotFound -> ctx.str(R.string.error_back_flash_not_found)
    is ImeNotFound -> ctx.str(R.string.error_ime_not_found, id)
    is DownloadFailed -> ctx.str(R.string.error_download_failed)
    is FileNotCached -> ctx.str(R.string.error_file_not_cached)
    is SSLHandshakeError -> ctx.str(R.string.error_ssl_handshake_exception)
    is DeviceNotFound -> ctx.str(R.string.error_device_not_found)
    is GenericError -> exception.toString()
    is EmptyJson -> ctx.str(R.string.error_empty_json)
    is FileAccessDenied -> ctx.str(R.string.error_file_access_denied)
    is FailedToSplitString -> ctx.str(R.string.error_failed_to_split_string, string)
    is InvalidNumber -> ctx.str(R.string.error_invalid_number)
    is NumberTooSmall -> ctx.str(R.string.error_number_too_small, min)
    is NumberTooBig -> ctx.str(R.string.error_number_too_big, max)
    is CantBeEmpty -> ctx.str(R.string.error_cant_be_empty)

    else -> throw Exception("Can't find error message for ${this::class.simpleName}")
}

fun Error.getFullMessage(resourceProvider: ResourceProvider) = when (this) {
    is PermissionDenied -> PermissionDenied.getMessageForPermission(resourceProvider, permission)
    is AppNotFound -> resourceProvider.getString(R.string.error_app_isnt_installed, packageName)
    is AppDisabled -> resourceProvider.getString(R.string.error_app_is_disabled)
    is NoCompatibleImeEnabled -> resourceProvider.getString(R.string.error_ime_service_disabled)
    is NoCompatibleImeChosen -> resourceProvider.getString(R.string.error_ime_must_be_chosen)
    is OptionsNotRequired -> resourceProvider.getString(R.string.error_options_not_required)
    is SystemFeatureNotSupported -> resourceProvider.getString(R.string.error_feature_not_available, feature)
    is ConstraintNotFound -> resourceProvider.getString(R.string.error_constraint_not_found)
    is ExtraNotFound -> resourceProvider.getString(R.string.error_extra_not_found, extraId)
    is NoActionData -> resourceProvider.getString(R.string.error_no_action_data)
    is SdkVersionTooLow -> resourceProvider.getString(R.string.error_sdk_version_too_low, BuildUtils.getSdkVersionName(sdkVersion))
    is SdkVersionTooHigh -> resourceProvider.getString(R.string.error_sdk_version_too_high, BuildUtils.getSdkVersionName(sdkVersion))
    is FeatureUnavailable -> resourceProvider.getString(R.string.error_feature_not_available, feature)
    is SystemActionNotFound -> resourceProvider.getString(R.string.error_system_action_not_found, id)
    is KeyMapperImeNotFound -> resourceProvider.getString(R.string.error_key_mapper_ime_not_found)
    is InputMethodNotFound -> resourceProvider.getString(R.string.error_ime_not_found, id)
    is OptionLabelNotFound -> resourceProvider.getString(R.string.error_cant_find_option_label, id)
    is NoEnabledInputMethods -> resourceProvider.getString(R.string.error_no_enabled_imes)
    is GoogleAppNotFound -> resourceProvider.getString(R.string.error_google_app_not_installed)
    is FrontFlashNotFound -> resourceProvider.getString(R.string.error_front_flash_not_found)
    is BackFlashNotFound -> resourceProvider.getString(R.string.error_back_flash_not_found)
    is ImeNotFound -> resourceProvider.getString(R.string.error_ime_not_found, id)
    is DownloadFailed -> resourceProvider.getString(R.string.error_download_failed)
    is FileNotCached -> resourceProvider.getString(R.string.error_file_not_cached)
    is SSLHandshakeError -> resourceProvider.getString(R.string.error_ssl_handshake_exception)
    is DeviceNotFound -> resourceProvider.getString(R.string.error_device_not_found)
    is GenericError -> exception.toString()
    is EmptyJson -> resourceProvider.getString(R.string.error_empty_json)
    is FileAccessDenied -> resourceProvider.getString(R.string.error_file_access_denied)
    is FailedToSplitString -> resourceProvider.getString(R.string.error_failed_to_split_string, string)
    is InvalidNumber -> resourceProvider.getString(R.string.error_invalid_number)
    is NumberTooSmall -> resourceProvider.getString(R.string.error_number_too_small, min)
    is NumberTooBig -> resourceProvider.getString(R.string.error_number_too_big, max)
    is CantBeEmpty -> resourceProvider.getString(R.string.error_cant_be_empty)

    else -> throw Exception("Can't find error message for ${this::class.simpleName}")
}

fun Error.getBriefMessage(ctx: Context) = when (this) {
    is AppNotFound -> ctx.str(R.string.error_app_isnt_installed_brief)

    else -> getFullMessage(ctx)
}

class PermissionDenied(val permission: String) : RecoverableError() {
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

        fun getMessageForPermission(resourceProvider: ResourceProvider, permission: String): String {
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

open class AppNotFound(val packageName: String) : RecoverableError()
data class AppDisabled(val packageName: String) : RecoverableError()
class NoCompatibleImeEnabled : RecoverableError()
class NoCompatibleImeChosen : RecoverableError()
class FileAccessDenied : Error()
data class GenericError(val exception: Exception) : Error()
object EmptyJson : Error()
class OptionsNotRequired : Error()
data class SystemFeatureNotSupported(val feature: String) : Error()
class ConstraintNotFound : Error()
data class ExtraNotFound(val extraId: String) : Error()
class NoActionData : Error()
data class SdkVersionTooLow(val sdkVersion: Int) : Error()
data class SdkVersionTooHigh(val sdkVersion: Int) : Error()
data class FeatureUnavailable(val feature: String) : Error()
data class SystemActionNotFound(val id: String) : Error()
class KeyMapperImeNotFound : Error()
data class InputMethodNotFound(val id: String) : Error()
data class OptionLabelNotFound(val id: String) : Error()
class NoEnabledInputMethods : Error()
class GoogleAppNotFound : AppNotFound("com.google.android.googlequicksearchbox")
class FrontFlashNotFound : Error()
class BackFlashNotFound : Error()
data class ImeNotFound(val id: String) : Error()
class DownloadFailed : Error()
class FileNotCached : Error()
class SSLHandshakeError : Error()
class DeviceNotFound : Error()
data class FailedToSplitString(val string: String) : Error()
class InvalidNumber : Error()
data class NumberTooBig(val max: Int) : Error()
data class NumberTooSmall(val min: Int) : Error()
class CantBeEmpty : Error()
class NoIncompatibleKeyboardsInstalled : Error()
class NoMediaSessions : Error()
data class UnknownFileLocation(val path: String) : Error()
object BackupVersionTooNew : Error()
object CorruptJsonFile : Error()
object CorruptActionError: Error()