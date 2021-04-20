package io.github.sds100.keymapper.util

import android.content.Context
import io.github.sds100.keymapper.R
import io.github.sds100.keymapper.util.ui.ResourceProvider
import io.github.sds100.keymapper.system.BuildUtils
import io.github.sds100.keymapper.util.result.Error
import io.github.sds100.keymapper.util.result.FixableError

/**
 * Created by sds100 on 29/02/2020.
 */

fun Error.getFullMessage(resourceProvider: ResourceProvider) = when (this) {
    is FixableError.PermissionDenied ->
        FixableError.PermissionDenied.getMessageForPermission(
            resourceProvider,
            permission
        )
    is FixableError.AppNotFound -> resourceProvider.getString(
        R.string.error_app_isnt_installed,
        packageName
    )
    is FixableError.AppDisabled -> resourceProvider.getString(R.string.error_app_is_disabled)
    is FixableError.NoCompatibleImeEnabled -> resourceProvider.getString(R.string.error_ime_service_disabled)
    is FixableError.NoCompatibleImeChosen -> resourceProvider.getString(R.string.error_ime_must_be_chosen)
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
        BuildUtils.getSdkVersionName(minSdk)
    )
    is Error.SdkVersionTooHigh -> resourceProvider.getString(
        R.string.error_sdk_version_too_high,
        BuildUtils.getSdkVersionName(maxSdk)
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
    Error.BackupVersionTooNew ->  resourceProvider.getString(R.string.error_backup_version_too_new)
    Error.CorruptActionError -> throw Exception()
    is Error.CorruptJsonFile -> reason
    Error.NoIncompatibleKeyboardsInstalled -> throw Exception()
    Error.NoMediaSessions -> throw Exception()
    Error.NoVoiceAssistant -> resourceProvider.getString(R.string.errorvoice_assistant_not_found)
    is Error.UnknownFileLocation ->throw Exception()
    FixableError.AccessibilityServiceDisabled -> resourceProvider.getString(R.string.error_accessibility_service_disabled)
    Error.Duplicate -> resourceProvider.getString(R.string.error_duplicate_constraint)
    is Error.ImeNotFoundForPackage -> throw Exception()
    Error.LauncherShortcutsNotSupported -> resourceProvider.getString(R.string.error_launcher_shortcuts_not_supported)
    FixableError.AccessibilityServiceCrashed -> resourceProvider.getString(R.string.error_accessibility_service_crashed)
}