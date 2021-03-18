package io.github.sds100.keymapper.util.result

import android.content.Context
import io.github.sds100.keymapper.R
import io.github.sds100.keymapper.framework.adapters.ResourceProvider
import io.github.sds100.keymapper.util.BuildUtils
import io.github.sds100.keymapper.util.str

/**
 * Created by sds100 on 29/02/2020.
 */

//TODO rename file to Error

//TODO remove
fun Error.getFullMessage(ctx: Context) = when (this) {
    is RecoverableError.PermissionDenied -> RecoverableError.PermissionDenied.getMessageForPermission(
        ctx,
        permission
    )
    is RecoverableError.AppNotFound -> ctx.str(R.string.error_app_isnt_installed, packageName)
    is RecoverableError.AppDisabled -> ctx.str(R.string.error_app_is_disabled)
    is RecoverableError.NoCompatibleImeEnabled -> ctx.str(R.string.error_ime_service_disabled)
    is RecoverableError.NoCompatibleImeChosen -> ctx.str(R.string.error_ime_must_be_chosen)
    is Error.OptionsNotRequired -> ctx.str(R.string.error_options_not_required)
    is Error.SystemFeatureNotSupported -> ctx.str(R.string.error_feature_not_available, feature)
    is Error.ConstraintNotFound -> ctx.str(R.string.error_constraint_not_found)
    is Error.ExtraNotFound -> ctx.str(R.string.error_extra_not_found, extraId)
    is Error.NoActionData -> ctx.str(R.string.error_no_action_data)
    is Error.SdkVersionTooLow -> ctx.str(
        R.string.error_sdk_version_too_low,
        BuildUtils.getSdkVersionName(sdkVersion)
    )
    is Error.SdkVersionTooHigh -> ctx.str(
        R.string.error_sdk_version_too_high,
        BuildUtils.getSdkVersionName(sdkVersion)
    )
    is Error.FeatureUnavailable -> ctx.str(R.string.error_feature_not_available, feature)
    is Error.SystemActionNotFound -> ctx.str(R.string.error_system_action_not_found, id)
    is Error.KeyMapperImeNotFound -> ctx.str(R.string.error_key_mapper_ime_not_found)
    is Error.InputMethodNotFound -> ctx.str(R.string.error_ime_not_found, id)
    is Error.OptionLabelNotFound -> ctx.str(R.string.error_cant_find_option_label, id)
    is Error.NoEnabledInputMethods -> ctx.str(R.string.error_no_enabled_imes)
    is Error.FrontFlashNotFound -> ctx.str(R.string.error_front_flash_not_found)
    is Error.BackFlashNotFound -> ctx.str(R.string.error_back_flash_not_found)
    is Error.ImeNotFound -> ctx.str(R.string.error_ime_not_found, id)
    is Error.DownloadFailed -> ctx.str(R.string.error_download_failed)
    is Error.FileNotCached -> ctx.str(R.string.error_file_not_cached)
    is Error.SSLHandshakeError -> ctx.str(R.string.error_ssl_handshake_exception)
    is Error.DeviceNotFound -> ctx.str(R.string.error_device_not_found)
    is Error.GenericError -> exception.toString()
    is Error.EmptyJson -> ctx.str(R.string.error_empty_json)
    is Error.FileAccessDenied -> ctx.str(R.string.error_file_access_denied)
    is Error.FailedToSplitString -> ctx.str(R.string.error_failed_to_split_string, string)
    is Error.InvalidNumber -> ctx.str(R.string.error_invalid_number)
    is Error.NumberTooSmall -> ctx.str(R.string.error_number_too_small, min)
    is Error.NumberTooBig -> ctx.str(R.string.error_number_too_big, max)
    is Error.CantBeEmpty -> ctx.str(R.string.error_cant_be_empty)

    else -> throw Exception("Can't find error message for ${this::class.simpleName}")
}

fun Error.getFullMessage(resourceProvider: ResourceProvider) = when (this) {
    is RecoverableError.PermissionDenied -> RecoverableError.PermissionDenied.getMessageForPermission(
        resourceProvider,
        permission
    )
    is RecoverableError.AppNotFound -> resourceProvider.getString(
        R.string.error_app_isnt_installed,
        packageName
    )
    is RecoverableError.AppDisabled -> resourceProvider.getString(R.string.error_app_is_disabled)
    is RecoverableError.NoCompatibleImeEnabled -> resourceProvider.getString(R.string.error_ime_service_disabled)
    is RecoverableError.NoCompatibleImeChosen -> resourceProvider.getString(R.string.error_ime_must_be_chosen)
    is Error.OptionsNotRequired -> resourceProvider.getString(R.string.error_options_not_required)
    is Error.SystemFeatureNotSupported -> resourceProvider.getString(
        R.string.error_feature_not_available,
        feature
    )
    is Error.ConstraintNotFound -> resourceProvider.getString(R.string.error_constraint_not_found)
    is Error.ExtraNotFound -> resourceProvider.getString(R.string.error_extra_not_found, extraId)
    is Error.NoActionData -> resourceProvider.getString(R.string.error_no_action_data)
    is Error.SdkVersionTooLow -> resourceProvider.getString(
        R.string.error_sdk_version_too_low,
        BuildUtils.getSdkVersionName(sdkVersion)
    )
    is Error.SdkVersionTooHigh -> resourceProvider.getString(
        R.string.error_sdk_version_too_high,
        BuildUtils.getSdkVersionName(sdkVersion)
    )
    is Error.FeatureUnavailable -> resourceProvider.getString(
        R.string.error_feature_not_available,
        feature
    )
    is Error.SystemActionNotFound -> resourceProvider.getString(
        R.string.error_system_action_not_found,
        id
    )
    is Error.KeyMapperImeNotFound -> resourceProvider.getString(R.string.error_key_mapper_ime_not_found)
    is Error.InputMethodNotFound -> resourceProvider.getString(R.string.error_ime_not_found, id)
    is Error.OptionLabelNotFound -> resourceProvider.getString(
        R.string.error_cant_find_option_label,
        id
    )
    is Error.NoEnabledInputMethods -> resourceProvider.getString(R.string.error_no_enabled_imes)
    is Error.FrontFlashNotFound -> resourceProvider.getString(R.string.error_front_flash_not_found)
    is Error.BackFlashNotFound -> resourceProvider.getString(R.string.error_back_flash_not_found)
    is Error.ImeNotFound -> resourceProvider.getString(R.string.error_ime_not_found, id)
    is Error.DownloadFailed -> resourceProvider.getString(R.string.error_download_failed)
    is Error.FileNotCached -> resourceProvider.getString(R.string.error_file_not_cached)
    is Error.SSLHandshakeError -> resourceProvider.getString(R.string.error_ssl_handshake_exception)
    is Error.DeviceNotFound -> resourceProvider.getString(R.string.error_device_not_found)
    is Error.GenericError -> exception.toString()
    is Error.EmptyJson -> resourceProvider.getString(R.string.error_empty_json)
    is Error.FileAccessDenied -> resourceProvider.getString(R.string.error_file_access_denied)
    is Error.FailedToSplitString -> resourceProvider.getString(
        R.string.error_failed_to_split_string,
        string
    )
    is Error.InvalidNumber -> resourceProvider.getString(R.string.error_invalid_number)
    is Error.NumberTooSmall -> resourceProvider.getString(R.string.error_number_too_small, min)
    is Error.NumberTooBig -> resourceProvider.getString(R.string.error_number_too_big, max)
    is Error.CantBeEmpty -> resourceProvider.getString(R.string.error_cant_be_empty)
    Error.BackupVersionTooNew -> TODO()
    Error.CorruptActionError -> TODO()
    Error.CorruptJsonFile -> TODO()
    Error.NoIncompatibleKeyboardsInstalled -> TODO()
    Error.NoMediaSessions -> TODO()
    Error.NoVoiceAssistant -> TODO()
    is Error.UnknownFileLocation -> TODO()
    RecoverableError.AccessibilityServiceDisabled -> resourceProvider.getString(R.string.error_accessibility_service_disabled)
}

fun Error.getBriefMessage(ctx: Context) = when (this) {
    is RecoverableError.AppNotFound -> ctx.str(R.string.error_app_isnt_installed_brief)

    else -> getFullMessage(ctx)
}