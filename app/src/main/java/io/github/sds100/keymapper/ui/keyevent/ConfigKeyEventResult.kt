package io.github.sds100.keymapper.ui.keyevent

import io.github.sds100.keymapper.domain.devices.InputDeviceInfo
import kotlinx.serialization.Serializable

@Serializable
data class ConfigKeyEventResult(
    val keyCode: Int,
    val metaState: Int,
    val useShell: Boolean,
    val device: InputDeviceInfo?
)