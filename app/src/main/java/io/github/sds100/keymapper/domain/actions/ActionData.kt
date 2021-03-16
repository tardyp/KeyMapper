package io.github.sds100.keymapper.domain.actions

import io.github.sds100.keymapper.domain.devices.DeviceInfo
import io.github.sds100.keymapper.domain.utils.*
import io.github.sds100.keymapper.util.IntentTarget
import kotlinx.serialization.Serializable

@Serializable
sealed class ActionData

@Serializable
object CorruptAction : ActionData()

@Serializable
data class OpenAppAction(
    val packageName: String
) : ActionData()

@Serializable
data class OpenAppShortcutAction(
    val packageName: String,
    val shortcutTitle: String,
) : ActionData()

@Serializable
data class KeyEventAction(
    val keyCode: Int,
    val metaState: Int,
    val useShell: Boolean,
    val device: DeviceInfo?
) : ActionData()

sealed class SystemAction : ActionData() {
    abstract val id: SystemActionId
}

@Serializable
data class SimpleSystemAction(
    override val id: SystemActionId,
) : SystemAction()

@Serializable
data class VolumeSystemAction(
    val showVolumeUi: Boolean,
    override val id: SystemActionId
) : SystemAction()

@Serializable
data class ChangeVolumeStreamSystemAction(
    override val id: SystemActionId,
    val showVolumeUi: Boolean,
    val streamType: StreamType
) : SystemAction()

@Serializable
data class FlashlightSystemAction(
    override val id: SystemActionId,
    val lens: CameraLens
) : SystemAction()

@Serializable
data class ChangeRingerModeSystemAction(
    val ringerMode: RingerMode
) : SystemAction() {
    override val id: SystemActionId = SystemActionId.CHANGE_RINGER_MODE
}

@Serializable
data class SwitchKeyboardSystemAction(
    val imeId: String,
    val savedImeName: String
) : SystemAction() {
    override val id = SystemActionId.SWITCH_KEYBOARD
}

@Serializable
class ChangeDndModeSystemAction(
    override val id: SystemActionId,
    val dndMode: DndMode
) : SystemAction()

@Serializable
data class CycleRotationsSystemAction(
    val orientations: List<Orientation>
) : SystemAction() {
    override val id = SystemActionId.CYCLE_ROTATIONS
}

@Serializable
data class ControlMediaForAppSystemAction(
    override val id: SystemActionId,
    val packageName: String
) : SystemAction()

@Serializable
data class IntentAction(
    val description: String,
    val target: IntentTarget,
    val uri: String
) : ActionData()

@Serializable
data class TapCoordinateAction(
    val x: Int,
    val y: Int,
    val description: String?
) : ActionData()

@Serializable
data class PhoneCallAction(
    val number: String
) : ActionData()