package io.github.sds100.keymapper.ui.actions

import android.view.KeyEvent
import io.github.sds100.keymapper.R
import io.github.sds100.keymapper.domain.actions.*
import io.github.sds100.keymapper.domain.adapter.InputMethodAdapter
import io.github.sds100.keymapper.domain.utils.*
import io.github.sds100.keymapper.framework.adapters.AppInfoAdapter
import io.github.sds100.keymapper.framework.adapters.ResourceProvider
import io.github.sds100.keymapper.ui.IconInfo
import io.github.sds100.keymapper.util.*
import io.github.sds100.keymapper.util.result.*
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import splitties.bitflags.hasFlag

/**
 * Created by sds100 on 18/03/2021.
 */

abstract class BaseActionUiHelper<A>(
    private val appInfoAdapter: AppInfoAdapter,
    private val inputMethodAdapter: InputMethodAdapter,
    resourceProvider: ResourceProvider
) : ActionUiHelper<A>, ResourceProvider by resourceProvider {

    override fun getTitle(action: ActionData): Result<String> = when (action) {
        is OpenAppAction ->
            appInfoAdapter.getAppName(action.packageName).map { appName ->
                Success(getString(R.string.description_open_app, appName))
            }.firstBlocking()

        is OpenAppShortcutAction -> Success(action.shortcutTitle)

        is KeyEventAction -> {
            val key = if (action.keyCode > KeyEvent.getMaxKeyCode()) {
                "Key Code ${action.keyCode}"
            } else {
                KeyEvent.keyCodeToString(action.keyCode)
            }

            val metaStateString = buildString {

                KeyEventUtils.MODIFIER_LABELS.entries.forEach {
                    val modifier = it.key
                    val labelRes = it.value

                    if (action.metaState.hasFlag(modifier)) {
                        append("${getString(labelRes)} + ")
                    }
                }
            }

            val title = if (action.device != null) {

                val strRes = if (action.useShell) {
                    R.string.description_keyevent_from_device_through_shell
                } else {
                    R.string.description_keyevent_from_device
                }

                getString(
                    strRes,
                    arrayOf(metaStateString, key, action.device.name)
                )
            } else {
                val strRes = if (action.useShell) {
                    R.string.description_keyevent_through_shell
                } else {
                    R.string.description_keyevent
                }

                getString(strRes, arrayOf(metaStateString, key))
            }

            Success(title)
        }

        is SimpleSystemAction -> Success(getString(SystemActionUtils.getTitle(action)))

        is ChangeDndModeSystemAction -> {
            val dndModeString = when (action.dndMode) {
                DndMode.ALARMS -> getString(R.string.dnd_mode_alarms)
                DndMode.PRIORITY -> getString(R.string.dnd_mode_priority)
                DndMode.NONE -> getString(R.string.dnd_mode_none)
            }

            when (action) {
                is ChangeDndModeSystemAction.Toggle -> getString(
                    R.string.action_toggle_dnd_mode_formatted,
                    dndModeString
                )
                is ChangeDndModeSystemAction.Disable -> getString(
                    R.string.action_enable_dnd_mode_formatted,
                    dndModeString
                )
                is ChangeDndModeSystemAction.Enable -> getString(
                    R.string.action_enable_dnd_mode_formatted,
                    dndModeString
                )
            }.success()
        }

        is ChangeRingerModeSystemAction -> {
            val ringerModeString = when (action.ringerMode) {
                RingerMode.NORMAL -> getString(R.string.ringer_mode_normal)
                RingerMode.VIBRATE -> getString(R.string.ringer_mode_vibrate)
                RingerMode.SILENT -> getString(R.string.ringer_mode_silent)
            }

            getString(R.string.action_change_ringer_mode_formatted, ringerModeString).success()
        }

        is VolumeSystemAction -> {
            val string = when (action) {
                is VolumeSystemAction.Stream -> {
                    val streamString = when (action.volumeStream) {
                        VolumeStream.ALARM -> getString(R.string.stream_alarm)
                        VolumeStream.DTMF -> getString(R.string.stream_dtmf)
                        VolumeStream.MUSIC -> getString(R.string.stream_music)
                        VolumeStream.NOTIFICATION -> getString(R.string.stream_notification)
                        VolumeStream.RING -> getString(R.string.stream_ring)
                        VolumeStream.SYSTEM -> getString(R.string.stream_system)
                        VolumeStream.VOICE_CALL -> getString(R.string.stream_voice_call)
                        VolumeStream.ACCESSIBILITY -> getString(R.string.stream_accessibility)
                    }

                    when (action) {
                        is VolumeSystemAction.Stream.Decrease -> getString(
                            R.string.action_decrease_stream_formatted,
                            streamString
                        )

                        is VolumeSystemAction.Stream.Increase -> getString(
                            R.string.action_increase_stream_formatted,
                            streamString
                        )
                    }
                }

                is VolumeSystemAction.Down -> getString(R.string.action_volume_down)
                is VolumeSystemAction.Mute -> getString(R.string.action_volume_mute)
                is VolumeSystemAction.ToggleMute -> getString(R.string.action_toggle_mute)
                is VolumeSystemAction.UnMute -> getString(R.string.action_volume_unmute)
                is VolumeSystemAction.Up -> getString(R.string.action_volume_up)
            }

            if (action.showVolumeUi) {
                val midDot = getString(R.string.middot)
                "$string $midDot ${getString(R.string.flag_show_volume_dialog)}"
            } else {
                string
            }.success()
        }

        is ControlMediaForAppSystemAction ->
            appInfoAdapter.getAppName(action.packageName).map { appName ->
                val resId = when (action) {
                    is ControlMediaForAppSystemAction.Play -> R.string.action_play_media_package_formatted
                    is ControlMediaForAppSystemAction.FastForward -> R.string.action_fast_forward_package_formatted
                    is ControlMediaForAppSystemAction.NextTrack -> R.string.action_next_track_package_formatted
                    is ControlMediaForAppSystemAction.Pause -> R.string.action_pause_media_package_formatted
                    is ControlMediaForAppSystemAction.PlayPause -> R.string.action_play_pause_media_package_formatted
                    is ControlMediaForAppSystemAction.PreviousTrack -> R.string.action_previous_track_package_formatted
                    is ControlMediaForAppSystemAction.Rewind -> R.string.action_rewind_package_formatted
                }

                getString(resId, appName).success()

            }.catch {
                val resId = when (action) {
                    is ControlMediaForAppSystemAction.Play -> R.string.action_play_media_package
                    is ControlMediaForAppSystemAction.FastForward -> R.string.action_fast_forward_package
                    is ControlMediaForAppSystemAction.NextTrack -> R.string.action_next_track_package
                    is ControlMediaForAppSystemAction.Pause -> R.string.action_pause_media_package
                    is ControlMediaForAppSystemAction.PlayPause -> R.string.action_play_pause_media_package
                    is ControlMediaForAppSystemAction.PreviousTrack -> R.string.action_previous_track_package
                    is ControlMediaForAppSystemAction.Rewind -> R.string.action_rewind_package
                }

                getString(resId).success()
            }.firstBlocking()

        is CycleRotationsSystemAction -> {
            val orientationStrings = action.orientations.map {
                when (it) {
                    Orientation.ORIENTATION_0 -> getString(R.string.orientation_0)
                    Orientation.ORIENTATION_90 -> getString(R.string.orientation_90)
                    Orientation.ORIENTATION_180 -> getString(R.string.orientation_180)
                    Orientation.ORIENTATION_270 -> getString(R.string.orientation_270)
                }
            }

            getString(
                R.string.action_cycle_rotations_formatted,
                orientationStrings.joinToString()
            ).success()
        }

        is FlashlightSystemAction -> {
            val resId = when (action) {
                is FlashlightSystemAction.Toggle -> R.string.action_toggle_flashlight_formatted
                is FlashlightSystemAction.Enable -> R.string.action_enable_flashlight_formatted
                is FlashlightSystemAction.Disable -> R.string.action_disable_flashlight_formatted
            }

            val lensString = when (action.lens) {
                CameraLens.FRONT -> getString(R.string.lens_front)
                CameraLens.BACK -> getString(R.string.lens_back)
            }

            getString(resId, lensString).success()
        }

        is SwitchKeyboardSystemAction -> inputMethodAdapter.getLabel(action.imeId).then {
            getString(R.string.action_switch_keyboard_formatted, it).success()
        }.otherwise {
            getString(R.string.action_switch_keyboard_formatted, action.savedImeName).success()
        }

        is CorruptAction -> Error.CorruptActionError
        is IntentAction -> {
            val resId = when (action.target) {
                IntentTarget.ACTIVITY -> R.string.action_title_intent_start_activity
                IntentTarget.BROADCAST_RECEIVER -> R.string.action_title_intent_send_broadcast
                IntentTarget.SERVICE -> R.string.action_title_intent_start_service
            }

            getString(resId, action.description).success()
        }

        is PhoneCallAction -> getString(R.string.action_type_phone_call, action.number).success()

        is TapCoordinateAction -> if (action.description != null) {
            getString(
                R.string.description_tap_coordinate_with_description,
                arrayOf(action.x, action.y, action.description)
            ).success()
        } else {
            getString(
                R.string.description_tap_coordinate_default,
                arrayOf(action.x, action.y)
            ).success()
        }

        is TextAction -> getString(R.string.description_text_block, action.text).success()
        is UrlAction -> getString(R.string.description_url, action.url).success()
    }

    override fun getIcon(action: ActionData): Result<IconInfo> = when (action) {
        CorruptAction -> Error.CorruptActionError

        is KeyEventAction -> IconInfo(
            drawable = null,
            tintType = TintType.NONE
        ).success()

        is OpenAppAction -> appInfoAdapter.getAppIcon(action.packageName).map {
            IconInfo(it, TintType.NONE).success()
        }.firstBlocking()

        is OpenAppShortcutAction -> appInfoAdapter.getAppIcon(action.packageName).map {
            IconInfo(it, TintType.NONE).success()
        }.firstBlocking()

        is SystemAction -> IconInfo(
            SystemActionUtils.getIcon(action)?.let { getDrawable(it) },
            TintType.ON_SURFACE
        ).success()

        is IntentAction -> IconInfo(
            drawable = null,
            tintType = TintType.NONE
        ).success()

        is PhoneCallAction -> IconInfo(
            getDrawable(R.drawable.ic_outline_call_24),
            tintType = TintType.ON_SURFACE
        ).success()

        is TapCoordinateAction -> IconInfo(
            getDrawable(R.drawable.ic_outline_touch_app_24),
            TintType.ON_SURFACE
        ).success()

        is TextAction -> IconInfo(null, TintType.NONE).success()
        is UrlAction -> IconInfo(null, TintType.NONE).success()
    }
}

interface ActionUiHelper<A> {
    fun getTitle(action: ActionData): Result<String>
    fun getOptionLabels(action: A): List<String>
    fun getIcon(action: ActionData): Result<IconInfo>
}