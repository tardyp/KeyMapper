package io.github.sds100.keymapper.domain.models

import io.github.sds100.keymapper.data.model.ActionEntity
import io.github.sds100.keymapper.domain.actions.ActionWithOptions
import io.github.sds100.keymapper.domain.devices.DeviceInfo
import io.github.sds100.keymapper.domain.utils.*
import io.github.sds100.keymapper.util.result.Error

/**
 * Created by sds100 on 21/02/2021.
 */

sealed class Action(val uid: String)

class CorruptAction(uid: String, val error: Error) : Action(uid)

class OpenAppAction(
    uid: String,
    val packageName: String
) : Action(uid)

class OpenAppShortcutAction(
    uid: String,
    val packageName: String,
    val shortcutTitle: String,
) : Action(uid)

class KeyEventAction(
    uid: String,
    val keyCode: Int,
    val metaState: Int,
    val useShell: Boolean,
    val device: DeviceInfo?
) : Action(uid)

abstract class SystemAction(
    uid: String,
    val systemActionId: String,
) : Action(uid)

class SimpleSystemAction(
    uid: String,
    systemActionId: String,
) : SystemAction(uid, systemActionId)

class VolumeSystemAction(
    uid: String,
    systemActionId: String,
    val showVolumeUi: Boolean
) : SystemAction(uid, systemActionId)

class ChangeVolumeStreamSystemAction(
    uid: String,
    systemActionId: String,
    val showVolumeUi: Boolean,
    val streamType: StreamType
) : SystemAction(uid, systemActionId)

class FlashlightSystemAction(
    uid: String,
    systemActionId: String,
    val lens: CameraLens
) : SystemAction(uid, systemActionId)

class ChangeRingerModeSystemAction(
    uid: String,
    systemActionId: String,
    val ringerMode: RingerMode
) : SystemAction(uid, systemActionId)

class SwitchKeyboardSystemAction(
    uid: String,
    systemActionId: String,
    val imeId: String
) : SystemAction(uid, systemActionId)

class ChangeDndModeSystemAction(
    uid: String,
    systemActionId: String,
    val dndMode: DndMode
) : SystemAction(uid, systemActionId)

class CycleRotationsSystemAction(
    uid: String,
    systemActionId: String,
    val orientations: List<Orientation>
) : SystemAction(uid, systemActionId)

class ControlMediaForAppSystemAction(
    uid: String,
    systemActionId: String,
    val packageName: String
) : SystemAction(uid, systemActionId)