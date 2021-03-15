package io.github.sds100.keymapper.domain.actions

import io.github.sds100.keymapper.domain.devices.DeviceInfo
import io.github.sds100.keymapper.domain.utils.*
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

abstract class SystemAction : ActionData() {
    abstract val systemActionId: String
}

@Serializable
data class SimpleSystemAction(
    override val systemActionId: String,
) : SystemAction()

@Serializable
data class VolumeSystemAction(
    val showVolumeUi: Boolean,
    override val systemActionId: String
) : SystemAction()

@Serializable
data class ChangeVolumeStreamSystemAction(
    override val systemActionId: String,
    val showVolumeUi: Boolean,
    val streamType: StreamType
) : SystemAction()

@Serializable
data class FlashlightSystemAction(
    override val systemActionId: String,
    val lens: CameraLens
) : SystemAction()

@Serializable
data class ChangeRingerModeSystemAction(
    override val systemActionId: String,
    val ringerMode: RingerMode
) : SystemAction()

@Serializable
data class SwitchKeyboardSystemAction(
    override val systemActionId: String,
    val imeId: String
) : SystemAction()

@Serializable
class ChangeDndModeSystemAction(
    override val systemActionId: String,
    val dndMode: DndMode
) : SystemAction()

@Serializable
data class CycleRotationsSystemAction(
    override val systemActionId: String,
    val orientations: List<Orientation>
) : SystemAction()

@Serializable
data class ControlMediaForAppSystemAction(
    override val systemActionId: String,
    val packageName: String
) : SystemAction()