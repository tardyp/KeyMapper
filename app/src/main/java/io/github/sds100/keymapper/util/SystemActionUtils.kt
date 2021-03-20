package io.github.sds100.keymapper.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import io.github.sds100.keymapper.Constants
import io.github.sds100.keymapper.R
import io.github.sds100.keymapper.ServiceLocator
import io.github.sds100.keymapper.data.model.OptionType
import io.github.sds100.keymapper.data.model.SystemActionDef
import io.github.sds100.keymapper.data.model.SystemActionOption
import io.github.sds100.keymapper.domain.actions.SystemActionId
import io.github.sds100.keymapper.util.OldSystemAction.CATEGORY_AIRPLANE_MODE
import io.github.sds100.keymapper.util.OldSystemAction.CATEGORY_BLUETOOTH
import io.github.sds100.keymapper.util.OldSystemAction.CATEGORY_BRIGHTNESS
import io.github.sds100.keymapper.util.OldSystemAction.CATEGORY_FLASHLIGHT
import io.github.sds100.keymapper.util.OldSystemAction.CATEGORY_KEYBOARD
import io.github.sds100.keymapper.util.OldSystemAction.CATEGORY_MEDIA
import io.github.sds100.keymapper.util.OldSystemAction.CATEGORY_MOBILE_DATA
import io.github.sds100.keymapper.util.OldSystemAction.CATEGORY_NAVIGATION
import io.github.sds100.keymapper.util.OldSystemAction.CATEGORY_NFC
import io.github.sds100.keymapper.util.OldSystemAction.CATEGORY_OTHER
import io.github.sds100.keymapper.util.OldSystemAction.CATEGORY_SCREEN_ROTATION
import io.github.sds100.keymapper.util.OldSystemAction.CATEGORY_STATUS_BAR
import io.github.sds100.keymapper.util.OldSystemAction.CATEGORY_VOLUME
import io.github.sds100.keymapper.util.OldSystemAction.CATEGORY_WIFI
import io.github.sds100.keymapper.util.OldSystemAction.COLLAPSE_STATUS_BAR
import io.github.sds100.keymapper.util.OldSystemAction.CONSUME_KEY_EVENT
import io.github.sds100.keymapper.util.OldSystemAction.CYCLE_ROTATIONS
import io.github.sds100.keymapper.util.OldSystemAction.DECREASE_BRIGHTNESS
import io.github.sds100.keymapper.util.OldSystemAction.DISABLE_AIRPLANE_MODE
import io.github.sds100.keymapper.util.OldSystemAction.DISABLE_AUTO_BRIGHTNESS
import io.github.sds100.keymapper.util.OldSystemAction.DISABLE_AUTO_ROTATE
import io.github.sds100.keymapper.util.OldSystemAction.DISABLE_BLUETOOTH
import io.github.sds100.keymapper.util.OldSystemAction.DISABLE_MOBILE_DATA
import io.github.sds100.keymapper.util.OldSystemAction.DISABLE_NFC
import io.github.sds100.keymapper.util.OldSystemAction.DISABLE_WIFI
import io.github.sds100.keymapper.util.OldSystemAction.DISABLE_WIFI_ROOT
import io.github.sds100.keymapper.util.OldSystemAction.ENABLE_AIRPLANE_MODE
import io.github.sds100.keymapper.util.OldSystemAction.ENABLE_AUTO_BRIGHTNESS
import io.github.sds100.keymapper.util.OldSystemAction.ENABLE_AUTO_ROTATE
import io.github.sds100.keymapper.util.OldSystemAction.ENABLE_BLUETOOTH
import io.github.sds100.keymapper.util.OldSystemAction.ENABLE_MOBILE_DATA
import io.github.sds100.keymapper.util.OldSystemAction.ENABLE_NFC
import io.github.sds100.keymapper.util.OldSystemAction.ENABLE_WIFI
import io.github.sds100.keymapper.util.OldSystemAction.ENABLE_WIFI_ROOT
import io.github.sds100.keymapper.util.OldSystemAction.EXPAND_NOTIFICATION_DRAWER
import io.github.sds100.keymapper.util.OldSystemAction.EXPAND_QUICK_SETTINGS
import io.github.sds100.keymapper.util.OldSystemAction.FAST_FORWARD
import io.github.sds100.keymapper.util.OldSystemAction.FAST_FORWARD_PACKAGE
import io.github.sds100.keymapper.util.OldSystemAction.GO_BACK
import io.github.sds100.keymapper.util.OldSystemAction.GO_HOME
import io.github.sds100.keymapper.util.OldSystemAction.GO_LAST_APP
import io.github.sds100.keymapper.util.OldSystemAction.HIDE_KEYBOARD
import io.github.sds100.keymapper.util.OldSystemAction.INCREASE_BRIGHTNESS
import io.github.sds100.keymapper.util.OldSystemAction.LANDSCAPE_MODE
import io.github.sds100.keymapper.util.OldSystemAction.LOCK_DEVICE
import io.github.sds100.keymapper.util.OldSystemAction.LOCK_DEVICE_ROOT
import io.github.sds100.keymapper.util.OldSystemAction.MOVE_CURSOR_TO_END
import io.github.sds100.keymapper.util.OldSystemAction.NEXT_TRACK
import io.github.sds100.keymapper.util.OldSystemAction.NEXT_TRACK_PACKAGE
import io.github.sds100.keymapper.util.OldSystemAction.OPEN_CAMERA
import io.github.sds100.keymapper.util.OldSystemAction.OPEN_DEVICE_ASSISTANT
import io.github.sds100.keymapper.util.OldSystemAction.OPEN_MENU
import io.github.sds100.keymapper.util.OldSystemAction.OPEN_RECENTS
import io.github.sds100.keymapper.util.OldSystemAction.OPEN_SETTINGS
import io.github.sds100.keymapper.util.OldSystemAction.OPEN_VOICE_ASSISTANT
import io.github.sds100.keymapper.util.OldSystemAction.PAUSE_MEDIA
import io.github.sds100.keymapper.util.OldSystemAction.PAUSE_MEDIA_PACKAGE
import io.github.sds100.keymapper.util.OldSystemAction.PLAY_MEDIA
import io.github.sds100.keymapper.util.OldSystemAction.PLAY_MEDIA_PACKAGE
import io.github.sds100.keymapper.util.OldSystemAction.PLAY_PAUSE_MEDIA
import io.github.sds100.keymapper.util.OldSystemAction.PLAY_PAUSE_MEDIA_PACKAGE
import io.github.sds100.keymapper.util.OldSystemAction.PORTRAIT_MODE
import io.github.sds100.keymapper.util.OldSystemAction.POWER_ON_OFF_DEVICE
import io.github.sds100.keymapper.util.OldSystemAction.PREVIOUS_TRACK
import io.github.sds100.keymapper.util.OldSystemAction.PREVIOUS_TRACK_PACKAGE
import io.github.sds100.keymapper.util.OldSystemAction.REWIND
import io.github.sds100.keymapper.util.OldSystemAction.REWIND_PACKAGE
import io.github.sds100.keymapper.util.OldSystemAction.SCREENSHOT
import io.github.sds100.keymapper.util.OldSystemAction.SCREENSHOT_ROOT
import io.github.sds100.keymapper.util.OldSystemAction.SECURE_LOCK_DEVICE
import io.github.sds100.keymapper.util.OldSystemAction.SELECT_WORD_AT_CURSOR
import io.github.sds100.keymapper.util.OldSystemAction.SHOW_KEYBOARD
import io.github.sds100.keymapper.util.OldSystemAction.SHOW_KEYBOARD_PICKER
import io.github.sds100.keymapper.util.OldSystemAction.SHOW_KEYBOARD_PICKER_ROOT
import io.github.sds100.keymapper.util.OldSystemAction.SHOW_POWER_MENU
import io.github.sds100.keymapper.util.OldSystemAction.SWITCH_KEYBOARD
import io.github.sds100.keymapper.util.OldSystemAction.SWITCH_ORIENTATION
import io.github.sds100.keymapper.util.OldSystemAction.TEXT_COPY
import io.github.sds100.keymapper.util.OldSystemAction.TEXT_CUT
import io.github.sds100.keymapper.util.OldSystemAction.TEXT_PASTE
import io.github.sds100.keymapper.util.OldSystemAction.TOGGLE_AIRPLANE_MODE
import io.github.sds100.keymapper.util.OldSystemAction.TOGGLE_AUTO_BRIGHTNESS
import io.github.sds100.keymapper.util.OldSystemAction.TOGGLE_AUTO_ROTATE
import io.github.sds100.keymapper.util.OldSystemAction.TOGGLE_BLUETOOTH
import io.github.sds100.keymapper.util.OldSystemAction.TOGGLE_KEYBOARD
import io.github.sds100.keymapper.util.OldSystemAction.TOGGLE_MOBILE_DATA
import io.github.sds100.keymapper.util.OldSystemAction.TOGGLE_NFC
import io.github.sds100.keymapper.util.OldSystemAction.TOGGLE_NOTIFICATION_DRAWER
import io.github.sds100.keymapper.util.OldSystemAction.TOGGLE_QUICK_SETTINGS_DRAWER
import io.github.sds100.keymapper.util.OldSystemAction.TOGGLE_SPLIT_SCREEN
import io.github.sds100.keymapper.util.OldSystemAction.TOGGLE_WIFI
import io.github.sds100.keymapper.util.OldSystemAction.TOGGLE_WIFI_ROOT
import io.github.sds100.keymapper.util.OldSystemAction.VOLUME_MUTE
import io.github.sds100.keymapper.util.OldSystemAction.VOLUME_TOGGLE_MUTE
import io.github.sds100.keymapper.util.OldSystemAction.VOLUME_UNMUTE
import io.github.sds100.keymapper.util.result.Error
import io.github.sds100.keymapper.util.result.Result
import io.github.sds100.keymapper.util.result.Success
import java.util.*

/**
 * Created by sds100 on 01/08/2018.
 */

object SystemActionUtils {

    /**
     * Maps system action category ids to the string resource of their label
     */
    val CATEGORY_LABEL_MAP = mapOf(
        CATEGORY_WIFI to R.string.system_action_cat_wifi,
        CATEGORY_BLUETOOTH to R.string.system_action_cat_bluetooth,
        CATEGORY_MOBILE_DATA to R.string.system_action_cat_mobile_data,
        CATEGORY_NAVIGATION to R.string.system_action_cat_navigation,
        CATEGORY_SCREEN_ROTATION to R.string.system_action_cat_screen_rotation,
        CATEGORY_VOLUME to R.string.system_action_cat_volume,
        CATEGORY_BRIGHTNESS to R.string.system_action_cat_brightness,
        CATEGORY_STATUS_BAR to R.string.system_action_cat_status_bar,
        CATEGORY_MEDIA to R.string.system_action_cat_media,
        CATEGORY_FLASHLIGHT to R.string.system_action_cat_flashlight,
        CATEGORY_KEYBOARD to R.string.system_action_cat_keyboard,
        CATEGORY_NFC to R.string.system_action_cat_nfc,
        CATEGORY_AIRPLANE_MODE to R.string.system_action_cat_airplane_mode,
        CATEGORY_OTHER to R.string.system_action_cat_other
    )

    @StringRes
    fun getTitle(action: io.github.sds100.keymapper.domain.actions.SystemAction): Int =
        when (action.id) {
            SystemActionId.TOGGLE_WIFI -> R.string.action_toggle_wifi
            SystemActionId.ENABLE_WIFI -> R.string.action_enable_wifi
            SystemActionId.DISABLE_WIFI -> R.string.action_disable_wifi
            SystemActionId.TOGGLE_WIFI_ROOT -> R.string.action_toggle_wifi_root
            SystemActionId.ENABLE_WIFI_ROOT -> R.string.action_enable_wifi_root
            SystemActionId.DISABLE_WIFI_ROOT -> R.string.action_disable_wifi_root
            SystemActionId.TOGGLE_BLUETOOTH -> R.string.action_toggle_bluetooth
            SystemActionId.ENABLE_BLUETOOTH -> R.string.action_enable_bluetooth
            SystemActionId.DISABLE_BLUETOOTH -> R.string.action_disable_bluetooth
            SystemActionId.TOGGLE_MOBILE_DATA -> R.string.action_toggle_mobile_data
            SystemActionId.ENABLE_MOBILE_DATA -> R.string.action_enable_mobile_data
            SystemActionId.DISABLE_MOBILE_DATA -> R.string.action_disable_mobile_data
            SystemActionId.TOGGLE_AUTO_BRIGHTNESS -> R.string.action_toggle_auto_brightness
            SystemActionId.DISABLE_AUTO_BRIGHTNESS -> R.string.action_disable_auto_brightness
            SystemActionId.ENABLE_AUTO_BRIGHTNESS -> R.string.action_enable_auto_brightness
            SystemActionId.INCREASE_BRIGHTNESS -> R.string.action_increase_brightness
            SystemActionId.DECREASE_BRIGHTNESS -> R.string.action_decrease_brightness
            SystemActionId.TOGGLE_AUTO_ROTATE -> R.string.action_toggle_auto_rotate
            SystemActionId.ENABLE_AUTO_ROTATE -> R.string.action_enable_auto_rotate
            SystemActionId.DISABLE_AUTO_ROTATE -> R.string.action_disable_auto_rotate
            SystemActionId.PORTRAIT_MODE -> R.string.action_portrait_mode
            SystemActionId.LANDSCAPE_MODE -> R.string.action_landscape_mode
            SystemActionId.SWITCH_ORIENTATION -> R.string.action_switch_orientation
            SystemActionId.CYCLE_ROTATIONS -> R.string.action_cycle_rotations
            SystemActionId.VOLUME_UP -> R.string.action_volume_up
            SystemActionId.VOLUME_DOWN -> R.string.action_volume_down
            SystemActionId.VOLUME_SHOW_DIALOG -> R.string.action_volume_show_dialog
            SystemActionId.VOLUME_DECREASE_STREAM -> R.string.action_decrease_stream
            SystemActionId.VOLUME_INCREASE_STREAM -> R.string.action_increase_stream
            SystemActionId.CYCLE_RINGER_MODE -> R.string.action_cycle_ringer_mode
            SystemActionId.CHANGE_RINGER_MODE -> R.string.action_change_ringer_mode
            SystemActionId.CYCLE_VIBRATE_RING -> R.string.action_cycle_vibrate_ring
            SystemActionId.TOGGLE_DND_MODE -> R.string.action_toggle_dnd_mode
            SystemActionId.ENABLE_DND_MODE -> R.string.action_enable_dnd_mode
            SystemActionId.DISABLE_DND_MODE -> R.string.action_disable_dnd_mode
            SystemActionId.VOLUME_UNMUTE -> R.string.action_volume_unmute
            SystemActionId.VOLUME_MUTE -> R.string.action_volume_mute
            SystemActionId.VOLUME_TOGGLE_MUTE -> R.string.action_toggle_mute
            SystemActionId.EXPAND_NOTIFICATION_DRAWER -> R.string.action_expand_notification_drawer
            SystemActionId.TOGGLE_NOTIFICATION_DRAWER -> R.string.action_toggle_notification_drawer
            SystemActionId.EXPAND_QUICK_SETTINGS -> R.string.action_expand_quick_settings
            SystemActionId.TOGGLE_QUICK_SETTINGS_DRAWER -> R.string.action_toggle_quick_settings
            SystemActionId.COLLAPSE_STATUS_BAR -> R.string.action_collapse_status_bar
            SystemActionId.PAUSE_MEDIA -> R.string.action_pause_media
            SystemActionId.PAUSE_MEDIA_PACKAGE -> R.string.action_pause_media_package
            SystemActionId.PLAY_MEDIA -> R.string.action_play_media
            SystemActionId.PLAY_MEDIA_PACKAGE -> R.string.action_play_media_package
            SystemActionId.PLAY_PAUSE_MEDIA -> R.string.action_pause_media
            SystemActionId.PLAY_PAUSE_MEDIA_PACKAGE -> R.string.action_pause_media_package
            SystemActionId.NEXT_TRACK -> R.string.action_next_track
            SystemActionId.NEXT_TRACK_PACKAGE -> R.string.action_next_track_package
            SystemActionId.PREVIOUS_TRACK -> R.string.action_previous_track
            SystemActionId.PREVIOUS_TRACK_PACKAGE -> R.string.action_previous_track_package
            SystemActionId.FAST_FORWARD -> R.string.action_fast_forward
            SystemActionId.FAST_FORWARD_PACKAGE -> R.string.action_fast_forward_package
            SystemActionId.REWIND -> R.string.action_rewind
            SystemActionId.REWIND_PACKAGE -> R.string.action_rewind_package
            SystemActionId.GO_BACK -> R.string.action_go_back
            SystemActionId.GO_HOME -> R.string.action_go_home
            SystemActionId.OPEN_RECENTS -> R.string.action_open_recents
            SystemActionId.TOGGLE_SPLIT_SCREEN -> R.string.action_toggle_split_screen
            SystemActionId.GO_LAST_APP -> R.string.action_go_last_app
            SystemActionId.OPEN_MENU -> R.string.action_open_menu
            SystemActionId.TOGGLE_FLASHLIGHT -> R.string.action_toggle_flashlight
            SystemActionId.ENABLE_FLASHLIGHT -> R.string.action_enable_flashlight
            SystemActionId.DISABLE_FLASHLIGHT -> R.string.action_disable_flashlight
            SystemActionId.ENABLE_NFC -> R.string.action_nfc_enable
            SystemActionId.DISABLE_NFC -> R.string.action_nfc_disable
            SystemActionId.TOGGLE_NFC -> R.string.action_nfc_toggle
            SystemActionId.MOVE_CURSOR_TO_END -> R.string.action_move_to_end_of_text
            SystemActionId.TOGGLE_KEYBOARD -> R.string.action_toggle_keyboard
            SystemActionId.SHOW_KEYBOARD -> R.string.action_show_keyboard
            SystemActionId.HIDE_KEYBOARD -> R.string.action_hide_keyboard
            SystemActionId.SHOW_KEYBOARD_PICKER -> R.string.action_show_keyboard_picker
            SystemActionId.SHOW_KEYBOARD_PICKER_ROOT -> R.string.action_show_keyboard_picker_root
            SystemActionId.TEXT_CUT -> R.string.action_text_cut
            SystemActionId.TEXT_COPY -> R.string.action_text_copy
            SystemActionId.TEXT_PASTE -> R.string.action_text_paste
            SystemActionId.SELECT_WORD_AT_CURSOR -> R.string.action_select_word_at_cursor
            SystemActionId.SWITCH_KEYBOARD -> R.string.action_switch_keyboard
            SystemActionId.TOGGLE_AIRPLANE_MODE -> R.string.action_toggle_airplane_mode
            SystemActionId.ENABLE_AIRPLANE_MODE -> R.string.action_enable_airplane_mode
            SystemActionId.DISABLE_AIRPLANE_MODE -> R.string.action_disable_airplane_mode
            SystemActionId.SCREENSHOT -> R.string.action_screenshot
            SystemActionId.SCREENSHOT_ROOT -> R.string.action_screenshot_root
            SystemActionId.OPEN_VOICE_ASSISTANT -> R.string.action_open_assistant
            SystemActionId.OPEN_DEVICE_ASSISTANT -> R.string.action_open_device_assistant
            SystemActionId.OPEN_CAMERA -> R.string.action_open_camera
            SystemActionId.LOCK_DEVICE -> R.string.action_lock_device
            SystemActionId.LOCK_DEVICE_ROOT -> R.string.action_lock_device_root
            SystemActionId.POWER_ON_OFF_DEVICE -> R.string.action_power_on_off_device
            SystemActionId.SECURE_LOCK_DEVICE -> R.string.action_secure_lock_device
            SystemActionId.CONSUME_KEY_EVENT -> R.string.action_consume_keyevent
            SystemActionId.OPEN_SETTINGS -> R.string.action_open_settings
            SystemActionId.SHOW_POWER_MENU -> R.string.action_show_power_menu
        }

    @DrawableRes
    fun getIcon(action: io.github.sds100.keymapper.domain.actions.SystemAction): Int? =
        when (action.id) {
            SystemActionId.TOGGLE_WIFI -> R.drawable.ic_outline_wifi_24
            SystemActionId.ENABLE_WIFI -> R.drawable.ic_outline_wifi_24
            SystemActionId.DISABLE_WIFI -> R.drawable.ic_outline_wifi_off_24
            SystemActionId.TOGGLE_WIFI_ROOT -> R.drawable.ic_outline_wifi_24
            SystemActionId.ENABLE_WIFI_ROOT -> R.drawable.ic_outline_wifi_24
            SystemActionId.DISABLE_WIFI_ROOT -> R.drawable.ic_outline_wifi_off_24
            SystemActionId.TOGGLE_BLUETOOTH -> R.drawable.ic_outline_bluetooth_24
            SystemActionId.ENABLE_BLUETOOTH -> R.drawable.ic_outline_bluetooth_24
            SystemActionId.DISABLE_BLUETOOTH -> R.drawable.ic_outline_bluetooth_disabled_24
            SystemActionId.TOGGLE_MOBILE_DATA -> R.drawable.ic_outline_signal_cellular_4_bar_24
            SystemActionId.ENABLE_MOBILE_DATA -> R.drawable.ic_outline_signal_cellular_4_bar_24
            SystemActionId.DISABLE_MOBILE_DATA -> R.drawable.ic_outline_signal_cellular_off_24
            SystemActionId.TOGGLE_AUTO_BRIGHTNESS -> R.drawable.ic_outline_brightness_auto_24
            SystemActionId.DISABLE_AUTO_BRIGHTNESS -> R.drawable.ic_disable_brightness_auto_24dp
            SystemActionId.ENABLE_AUTO_BRIGHTNESS -> R.drawable.ic_outline_brightness_auto_24
            SystemActionId.INCREASE_BRIGHTNESS -> R.drawable.ic_outline_brightness_high_24
            SystemActionId.DECREASE_BRIGHTNESS -> R.drawable.ic_outline_brightness_low_24
            SystemActionId.TOGGLE_AUTO_ROTATE -> R.drawable.ic_outline_screen_rotation_24
            SystemActionId.ENABLE_AUTO_ROTATE -> R.drawable.ic_outline_screen_rotation_24
            SystemActionId.DISABLE_AUTO_ROTATE -> R.drawable.ic_outline_screen_lock_rotation_24
            SystemActionId.PORTRAIT_MODE -> R.drawable.ic_outline_stay_current_portrait_24
            SystemActionId.LANDSCAPE_MODE -> R.drawable.ic_outline_stay_current_landscape_24
            SystemActionId.SWITCH_ORIENTATION -> R.drawable.ic_outline_screen_rotation_24
            SystemActionId.CYCLE_ROTATIONS -> R.drawable.ic_outline_screen_rotation_24
            SystemActionId.VOLUME_UP -> R.drawable.ic_outline_volume_up_24
            SystemActionId.VOLUME_DOWN -> R.drawable.ic_outline_volume_down_24
            SystemActionId.VOLUME_SHOW_DIALOG -> null
            SystemActionId.VOLUME_DECREASE_STREAM -> R.drawable.ic_outline_volume_down_24
            SystemActionId.VOLUME_INCREASE_STREAM -> R.drawable.ic_outline_volume_up_24
            SystemActionId.CYCLE_RINGER_MODE -> null
            SystemActionId.CHANGE_RINGER_MODE -> null
            SystemActionId.CYCLE_VIBRATE_RING -> null
            SystemActionId.TOGGLE_DND_MODE -> R.drawable.dnd_circle_outline
            SystemActionId.ENABLE_DND_MODE -> R.drawable.dnd_circle_outline
            SystemActionId.DISABLE_DND_MODE -> R.drawable.dnd_circle_off_outline
            SystemActionId.VOLUME_UNMUTE -> R.drawable.ic_outline_volume_up_24
            SystemActionId.VOLUME_MUTE -> R.drawable.ic_outline_volume_mute_24
            SystemActionId.VOLUME_TOGGLE_MUTE -> R.drawable.ic_outline_volume_mute_24
            SystemActionId.EXPAND_NOTIFICATION_DRAWER -> null
            SystemActionId.TOGGLE_NOTIFICATION_DRAWER -> null
            SystemActionId.EXPAND_QUICK_SETTINGS -> null
            SystemActionId.TOGGLE_QUICK_SETTINGS_DRAWER ->null
            SystemActionId.COLLAPSE_STATUS_BAR -> null
            SystemActionId.PAUSE_MEDIA -> R.drawable.ic_outline_pause_24
            SystemActionId.PAUSE_MEDIA_PACKAGE -> R.drawable.ic_outline_pause_24
            SystemActionId.PLAY_MEDIA -> R.drawable.ic_outline_play_arrow_24
            SystemActionId.PLAY_MEDIA_PACKAGE -> R.drawable.ic_outline_play_arrow_24
            SystemActionId.PLAY_PAUSE_MEDIA -> R.drawable.ic_play_pause_24dp
            SystemActionId.PLAY_PAUSE_MEDIA_PACKAGE -> R.drawable.ic_play_pause_24dp
            SystemActionId.NEXT_TRACK -> R.drawable.ic_outline_skip_next_24
            SystemActionId.NEXT_TRACK_PACKAGE -> R.drawable.ic_outline_skip_next_24
            SystemActionId.PREVIOUS_TRACK -> R.drawable.ic_outline_skip_previous_24
            SystemActionId.PREVIOUS_TRACK_PACKAGE -> R.drawable.ic_outline_skip_previous_24
            SystemActionId.FAST_FORWARD -> R.drawable.ic_outline_fast_forward_24
            SystemActionId.FAST_FORWARD_PACKAGE -> R.drawable.ic_outline_fast_forward_24
            SystemActionId.REWIND -> R.drawable.ic_outline_fast_rewind_24
            SystemActionId.REWIND_PACKAGE -> R.drawable.ic_outline_fast_rewind_24
            SystemActionId.GO_BACK -> R.drawable.ic_baseline_arrow_back_24
            SystemActionId.GO_HOME -> R.drawable.ic_outline_home_24
            SystemActionId.OPEN_RECENTS -> null
            SystemActionId.TOGGLE_SPLIT_SCREEN -> null
            SystemActionId.GO_LAST_APP -> null
            SystemActionId.OPEN_MENU -> R.drawable.ic_outline_more_vert_24
            SystemActionId.TOGGLE_FLASHLIGHT -> R.drawable.ic_flashlight
            SystemActionId.ENABLE_FLASHLIGHT -> R.drawable.ic_flashlight
            SystemActionId.DISABLE_FLASHLIGHT -> R.drawable.ic_flashlight_off
            SystemActionId.ENABLE_NFC -> R.drawable.ic_outline_nfc_24
            SystemActionId.DISABLE_NFC -> R.drawable.ic_nfc_off
            SystemActionId.TOGGLE_NFC -> R.drawable.ic_outline_nfc_24
            SystemActionId.MOVE_CURSOR_TO_END -> R.drawable.ic_cursor
            SystemActionId.TOGGLE_KEYBOARD -> R.drawable.ic_outline_keyboard_24
            SystemActionId.SHOW_KEYBOARD -> R.drawable.ic_outline_keyboard_24
            SystemActionId.HIDE_KEYBOARD -> R.drawable.ic_outline_keyboard_hide_24
            SystemActionId.SHOW_KEYBOARD_PICKER -> R.drawable.ic_outline_keyboard_24
            SystemActionId.SHOW_KEYBOARD_PICKER_ROOT -> R.drawable.ic_outline_keyboard_24
            SystemActionId.TEXT_CUT -> R.drawable.ic_content_cut
            SystemActionId.TEXT_COPY -> R.drawable.ic_content_copy
            SystemActionId.TEXT_PASTE -> R.drawable.ic_content_paste
            SystemActionId.SELECT_WORD_AT_CURSOR ->null
            SystemActionId.SWITCH_KEYBOARD -> R.drawable.ic_outline_keyboard_24
            SystemActionId.TOGGLE_AIRPLANE_MODE -> R.drawable.ic_outline_airplanemode_active_24
            SystemActionId.ENABLE_AIRPLANE_MODE -> R.drawable.ic_outline_airplanemode_active_24
            SystemActionId.DISABLE_AIRPLANE_MODE -> R.drawable.ic_outline_airplanemode_inactive_24
            SystemActionId.SCREENSHOT -> R.drawable.ic_outline_fullscreen_24
            SystemActionId.SCREENSHOT_ROOT -> R.drawable.ic_outline_fullscreen_24
            SystemActionId.OPEN_VOICE_ASSISTANT -> R.drawable.ic_outline_assistant_24
            SystemActionId.OPEN_DEVICE_ASSISTANT -> R.drawable.ic_outline_assistant_24
            SystemActionId.OPEN_CAMERA -> R.drawable.ic_outline_camera_alt_24
            SystemActionId.LOCK_DEVICE -> R.drawable.ic_outline_lock_24
            SystemActionId.LOCK_DEVICE_ROOT -> R.drawable.ic_outline_lock_24
            SystemActionId.POWER_ON_OFF_DEVICE -> R.drawable.ic_outline_power_settings_new_24
            SystemActionId.SECURE_LOCK_DEVICE -> R.drawable.ic_outline_lock_24
            SystemActionId.CONSUME_KEY_EVENT -> null
            SystemActionId.OPEN_SETTINGS -> R.drawable.ic_outline_settings_24
            SystemActionId.SHOW_POWER_MENU -> R.drawable.ic_outline_power_settings_new_24
        }

    fun getMinApi(action: io.github.sds100.keymapper.domain.actions.SystemAction): Int {
        return when (action.id) {
            SystemActionId.TOGGLE_SPLIT_SCREEN -> Build.VERSION_CODES.N
            SystemActionId.GO_LAST_APP -> Build.VERSION_CODES.N

            SystemActionId.PLAY_PAUSE_MEDIA,
            SystemActionId.PAUSE_MEDIA,
            SystemActionId.PLAY_MEDIA,
            SystemActionId.NEXT_TRACK,
            SystemActionId.PREVIOUS_TRACK,
            SystemActionId.FAST_FORWARD,
            SystemActionId.REWIND,
            -> Build.VERSION_CODES.KITKAT

            SystemActionId.PLAY_PAUSE_MEDIA_PACKAGE,
            SystemActionId.PAUSE_MEDIA_PACKAGE,
            SystemActionId.PLAY_MEDIA_PACKAGE,
            SystemActionId.NEXT_TRACK_PACKAGE,
            SystemActionId.PREVIOUS_TRACK_PACKAGE,
            SystemActionId.FAST_FORWARD_PACKAGE,
            SystemActionId.REWIND_PACKAGE,
            -> Build.VERSION_CODES.LOLLIPOP

            SystemActionId.VOLUME_MUTE,
            SystemActionId.VOLUME_UNMUTE,
            SystemActionId.VOLUME_TOGGLE_MUTE,
            SystemActionId.TOGGLE_DND_MODE,
            SystemActionId.ENABLE_DND_MODE,
            SystemActionId.DISABLE_DND_MODE,
            -> Build.VERSION_CODES.M

            SystemActionId.DISABLE_FLASHLIGHT,
            SystemActionId.ENABLE_FLASHLIGHT,
            SystemActionId.TOGGLE_FLASHLIGHT,
            -> Build.VERSION_CODES.M

            SystemActionId.TOGGLE_KEYBOARD,
            SystemActionId.SHOW_KEYBOARD,
            SystemActionId.HIDE_KEYBOARD,
            -> Build.VERSION_CODES.N

            SystemActionId.TEXT_CUT,
            SystemActionId.TEXT_COPY,
            SystemActionId.TEXT_PASTE,
            SystemActionId.SELECT_WORD_AT_CURSOR,
            -> Build.VERSION_CODES.JELLY_BEAN_MR2

            SystemActionId.SHOW_POWER_MENU -> Build.VERSION_CODES.LOLLIPOP

            else -> Constants.MIN_API
        }
    }

    fun getMaxApi(action: io.github.sds100.keymapper.domain.actions.SystemAction): Int {
        return when (action.id) {
            SystemActionId.SHOW_KEYBOARD_PICKER -> Build.VERSION_CODES.P

            else -> Constants.MAX_API
        }
    }

    fun getRequiredSystemFeatures(action: io.github.sds100.keymapper.domain.actions.SystemAction): List<String> {
        return when (action.id) {
            SystemActionId.SECURE_LOCK_DEVICE
            -> listOf(PackageManager.FEATURE_DEVICE_ADMIN)

            SystemActionId.TOGGLE_NFC,
            SystemActionId.ENABLE_NFC,
            SystemActionId.DISABLE_NFC,
            -> listOf(PackageManager.FEATURE_NFC)

            SystemActionId.TOGGLE_FLASHLIGHT,
            SystemActionId.ENABLE_FLASHLIGHT,
            SystemActionId.DISABLE_FLASHLIGHT,
            -> listOf(PackageManager.FEATURE_CAMERA_FLASH)

            else -> emptyList()
        }
    }

    fun getRequiredPermissions(action: io.github.sds100.keymapper.domain.actions.SystemAction): List<String> {
        when (action.id) {
            SystemActionId.TOGGLE_WIFI,
            SystemActionId.ENABLE_WIFI,
            SystemActionId.DISABLE_WIFI,
            -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                return listOf(Constants.PERMISSION_ROOT)
            }

            SystemActionId.TOGGLE_MOBILE_DATA,
            SystemActionId.ENABLE_MOBILE_DATA,
            SystemActionId.DISABLE_MOBILE_DATA,
            -> return listOf(Constants.PERMISSION_ROOT)

            SystemActionId.PLAY_PAUSE_MEDIA_PACKAGE,
            SystemActionId.PAUSE_MEDIA_PACKAGE,
            SystemActionId.PLAY_MEDIA_PACKAGE,
            SystemActionId.NEXT_TRACK_PACKAGE,
            SystemActionId.PREVIOUS_TRACK_PACKAGE,
            SystemActionId.FAST_FORWARD_PACKAGE,
            SystemActionId.REWIND_PACKAGE,
            -> Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE

            SystemActionId.VOLUME_UP,
            SystemActionId.VOLUME_DOWN,
            SystemActionId.VOLUME_INCREASE_STREAM,
            SystemActionId.VOLUME_DECREASE_STREAM,
            SystemActionId.VOLUME_SHOW_DIALOG,
            SystemActionId.CYCLE_RINGER_MODE,
            SystemActionId.CYCLE_VIBRATE_RING,
            SystemActionId.CHANGE_RINGER_MODE,
            SystemActionId.VOLUME_MUTE,
            SystemActionId.VOLUME_UNMUTE,
            SystemActionId.VOLUME_TOGGLE_MUTE,
            SystemActionId.TOGGLE_DND_MODE,
            SystemActionId.DISABLE_DND_MODE,
            SystemActionId.ENABLE_DND_MODE,
            -> Manifest.permission.ACCESS_NOTIFICATION_POLICY

            SystemActionId.TOGGLE_AUTO_ROTATE,
            SystemActionId.ENABLE_AUTO_ROTATE,
            SystemActionId.DISABLE_AUTO_ROTATE,
            SystemActionId.PORTRAIT_MODE,
            SystemActionId.LANDSCAPE_MODE,
            SystemActionId.SWITCH_ORIENTATION,
            SystemActionId.CYCLE_ROTATIONS,
            -> Manifest.permission.WRITE_SETTINGS

            SystemActionId.TOGGLE_AUTO_BRIGHTNESS,
            SystemActionId.ENABLE_AUTO_BRIGHTNESS,
            SystemActionId.DISABLE_AUTO_BRIGHTNESS,
            SystemActionId.INCREASE_BRIGHTNESS,
            SystemActionId.DECREASE_BRIGHTNESS,
            -> Manifest.permission.WRITE_SETTINGS

            SystemActionId.TOGGLE_FLASHLIGHT,
            SystemActionId.ENABLE_FLASHLIGHT,
            SystemActionId.DISABLE_FLASHLIGHT,
            -> Manifest.permission.CAMERA

            SystemActionId.ENABLE_NFC,
            SystemActionId.DISABLE_NFC,
            SystemActionId.TOGGLE_NFC,
            -> Constants.PERMISSION_ROOT

            SystemActionId.SHOW_KEYBOARD_PICKER ->
                if (Build.VERSION.SDK_INT in arrayOf(
                        Build.VERSION_CODES.O_MR1,
                        Build.VERSION_CODES.P
                    )
                ) {
                    arrayOf(Constants.PERMISSION_ROOT)
                } else {
                    emptyArray()
                }

            SystemActionId.SWITCH_KEYBOARD -> if (!KeyboardUtils.CAN_ACCESSIBILITY_SERVICE_SWITCH_KEYBOARD) {
                arrayOf(Manifest.permission.WRITE_SECURE_SETTINGS)
            } else {
                emptyArray()
            }

            SystemActionId.TOGGLE_AIRPLANE_MODE,
            SystemActionId.ENABLE_AIRPLANE_MODE,
            SystemActionId.DISABLE_AIRPLANE_MODE,
            -> Constants.PERMISSION_ROOT

            SystemActionId.SCREENSHOT -> if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                arrayOf(Constants.PERMISSION_ROOT)
            } else {
                emptyArray()
            }

            SystemActionId.LOCK_DEVICE -> if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
                arrayOf(Constants.PERMISSION_ROOT)
            } else {
                emptyArray()
            }

            SystemActionId.SECURE_LOCK_DEVICE -> arrayOf(Manifest.permission.BIND_DEVICE_ADMIN)
            SystemActionId.POWER_ON_OFF_DEVICE -> arrayOf(Constants.PERMISSION_ROOT)
        }

        return emptyList()
    }

    /**
     * A sorted list of system action definitions
     */
    @SuppressLint("NewApi")
    private val SYSTEM_ACTION_DEFINITIONS = listOf(

        //NAVIGATION
        SystemActionDef(
            id = GO_BACK,
            category = CATEGORY_NAVIGATION,
            iconRes = R.drawable.ic_baseline_arrow_back_24,
            descriptionRes = R.string.action_go_back
        ),
        SystemActionDef(
            id = GO_HOME,
            category = CATEGORY_NAVIGATION,
            iconRes = R.drawable.ic_outline_home_24,
            descriptionRes = R.string.action_go_home
        ),
        SystemActionDef(
            id = OPEN_RECENTS,
            category = CATEGORY_NAVIGATION,
            descriptionRes = R.string.action_open_recents
        ),
        SystemActionDef(
            id = OPEN_MENU,
            category = CATEGORY_NAVIGATION,
            iconRes = R.drawable.ic_outline_more_vert_24,
            descriptionRes = R.string.action_open_menu
        ),
        SystemActionDef(
            id = TOGGLE_SPLIT_SCREEN,
            category = CATEGORY_NAVIGATION,
            descriptionRes = R.string.action_toggle_split_screen,
            minApi = Build.VERSION_CODES.N
        ),
        SystemActionDef(
            id = GO_LAST_APP,
            category = CATEGORY_NAVIGATION,
            descriptionRes = R.string.action_go_last_app,
            minApi = Build.VERSION_CODES.N
        ),
        //NAVIGATION

        //STATUS BAR
        SystemActionDef(
            id = EXPAND_NOTIFICATION_DRAWER,
            category = CATEGORY_STATUS_BAR,
            descriptionRes = R.string.action_expand_notification_drawer
        ),
        SystemActionDef(
            id = TOGGLE_NOTIFICATION_DRAWER,
            category = CATEGORY_STATUS_BAR,
            descriptionRes = R.string.action_toggle_notification_drawer
        ),
        SystemActionDef(
            id = EXPAND_QUICK_SETTINGS,
            category = CATEGORY_STATUS_BAR,
            descriptionRes = R.string.action_expand_quick_settings
        ),
        SystemActionDef(
            id = TOGGLE_QUICK_SETTINGS_DRAWER,
            category = CATEGORY_STATUS_BAR,
            descriptionRes = R.string.action_toggle_quick_settings
        ),
        SystemActionDef(
            id = COLLAPSE_STATUS_BAR,
            category = CATEGORY_STATUS_BAR,
            descriptionRes = R.string.action_collapse_status_bar
        ),
        //STATUS BAR

        //WIFI
        SystemActionDef(
            id = TOGGLE_WIFI,
            category = CATEGORY_WIFI,
            iconRes = R.drawable.ic_outline_wifi_24,
            maxApi = Build.VERSION_CODES.P,
            descriptionRes = R.string.action_toggle_wifi
        ),
        SystemActionDef(
            id = ENABLE_WIFI,
            category = CATEGORY_WIFI,
            maxApi = Build.VERSION_CODES.P,
            iconRes = R.drawable.ic_outline_wifi_24,
            descriptionRes = R.string.action_enable_wifi
        ),
        SystemActionDef(
            id = DISABLE_WIFI,
            category = CATEGORY_WIFI,
            maxApi = Build.VERSION_CODES.P,
            iconRes = R.drawable.ic_outline_wifi_off_24,
            descriptionRes = R.string.action_disable_wifi
        ),

        SystemActionDef(
            id = TOGGLE_WIFI_ROOT,
            category = CATEGORY_WIFI,
            minApi = Build.VERSION_CODES.Q,
            iconRes = R.drawable.ic_outline_wifi_24,
            descriptionRes = R.string.action_toggle_wifi_root,
            permissions = arrayOf(Constants.PERMISSION_ROOT)
        ),

        SystemActionDef(
            id = ENABLE_WIFI_ROOT,
            category = CATEGORY_WIFI,
            minApi = Build.VERSION_CODES.Q,
            iconRes = R.drawable.ic_outline_wifi_24,
            descriptionRes = R.string.action_enable_wifi_root,
            permissions = arrayOf(Constants.PERMISSION_ROOT)
        ),

        SystemActionDef(
            id = DISABLE_WIFI_ROOT,
            category = CATEGORY_WIFI,
            minApi = Build.VERSION_CODES.Q,
            iconRes = R.drawable.ic_outline_wifi_off_24,
            descriptionRes = R.string.action_toggle_wifi_root,
            permissions = arrayOf(Constants.PERMISSION_ROOT)
        ),
        //WIFI

        //BLUETOOTH
        SystemActionDef(
            id = TOGGLE_BLUETOOTH,
            category = CATEGORY_BLUETOOTH,
            iconRes = R.drawable.ic_outline_bluetooth_24,
            descriptionRes = R.string.action_toggle_bluetooth
        ),
        SystemActionDef(
            id = ENABLE_BLUETOOTH,
            category = CATEGORY_BLUETOOTH,
            iconRes = R.drawable.ic_outline_bluetooth_24,
            descriptionRes = R.string.action_enable_bluetooth
        ),
        SystemActionDef(
            id = DISABLE_BLUETOOTH,
            category = CATEGORY_BLUETOOTH,
            iconRes = R.drawable.ic_outline_bluetooth_disabled_24,
            descriptionRes = R.string.action_disable_bluetooth
        ),
        //BLUETOOTH

        //MOBILE DATA REQUIRES ROOT!
        SystemActionDef(
            id = TOGGLE_MOBILE_DATA,
            category = CATEGORY_MOBILE_DATA,
            iconRes = R.drawable.ic_outline_signal_cellular_4_bar_24,
            /*needs READ_PHONE_STATE permission so it can check whether mobile data is enabled. On some devices
            * it seems to need this permission.*/
            permissions = arrayOf(Constants.PERMISSION_ROOT, Manifest.permission.READ_PHONE_STATE),
            descriptionRes = R.string.action_toggle_mobile_data
        ),
        SystemActionDef(
            id = ENABLE_MOBILE_DATA,
            category = CATEGORY_MOBILE_DATA,
            iconRes = R.drawable.ic_outline_signal_cellular_4_bar_24,
            permissions = arrayOf(Constants.PERMISSION_ROOT),
            descriptionRes = R.string.action_enable_mobile_data
        ),
        SystemActionDef(
            id = DISABLE_MOBILE_DATA,
            category = CATEGORY_MOBILE_DATA,
            iconRes = R.drawable.ic_outline_signal_cellular_off_24,
            permissions = arrayOf(Constants.PERMISSION_ROOT),
            descriptionRes = R.string.action_disable_mobile_data
        ),
        //MOBILE DATA

        //MEDIA
        SystemActionDef(
            id = PLAY_PAUSE_MEDIA,
            category = CATEGORY_MEDIA,
            iconRes = R.drawable.ic_play_pause_24dp,
            descriptionRes = R.string.action_play_pause_media,
            minApi = Build.VERSION_CODES.KITKAT
        ),
        SystemActionDef(
            id = PLAY_PAUSE_MEDIA_PACKAGE,
            category = CATEGORY_MEDIA,
            iconRes = R.drawable.ic_play_pause_24dp,
            descriptionRes = R.string.action_play_pause_media_package,
            descriptionFormattedRes = R.string.action_play_pause_media_package_formatted,
            minApi = Build.VERSION_CODES.LOLLIPOP,
            getOptions = { ctx -> getPackagesSortedByName(ctx) },
            permissions = arrayOf(Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE)
        ),
        SystemActionDef(
            id = PAUSE_MEDIA,
            category = CATEGORY_MEDIA,
            iconRes = R.drawable.ic_outline_pause_24,
            descriptionRes = R.string.action_pause_media,
            minApi = Build.VERSION_CODES.KITKAT

        ),
        SystemActionDef(
            id = PAUSE_MEDIA_PACKAGE,
            category = CATEGORY_MEDIA,
            iconRes = R.drawable.ic_outline_pause_24,
            descriptionRes = R.string.action_pause_media_package,
            descriptionFormattedRes = R.string.action_pause_media_package_formatted,
            minApi = Build.VERSION_CODES.LOLLIPOP,
            getOptions = { ctx -> getPackagesSortedByName(ctx) },
            permissions = arrayOf(Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE)
        ),
        SystemActionDef(
            id = PLAY_MEDIA,
            category = CATEGORY_MEDIA,
            iconRes = R.drawable.ic_outline_play_arrow_24,
            descriptionRes = R.string.action_play_media,
            minApi = Build.VERSION_CODES.KITKAT
        ),
        SystemActionDef(
            id = PLAY_MEDIA_PACKAGE,
            category = CATEGORY_MEDIA,
            iconRes = R.drawable.ic_outline_play_arrow_24,
            descriptionRes = R.string.action_play_media_package,
            descriptionFormattedRes = R.string.action_play_media_package_formatted,
            minApi = Build.VERSION_CODES.LOLLIPOP,
            getOptions = { ctx -> getPackagesSortedByName(ctx) },
            permissions = arrayOf(Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE)
        ),
        SystemActionDef(
            id = NEXT_TRACK,
            category = CATEGORY_MEDIA,
            iconRes = R.drawable.ic_outline_skip_next_24,
            descriptionRes = R.string.action_next_track,
            minApi = Build.VERSION_CODES.KITKAT
        ),
        SystemActionDef(
            id = NEXT_TRACK_PACKAGE,
            category = CATEGORY_MEDIA,
            iconRes = R.drawable.ic_outline_skip_next_24,
            descriptionRes = R.string.action_next_track_package,
            descriptionFormattedRes = R.string.action_next_track_package_formatted,
            minApi = Build.VERSION_CODES.LOLLIPOP,
            getOptions = { ctx -> getPackagesSortedByName(ctx) },
            permissions = arrayOf(Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE)
        ),
        SystemActionDef(
            id = PREVIOUS_TRACK,
            category = CATEGORY_MEDIA,
            iconRes = R.drawable.ic_outline_skip_previous_24,
            descriptionRes = R.string.action_previous_track,
            minApi = Build.VERSION_CODES.KITKAT
        ),
        SystemActionDef(
            id = PREVIOUS_TRACK_PACKAGE,
            category = CATEGORY_MEDIA,
            iconRes = R.drawable.ic_outline_skip_previous_24,
            descriptionRes = R.string.action_previous_track_package,
            descriptionFormattedRes = R.string.action_previous_track_package_formatted,
            minApi = Build.VERSION_CODES.LOLLIPOP,
            getOptions = { ctx -> getPackagesSortedByName(ctx) },
            permissions = arrayOf(Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE)
        ),
        SystemActionDef(
            id = FAST_FORWARD,
            category = CATEGORY_MEDIA,
            iconRes = R.drawable.ic_outline_fast_forward_24,
            descriptionRes = R.string.action_fast_forward,
            messageOnSelection = R.string.action_fast_forward_message,
            minApi = Build.VERSION_CODES.KITKAT
        ),
        SystemActionDef(
            id = FAST_FORWARD_PACKAGE,
            category = CATEGORY_MEDIA,
            iconRes = R.drawable.ic_outline_fast_forward_24,
            descriptionRes = R.string.action_fast_forward_package,
            descriptionFormattedRes = R.string.action_fast_forward_package_formatted,
            messageOnSelection = R.string.action_fast_forward_message,
            minApi = Build.VERSION_CODES.LOLLIPOP,
            getOptions = { ctx -> getPackagesSortedByName(ctx) },
            permissions = arrayOf(Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE)
        ),
        SystemActionDef(
            id = REWIND,
            category = CATEGORY_MEDIA,
            iconRes = R.drawable.ic_outline_fast_rewind_24,
            descriptionRes = R.string.action_rewind,
            messageOnSelection = R.string.action_rewind_message,
            minApi = Build.VERSION_CODES.KITKAT
        ),
        SystemActionDef(
            id = REWIND_PACKAGE,
            category = CATEGORY_MEDIA,
            iconRes = R.drawable.ic_outline_fast_rewind_24,
            descriptionRes = R.string.action_rewind_package,
            descriptionFormattedRes = R.string.action_rewind_package_formatted,
            messageOnSelection = R.string.action_rewind_message,
            minApi = Build.VERSION_CODES.LOLLIPOP,
            getOptions = { ctx -> getPackagesSortedByName(ctx) },
            permissions = arrayOf(Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE)
        ),
        //MEDIA

        //VOLUME
        SystemActionDef(
            id = OldSystemAction.VOLUME_UP,
            category = CATEGORY_VOLUME,
            iconRes = R.drawable.ic_outline_volume_up_24,
            descriptionRes = R.string.action_volume_up,
            permissions = arrayOf(Manifest.permission.ACCESS_NOTIFICATION_POLICY)
        ),
        SystemActionDef(
            id = OldSystemAction.VOLUME_DOWN,
            category = CATEGORY_VOLUME,
            iconRes = R.drawable.ic_outline_volume_down_24,
            descriptionRes = R.string.action_volume_down,
            permissions = arrayOf(Manifest.permission.ACCESS_NOTIFICATION_POLICY)
        ),
        SystemActionDef(
            id = OldSystemAction.VOLUME_INCREASE_STREAM,
            category = CATEGORY_VOLUME,
            iconRes = R.drawable.ic_outline_volume_up_24,
            descriptionRes = R.string.action_increase_stream,
            permissions = arrayOf(Manifest.permission.ACCESS_NOTIFICATION_POLICY),
            descriptionFormattedRes = R.string.action_increase_stream_formatted,
            options = SystemActionOption.STREAMS
        ),
        SystemActionDef(
            id = OldSystemAction.VOLUME_DECREASE_STREAM,
            category = CATEGORY_VOLUME,
            iconRes = R.drawable.ic_outline_volume_down_24,
            descriptionRes = R.string.action_decrease_stream,
            descriptionFormattedRes = R.string.action_increase_stream_formatted,
            options = SystemActionOption.STREAMS,
            permissions = arrayOf(Manifest.permission.ACCESS_NOTIFICATION_POLICY)
        ),
        SystemActionDef(
            id = OldSystemAction.VOLUME_SHOW_DIALOG,
            category = CATEGORY_VOLUME,
            descriptionRes = R.string.action_volume_show_dialog
        ),
        SystemActionDef(
            id = OldSystemAction.CYCLE_RINGER_MODE,
            category = CATEGORY_VOLUME,
            descriptionRes = R.string.action_cycle_ringer_mode,
            permissions = arrayOf(Manifest.permission.ACCESS_NOTIFICATION_POLICY)
        ),
        SystemActionDef(
            id = OldSystemAction.CYCLE_VIBRATE_RING,
            category = CATEGORY_VOLUME,
            descriptionRes = R.string.action_cycle_vibrate_ring,
            permissions = arrayOf(Manifest.permission.ACCESS_NOTIFICATION_POLICY)
        ),
        SystemActionDef(
            id = OldSystemAction.CHANGE_RINGER_MODE,
            category = CATEGORY_VOLUME,
            descriptionRes = R.string.action_change_ringer_mode,
            descriptionFormattedRes = R.string.action_change_ringer_mode_formatted,
            permissions = arrayOf(Manifest.permission.ACCESS_NOTIFICATION_POLICY),
            options = listOf(
                SystemActionOption.RINGER_MODE_NORMAL,
                SystemActionOption.RINGER_MODE_VIBRATE,
                SystemActionOption.RINGER_MODE_SILENT
            )
        ),

        //Require Marshmallow and higher
        SystemActionDef(
            id = VOLUME_MUTE,
            category = CATEGORY_VOLUME,
            minApi = Build.VERSION_CODES.M,
            iconRes = R.drawable.ic_outline_volume_mute_24,
            descriptionRes = R.string.action_volume_mute,
            permissions = arrayOf(Manifest.permission.ACCESS_NOTIFICATION_POLICY)
        ),
        SystemActionDef(
            id = VOLUME_UNMUTE,
            category = CATEGORY_VOLUME,
            minApi = Build.VERSION_CODES.M,
            iconRes = R.drawable.ic_outline_volume_up_24,
            descriptionRes = R.string.action_volume_unmute,
            permissions = arrayOf(Manifest.permission.ACCESS_NOTIFICATION_POLICY)
        ),
        SystemActionDef(
            id = VOLUME_TOGGLE_MUTE,
            category = CATEGORY_VOLUME,
            minApi = Build.VERSION_CODES.M,
            iconRes = R.drawable.ic_outline_volume_mute_24,
            descriptionRes = R.string.action_toggle_mute,
            permissions = arrayOf(Manifest.permission.ACCESS_NOTIFICATION_POLICY)
        ),

        SystemActionDef(
            id = OldSystemAction.TOGGLE_DND_MODE,
            category = CATEGORY_VOLUME,
            minApi = Build.VERSION_CODES.M,
            iconRes = R.drawable.dnd_circle_outline,
            descriptionRes = R.string.action_toggle_dnd_mode,
            descriptionFormattedRes = R.string.action_toggle_dnd_mode_formatted,
            permissions = arrayOf(Manifest.permission.ACCESS_NOTIFICATION_POLICY),
            options = SystemActionOption.DND_MODES
        ),

        SystemActionDef(
            id = OldSystemAction.ENABLE_DND_MODE,
            category = CATEGORY_VOLUME,
            minApi = Build.VERSION_CODES.M,
            iconRes = R.drawable.dnd_circle_outline,
            descriptionRes = R.string.action_enable_dnd_mode,
            descriptionFormattedRes = R.string.action_enable_dnd_mode_formatted,
            permissions = arrayOf(Manifest.permission.ACCESS_NOTIFICATION_POLICY),
            options = SystemActionOption.DND_MODES
        ),

        SystemActionDef(
            id = OldSystemAction.DISABLE_DND_MODE,
            category = CATEGORY_VOLUME,
            minApi = Build.VERSION_CODES.M,
            iconRes = R.drawable.dnd_circle_off_outline,
            descriptionRes = R.string.action_disable_dnd_mode,
            permissions = arrayOf(Manifest.permission.ACCESS_NOTIFICATION_POLICY)
        ),
        //VOLUME

        //SCREEN ORIENTATION
        SystemActionDef(
            id = TOGGLE_AUTO_ROTATE,
            category = CATEGORY_SCREEN_ROTATION,
            permissions = arrayOf(Manifest.permission.WRITE_SETTINGS),
            iconRes = R.drawable.ic_outline_screen_rotation_24,
            descriptionRes = R.string.action_toggle_auto_rotate
        ),
        SystemActionDef(
            id = ENABLE_AUTO_ROTATE,
            category = CATEGORY_SCREEN_ROTATION,
            permissions = arrayOf(Manifest.permission.WRITE_SETTINGS),
            iconRes = R.drawable.ic_outline_screen_rotation_24,
            descriptionRes = R.string.action_enable_auto_rotate
        ),
        SystemActionDef(
            id = DISABLE_AUTO_ROTATE,
            category = CATEGORY_SCREEN_ROTATION,
            permissions = arrayOf(Manifest.permission.WRITE_SETTINGS),
            iconRes = R.drawable.ic_outline_screen_lock_rotation_24,
            descriptionRes = R.string.action_disable_auto_rotate
        ),
        SystemActionDef(
            id = PORTRAIT_MODE,
            category = CATEGORY_SCREEN_ROTATION,
            permissions = arrayOf(Manifest.permission.WRITE_SETTINGS),
            iconRes = R.drawable.ic_outline_stay_current_portrait_24,
            descriptionRes = R.string.action_portrait_mode
        ),
        SystemActionDef(
            id = LANDSCAPE_MODE,
            category = CATEGORY_SCREEN_ROTATION,
            permissions = arrayOf(Manifest.permission.WRITE_SETTINGS),
            iconRes = R.drawable.ic_outline_stay_current_landscape_24,
            descriptionRes = R.string.action_landscape_mode
        ),
        SystemActionDef(
            id = SWITCH_ORIENTATION,
            category = CATEGORY_SCREEN_ROTATION,
            permissions = arrayOf(Manifest.permission.WRITE_SETTINGS),
            iconRes = R.drawable.ic_outline_screen_rotation_24,
            descriptionRes = R.string.action_switch_orientation
        ),
        SystemActionDef(
            id = CYCLE_ROTATIONS,
            category = CATEGORY_SCREEN_ROTATION,
            permissions = arrayOf(Manifest.permission.WRITE_SETTINGS),
            iconRes = R.drawable.ic_outline_screen_rotation_24,
            descriptionRes = R.string.action_cycle_rotations,
            descriptionFormattedRes = R.string.action_cycle_rotations_formatted,
            optionType = OptionType.MULTIPLE,
            options = SystemActionOption.ROTATIONS
        ),
        //SCREEN ORIENTATION

        //BRIGHTNESS
        SystemActionDef(
            id = TOGGLE_AUTO_BRIGHTNESS,
            category = CATEGORY_BRIGHTNESS,
            iconRes = R.drawable.ic_outline_brightness_auto_24,
            descriptionRes = R.string.action_toggle_auto_brightness,
            permissions = arrayOf(Manifest.permission.WRITE_SETTINGS)
        ),
        SystemActionDef(
            id = ENABLE_AUTO_BRIGHTNESS,
            category = CATEGORY_BRIGHTNESS,
            iconRes = R.drawable.ic_outline_brightness_auto_24,
            descriptionRes = R.string.action_enable_auto_brightness,
            permissions = arrayOf(Manifest.permission.WRITE_SETTINGS)
        ),
        SystemActionDef(
            id = DISABLE_AUTO_BRIGHTNESS,
            category = CATEGORY_BRIGHTNESS,
            iconRes = R.drawable.ic_disable_brightness_auto_24dp,
            descriptionRes = R.string.action_disable_auto_brightness,
            permissions = arrayOf(Manifest.permission.WRITE_SETTINGS)
        ),
        SystemActionDef(
            id = INCREASE_BRIGHTNESS,
            category = CATEGORY_BRIGHTNESS,
            iconRes = R.drawable.ic_outline_brightness_high_24,
            descriptionRes = R.string.action_increase_brightness,
            permissions = arrayOf(Manifest.permission.WRITE_SETTINGS)
        ),
        SystemActionDef(
            id = DECREASE_BRIGHTNESS,
            category = CATEGORY_BRIGHTNESS,
            iconRes = R.drawable.ic_outline_brightness_low_24,
            descriptionRes = R.string.action_decrease_brightness,
            permissions = arrayOf(Manifest.permission.WRITE_SETTINGS)
        ),

        //FLASHLIGHT
        SystemActionDef(
            id = OldSystemAction.TOGGLE_FLASHLIGHT,
            category = CATEGORY_FLASHLIGHT,
            permissions = arrayOf(Manifest.permission.CAMERA),
            features = arrayOf(PackageManager.FEATURE_CAMERA_FLASH),
            minApi = Build.VERSION_CODES.M,
            iconRes = R.drawable.ic_flashlight,
            descriptionRes = R.string.action_toggle_flashlight,
            descriptionFormattedRes = R.string.action_toggle_flashlight_formatted,
            options = SystemActionOption.LENSES
        ),
        SystemActionDef(
            id = OldSystemAction.ENABLE_FLASHLIGHT,
            category = CATEGORY_FLASHLIGHT,
            permissions = arrayOf(Manifest.permission.CAMERA),
            features = arrayOf(PackageManager.FEATURE_CAMERA_FLASH),
            minApi = Build.VERSION_CODES.M,
            iconRes = R.drawable.ic_flashlight,
            descriptionRes = R.string.action_enable_flashlight,
            descriptionFormattedRes = R.string.action_enable_flashlight_formatted,
            options = SystemActionOption.LENSES
        ),
        SystemActionDef(
            id = OldSystemAction.DISABLE_FLASHLIGHT,
            category = CATEGORY_FLASHLIGHT,
            permissions = arrayOf(Manifest.permission.CAMERA),
            features = arrayOf(PackageManager.FEATURE_CAMERA_FLASH),
            minApi = Build.VERSION_CODES.M,
            iconRes = R.drawable.ic_flashlight_off,
            descriptionRes = R.string.action_disable_flashlight,
            descriptionFormattedRes = R.string.action_disable_flashlight_formatted,
            options = SystemActionOption.LENSES
        ),

        //NFC
        SystemActionDef(
            id = ENABLE_NFC,
            category = CATEGORY_NFC,
            iconRes = R.drawable.ic_outline_nfc_24,
            permissions = arrayOf(Constants.PERMISSION_ROOT),
            features = arrayOf(PackageManager.FEATURE_NFC),
            descriptionRes = R.string.action_nfc_enable
        ),
        SystemActionDef(
            id = DISABLE_NFC,
            category = CATEGORY_NFC,
            features = arrayOf(PackageManager.FEATURE_NFC),
            iconRes = R.drawable.ic_nfc_off,
            permissions = arrayOf(Constants.PERMISSION_ROOT),
            descriptionRes = R.string.action_nfc_disable
        ),
        SystemActionDef(
            id = TOGGLE_NFC,
            category = CATEGORY_NFC,
            features = arrayOf(PackageManager.FEATURE_NFC),
            iconRes = R.drawable.ic_outline_nfc_24,
            permissions = arrayOf(Constants.PERMISSION_ROOT),
            descriptionRes = R.string.action_nfc_toggle
        ),

        //KEYBOARD
        SystemActionDef(
            id = MOVE_CURSOR_TO_END,
            category = CATEGORY_KEYBOARD,
            iconRes = R.drawable.ic_cursor,
            messageOnSelection = R.string.action_move_to_end_of_text_message,
            descriptionRes = R.string.action_move_to_end_of_text
        ),

        SystemActionDef(
            id = TOGGLE_KEYBOARD,
            category = CATEGORY_KEYBOARD,
            minApi = Build.VERSION_CODES.N,
            iconRes = R.drawable.ic_notification_keyboard,
            messageOnSelection = R.string.action_toggle_keyboard_message,
            descriptionRes = R.string.action_toggle_keyboard
        ),

        SystemActionDef(
            id = SHOW_KEYBOARD,
            category = CATEGORY_KEYBOARD,
            minApi = Build.VERSION_CODES.N,
            iconRes = R.drawable.ic_notification_keyboard,
            messageOnSelection = R.string.action_toggle_keyboard_message,
            descriptionRes = R.string.action_show_keyboard
        ),

        SystemActionDef(
            id = HIDE_KEYBOARD,
            category = CATEGORY_KEYBOARD,
            minApi = Build.VERSION_CODES.N,
            iconRes = R.drawable.ic_outline_keyboard_hide_24,
            messageOnSelection = R.string.action_toggle_keyboard_message,
            descriptionRes = R.string.action_hide_keyboard
        ),

        SystemActionDef(
            id = SHOW_KEYBOARD_PICKER,
            category = CATEGORY_KEYBOARD,
            iconRes = R.drawable.ic_notification_keyboard,
            maxApi = Build.VERSION_CODES.O,
            descriptionRes = R.string.action_show_keyboard_picker
        ),

        SystemActionDef(
            id = SHOW_KEYBOARD_PICKER_ROOT,
            category = CATEGORY_KEYBOARD,
            iconRes = R.drawable.ic_notification_keyboard,
            permissions = arrayOf(Constants.PERMISSION_ROOT),
            minApi = Build.VERSION_CODES.O_MR1,
            maxApi = Build.VERSION_CODES.P,
            descriptionRes = R.string.action_show_keyboard_picker_root
        ),

        SystemActionDef(id = SWITCH_KEYBOARD,
            category = CATEGORY_KEYBOARD,
            iconRes = R.drawable.ic_notification_keyboard,
            permissions = arrayOf(Manifest.permission.WRITE_SECURE_SETTINGS),
            descriptionRes = R.string.action_switch_keyboard,
            descriptionFormattedRes = R.string.action_switch_keyboard_formatted,
            getOptions = {
                KeyboardUtils.getInputMethodIds()
            }
        ),

        SystemActionDef(
            id = TEXT_CUT,
            category = CATEGORY_KEYBOARD,
            iconRes = R.drawable.ic_content_cut,
            descriptionRes = R.string.action_text_cut,
            minApi = Build.VERSION_CODES.JELLY_BEAN_MR2
        ),

        SystemActionDef(
            id = TEXT_COPY,
            category = CATEGORY_KEYBOARD,
            iconRes = R.drawable.ic_content_copy,
            descriptionRes = R.string.action_text_copy,
            minApi = Build.VERSION_CODES.JELLY_BEAN_MR2
        ),

        SystemActionDef(
            id = TEXT_PASTE,
            category = CATEGORY_KEYBOARD,
            iconRes = R.drawable.ic_content_paste,
            descriptionRes = R.string.action_text_paste,
            minApi = Build.VERSION_CODES.JELLY_BEAN_MR2
        ),

        SystemActionDef(
            id = SELECT_WORD_AT_CURSOR,
            category = CATEGORY_KEYBOARD,
            descriptionRes = R.string.action_select_word_at_cursor,
            minApi = Build.VERSION_CODES.JELLY_BEAN_MR2
        ),

        //AIRPLANE MODE
        SystemActionDef(
            id = TOGGLE_AIRPLANE_MODE,
            category = CATEGORY_AIRPLANE_MODE,
            iconRes = R.drawable.ic_outline_airplanemode_active_24,
            descriptionRes = R.string.action_toggle_airplane_mode,
            permissions = arrayOf(Constants.PERMISSION_ROOT)
        ),
        SystemActionDef(
            id = ENABLE_AIRPLANE_MODE,
            category = CATEGORY_AIRPLANE_MODE,
            iconRes = R.drawable.ic_outline_airplanemode_active_24,
            descriptionRes = R.string.action_enable_airplane_mode,
            permissions = arrayOf(Constants.PERMISSION_ROOT)
        ),
        SystemActionDef(
            id = DISABLE_AIRPLANE_MODE,
            category = CATEGORY_AIRPLANE_MODE,
            iconRes = R.drawable.ic_outline_airplanemode_inactive_24,
            descriptionRes = R.string.action_disable_airplane_mode,
            permissions = arrayOf(Constants.PERMISSION_ROOT)
        ),

        //OTHER
        SystemActionDef(
            id = SCREENSHOT,
            category = CATEGORY_OTHER,
            minApi = Build.VERSION_CODES.P,
            iconRes = R.drawable.ic_outline_fullscreen_24,
            descriptionRes = R.string.action_screenshot
        ),

        SystemActionDef(
            id = SCREENSHOT_ROOT,
            category = CATEGORY_OTHER,
            maxApi = Build.VERSION_CODES.O_MR1,
            iconRes = R.drawable.ic_outline_fullscreen_24,
            descriptionRes = R.string.action_screenshot_root,
            permissions = arrayOf(Constants.PERMISSION_ROOT)
        ),

        SystemActionDef(
            id = OPEN_VOICE_ASSISTANT,
            category = CATEGORY_OTHER,
            iconRes = R.drawable.ic_outline_assistant_24,
            descriptionRes = R.string.action_open_assistant
        ),
        SystemActionDef(
            id = OPEN_DEVICE_ASSISTANT,
            category = CATEGORY_OTHER,
            iconRes = R.drawable.ic_outline_assistant_24,
            descriptionRes = R.string.action_open_device_assistant
        ),
        SystemActionDef(
            id = OPEN_CAMERA,
            category = CATEGORY_OTHER,
            iconRes = R.drawable.ic_outline_camera_alt_24,
            descriptionRes = R.string.action_open_camera
        ),
        SystemActionDef(
            id = LOCK_DEVICE,
            category = CATEGORY_OTHER,
            iconRes = R.drawable.ic_outline_lock_24,
            descriptionRes = R.string.action_lock_device,
            minApi = Build.VERSION_CODES.P
        ),
        SystemActionDef(
            id = LOCK_DEVICE_ROOT,
            category = CATEGORY_OTHER,
            iconRes = R.drawable.ic_outline_lock_24,
            descriptionRes = R.string.action_lock_device_root,
            maxApi = Build.VERSION_CODES.O_MR1,
            permissions = arrayOf(Constants.PERMISSION_ROOT)
        ),
        SystemActionDef(
            id = SECURE_LOCK_DEVICE,
            category = CATEGORY_OTHER,
            iconRes = R.drawable.ic_outline_lock_24,
            descriptionRes = R.string.action_secure_lock_device,
            features = arrayOf(PackageManager.FEATURE_DEVICE_ADMIN),
            permissions = arrayOf(Manifest.permission.BIND_DEVICE_ADMIN),
            messageOnSelection = R.string.action_secure_lock_device_message
        ),
        SystemActionDef(
            id = POWER_ON_OFF_DEVICE,
            category = CATEGORY_OTHER,
            iconRes = R.drawable.ic_outline_power_settings_new_24,
            descriptionRes = R.string.action_power_on_off_device,
            permissions = arrayOf(Constants.PERMISSION_ROOT),
            messageOnSelection = R.string.action_power_on_off_device_message
        ),
        SystemActionDef(
            id = CONSUME_KEY_EVENT,
            category = CATEGORY_OTHER,
            descriptionRes = R.string.action_consume_keyevent
        ),
        SystemActionDef(
            id = OPEN_SETTINGS,
            category = CATEGORY_OTHER,
            descriptionRes = R.string.action_open_settings,
            iconRes = R.drawable.ic_outline_settings_applications_24
        ),
        SystemActionDef(
            id = SHOW_POWER_MENU,
            category = CATEGORY_OTHER,
            descriptionRes = R.string.action_show_power_menu,
            iconRes = R.drawable.ic_outline_power_settings_new_24,
            minApi = Build.VERSION_CODES.LOLLIPOP
        )
    )

    /**
     * Get all the system actions which are supported by the system.
     */
    fun getSupportedSystemActions(ctx: Context) =
        SYSTEM_ACTION_DEFINITIONS
            .filter { it.isSupported(ctx) is Success }

    fun getUnsupportedSystemActionsWithReasons(ctx: Context): Map<SystemActionDef, Error> =
        SYSTEM_ACTION_DEFINITIONS
            .filter { it.isSupported(ctx) is Error }
            .map { it to (it.isSupported(ctx) as Error) }
            .toMap()

    /**
     * @return null if the action is supported.
     */
    private fun SystemActionDef.isSupported(ctx: Context): Result<SystemActionDef> {
        if (Build.VERSION.SDK_INT < minApi) {
            return Error.SdkVersionTooLow(minApi)
        }

        for (feature in features) {
            if (!ctx.packageManager.hasSystemFeature(feature)) {
                return Error.FeatureUnavailable(feature)
            }
        }

        if (Build.VERSION.SDK_INT > maxApi) {
            return Error.SdkVersionTooHigh(maxApi)
        }

        return Success(this)
    }

    fun getSystemActionDef(id: String): Result<SystemActionDef> {
        val systemActionDef = SYSTEM_ACTION_DEFINITIONS.find { it.id == id }
            ?: return Error.SystemActionNotFound(id)

        return Success(systemActionDef)
    }

    fun SystemActionDef.getDescriptionWithOption(ctx: Context, optionText: String): String {
        descriptionFormattedRes
            ?: throw Exception("System action $id has options and doesn't have a formatted description")

        return ctx.str(descriptionFormattedRes, optionText)
    }

    fun SystemActionDef.getDescriptionWithOptionSet(
        ctx: Context,
        optionSetLabels: Set<String>
    ): String {
        descriptionFormattedRes
            ?: throw Exception("System action $id has options and doesn't have a formatted description")

        return ctx.str(descriptionFormattedRes, optionSetLabels.joinToString())
    }

    private suspend fun getPackagesSortedByName(ctx: Context) =
        ServiceLocator.packageRepository(ctx).let { repository ->
            repository.getLaunchableAppList()
                .sortedBy { repository.getAppName(it).toLowerCase(Locale.getDefault()) }
                .map { it.packageName }
                .let { Success(it) }
        }
}