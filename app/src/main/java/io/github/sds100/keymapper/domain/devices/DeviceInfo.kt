package io.github.sds100.keymapper.domain.devices

import kotlinx.serialization.Serializable

/**
 * Created by sds100 on 07/03/2021.
 */

@Serializable
data class DeviceInfo(val descriptor: String, val name: String)