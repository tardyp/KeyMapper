package io.github.sds100.keymapper.ui.actions

import android.graphics.drawable.Drawable
import android.view.KeyEvent
import io.github.sds100.keymapper.R
import io.github.sds100.keymapper.domain.actions.*
import io.github.sds100.keymapper.domain.adapter.InputMethodAdapter
import io.github.sds100.keymapper.domain.models.Defaultable
import io.github.sds100.keymapper.domain.utils.*
import io.github.sds100.keymapper.framework.adapters.AppInfoAdapter
import io.github.sds100.keymapper.framework.adapters.ResourceProvider
import io.github.sds100.keymapper.util.*
import io.github.sds100.keymapper.util.result.*
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import splitties.bitflags.hasFlag

/**
 * Created by sds100 on 22/02/2021.
 */

abstract class BaseActionListItemMapper<A : Action>(
    private val getActionError: GetActionErrorUseCase,
    private val appInfoAdapter: AppInfoAdapter,
    private val inputMethodAdapter: InputMethodAdapter,
    resourceProvider: ResourceProvider
) : ResourceProvider by resourceProvider, ActionListItemMapper<A> {

    override fun map(
        action: A,
        canBePerformedError: Error?,
        actionCount: Int
    ): ActionListItemState {
        var title: String? = null
        var icon: ActionIconInfo? = null

        val error: Error? = getTitle(action.data)
            .onSuccess {
                if (action.multiplier.isAllowed && action.multiplier.value is Defaultable.Custom) {
                    val multiplier = (action.multiplier.value as Defaultable.Custom<Int>).data
                    title = "${multiplier}x $it"
                } else {
                    title = it
                }
            }
            .then { getIcon(action.data) }.onSuccess { icon = it }
            .errorOrNull() ?: canBePerformedError

        val extraInfo = buildString {
            val midDot = getString(R.string.middot)

            getOptionLabels(action).forEachIndexed { index, label ->
                if (index != 0) {
                    append(" $midDot ")
                }

                append(label)

                action.delayBeforeNextAction.apply {
                    if (isAllowed && value is Defaultable.Custom) {
                        if (this@buildString.isNotBlank()) {
                            append(" $midDot ")
                        }

                        append(getString(R.string.action_title_wait, value.data))
                    }
                }
            }

        }.takeIf { it.isNotBlank() }

        return ActionListItemState(
            id = action.uid,
            tintType = icon?.tintType ?: TintType.ERROR,
            icon = icon?.drawable ?: getDrawable(R.drawable.ic_baseline_error_outline_24),
            title = title,
            extraInfo = extraInfo,
            errorMessage = error?.getFullMessage(this),
            dragAndDrop = actionCount > 1
        )
    }

    private fun getTitle(action: ActionData): Result<String> = when (action) {
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

        is VolumeSystemAction -> {
            val resId = SystemActionUtils.getTitle(action)

            if (action.showVolumeUi) {

                val midDot = getString(R.string.middot)
                "${getString(resId)} $midDot ${getString(R.string.flag_show_volume_dialog)}".success()
            } else {
                getString(resId).success()
            }
        }

        is ChangeDndModeSystemAction -> {
            val dndModeString = when (action.dndMode) {
                DndMode.ALARMS -> getString(R.string.dnd_mode_alarms)
                DndMode.PRIORITY -> getString(R.string.dnd_mode_priority)
                DndMode.NONE -> getString(R.string.dnd_mode_none)
            }

            when (action.id) {
                SystemActionId.TOGGLE_DND_MODE -> getString(
                    R.string.action_toggle_dnd_mode_formatted,
                    dndModeString
                )

                SystemActionId.ENABLE_DND_MODE -> getString(
                    R.string.action_enable_dnd_mode_formatted,
                    dndModeString
                )

                SystemActionId.DISABLE_DND_MODE -> getString(
                    R.string.action_disable_dnd_mode
                )

                else -> throw Exception("don't know how to create action title for this ${action.id}")
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

        is ChangeVolumeStreamSystemAction -> {
            val streamString = when (action.streamType) {
                StreamType.ALARM -> getString(R.string.stream_alarm)
                StreamType.DTMF -> getString(R.string.stream_dtmf)
                StreamType.MUSIC -> getString(R.string.stream_music)
                StreamType.NOTIFICATION -> getString(R.string.stream_notification)
                StreamType.RING -> getString(R.string.stream_ring)
                StreamType.SYSTEM -> getString(R.string.stream_system)
                StreamType.VOICE_CALL -> getString(R.string.stream_voice_call)
                StreamType.ACCESSIBILITY -> getString(R.string.stream_accessibility)
            }

            when (action.id) {
                SystemActionId.VOLUME_DECREASE_STREAM -> getString(
                    R.string.action_decrease_stream_formatted,
                    streamString
                )
                SystemActionId.VOLUME_INCREASE_STREAM -> getString(
                    R.string.action_increase_stream_formatted,
                    streamString
                )

                else -> throw Exception("don't know how to create action title for this ${action.id}")
            }.success()
        }

        is ControlMediaForAppSystemAction ->
            appInfoAdapter.getAppName(action.packageName).map { appName ->
                val resId = when (action.id) {
                    SystemActionId.PLAY_MEDIA_PACKAGE -> R.string.action_play_media_package_formatted
                    SystemActionId.PLAY_PAUSE_MEDIA_PACKAGE -> R.string.action_play_pause_media_package_formatted
                    SystemActionId.PAUSE_MEDIA_PACKAGE -> R.string.action_pause_media_package_formatted
                    SystemActionId.NEXT_TRACK_PACKAGE -> R.string.action_next_track_package_formatted
                    SystemActionId.PREVIOUS_TRACK_PACKAGE -> R.string.action_previous_track_package_formatted
                    SystemActionId.FAST_FORWARD_PACKAGE -> R.string.action_fast_forward_package_formatted
                    SystemActionId.REWIND_PACKAGE -> R.string.action_rewind_package_formatted


                    else -> throw Exception("don't know how to create action title for this ${action.id}")
                }

                getString(resId, appName).success()

            }.catch {
                val resId = when (action.id) {
                    SystemActionId.PLAY_MEDIA_PACKAGE -> R.string.action_play_media_package
                    SystemActionId.PLAY_PAUSE_MEDIA_PACKAGE -> R.string.action_play_pause_media_package
                    SystemActionId.PAUSE_MEDIA_PACKAGE -> R.string.action_pause_media_package
                    SystemActionId.NEXT_TRACK_PACKAGE -> R.string.action_next_track_package
                    SystemActionId.PREVIOUS_TRACK_PACKAGE -> R.string.action_previous_track_package
                    SystemActionId.FAST_FORWARD_PACKAGE -> R.string.action_fast_forward_package
                    SystemActionId.REWIND_PACKAGE -> R.string.action_rewind_package

                    else -> throw Exception("don't know how to create action title for this ${action.id}")
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
            val resId = when (action.id) {
                SystemActionId.TOGGLE_FLASHLIGHT -> R.string.action_toggle_flashlight_formatted
                SystemActionId.ENABLE_FLASHLIGHT -> R.string.action_enable_flashlight_formatted
                SystemActionId.DISABLE_FLASHLIGHT -> R.string.action_disable_flashlight_formatted
                else -> throw Exception("don't know how to create action title for this ${action.id}")
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

    private fun getIcon(action: ActionData): Result<ActionIconInfo> = when (action) {
        CorruptAction -> Error.CorruptActionError

        is KeyEventAction -> ActionIconInfo(
            drawable = null,
            tintType = TintType.NONE
        ).success()

        is OpenAppAction -> appInfoAdapter.getAppIcon(action.packageName).map {
            ActionIconInfo(it, TintType.NONE).success()
        }.firstBlocking()

        is OpenAppShortcutAction -> appInfoAdapter.getAppIcon(action.packageName).map {
            ActionIconInfo(it, TintType.NONE).success()
        }.firstBlocking()

        is SystemAction -> ActionIconInfo(
            SystemActionUtils.getIcon(action)?.let { getDrawable(it) },
            TintType.ON_SURFACE
        ).success()

        is IntentAction -> ActionIconInfo(
            drawable = null,
            tintType = TintType.NONE
        ).success()

        is PhoneCallAction -> ActionIconInfo(
            getDrawable(R.drawable.ic_outline_call_24),
            tintType = TintType.ON_SURFACE
        ).success()

        is TapCoordinateAction -> ActionIconInfo(
            getDrawable(R.drawable.ic_outline_touch_app_24),
            TintType.ON_SURFACE
        ).success()

        is TextAction -> ActionIconInfo(null, TintType.NONE).success()
        is UrlAction -> ActionIconInfo(null, TintType.NONE).success()
    }

    abstract fun getOptionLabels(action: A): List<String>

    private data class ActionIconInfo(val drawable: Drawable?, val tintType: TintType)
}

interface ActionListItemMapper<A : Action> {
    fun map(action: A, canBePerformedError: Error?, actionCount: Int): ActionListItemState
}