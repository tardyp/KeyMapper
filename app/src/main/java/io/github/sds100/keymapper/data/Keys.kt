package io.github.sds100.keymapper.data

import androidx.datastore.preferences.core.preferencesKey
import androidx.datastore.preferences.core.preferencesSetKey

/**
 * Created by sds100 on 19/01/21.
 */
 object Keys {
    val darkTheme = preferencesKey<String>("pref_dark_theme_mode")
    val hasRootPermission = preferencesKey<Boolean>("pref_allow_root_features")
    val shownAppIntro = preferencesKey<Boolean>("pref_first_time")
    val showImePickerNotification = preferencesKey<Boolean>("pref_show_ime_notification")
    val showToggleKeymapsNotification = preferencesKey<Boolean>("pref_show_remappings_notification")
    val showToggleKeyboardNotification =
        preferencesKey<Boolean>("pref_toggle_key_mapper_keyboard_notification")
    val bluetoothDevicesThatToggleKeyboard = preferencesSetKey<String>("pref_bluetooth_devices")
    val bluetoothDevicesThatShowImePicker =
        preferencesSetKey<String>("pref_bluetooth_devices_show_ime_picker")
    val changeImeOnBtConnect = preferencesKey<Boolean>("pref_auto_change_ime_on_connect_disconnect")
    val showImePickerOnBtConnect = preferencesKey<Boolean>("pref_auto_show_ime_picker")
    val forceVibrate = preferencesKey<Boolean>("pref_force_vibrate")
    val defaultLongPressDelay = preferencesKey<Int>("pref_long_press_delay")
    val defaultDoublePressDelay = preferencesKey<Int>("pref_double_press_delay")
    val defaultVibrateDuration = preferencesKey<Int>("pref_vibrate_duration")
    val defaultRepeatDelay = preferencesKey<Int>("pref_hold_down_delay")
    val defaultRepeatRate = preferencesKey<Int>("pref_repeat_delay")
    val defaultSequenceTriggerTimeout = preferencesKey<Int>("pref_sequence_trigger_timeout")
    val defaultHoldDownDuration = preferencesKey<Int>("pref_hold_down_duration")
    val toggleKeyboardOnToggleKeymaps =
        preferencesKey<Boolean>("key_toggle_keyboard_on_pause_resume_keymaps")
    val automaticBackupLocation = preferencesKey<String>("pref_automatic_backup_location")
    val mappingsPaused = preferencesKey<Boolean>("pref_keymaps_paused")
    val hideHomeScreenAlerts = preferencesKey<Boolean>("pref_hide_home_screen_alerts")
    val showGuiKeyboardAd = preferencesKey<Boolean>("pref_show_gui_keyboard_ad")
    val showDeviceDescriptors = preferencesKey<Boolean>("pref_show_device_descriptors")
    val approvedFingerprintFeaturePrompt =
        preferencesKey<Boolean>("pref_approved_fingerprint_feature_prompt")
    val shownParallelTriggerOrderExplanation =
        preferencesKey<Boolean>("key_shown_parallel_trigger_order_warning")
    val shownSequenceTriggerExplanation =
        preferencesKey<Boolean>("key_shown_sequence_trigger_explanation_dialog")
    val lastInstalledVersionCodeHomeScreen =
        preferencesKey<Int>("last_installed_version_home_screen")
    val lastInstalledVersionCodeAccessibilityService =
        preferencesKey<Int>("last_installed_version_accessibility_service")

    val shownQuickStartGuideHint = preferencesKey<Boolean>("tap_target_quick_start_guide")

    val fingerprintGesturesAvailable =
        preferencesKey<Boolean>("fingerprint_gestures_available")
}