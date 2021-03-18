package io.github.sds100.keymapper.domain.mappings.keymap.trigger

import io.github.sds100.keymapper.data.model.TriggerEntity
import io.github.sds100.keymapper.domain.adapter.ExternalDeviceAdapter
import io.github.sds100.keymapper.domain.utils.ClickType
import kotlinx.serialization.Serializable
import splitties.bitflags.withFlag
import java.util.*

/**
 * Created by sds100 on 21/02/2021.
 */
@Serializable
data class TriggerKey(
    val uid: String = UUID.randomUUID().toString(),
    val keyCode: Int,
    val device: TriggerKeyDevice,
    val clickType: ClickType,

    val consumeKeyEvent: Boolean = true,
)

object KeymapTriggerKeyEntityMapper {
    fun fromEntity(
        entity: TriggerEntity.KeyEntity,
        deviceAdapter: ExternalDeviceAdapter
    ): TriggerKey {
        return TriggerKey(
            uid = entity.uid,
            keyCode = entity.keyCode,
            device = when (entity.deviceId) {
                TriggerEntity.KeyEntity.DEVICE_ID_THIS_DEVICE -> TriggerKeyDevice.Internal
                TriggerEntity.KeyEntity.DEVICE_ID_ANY_DEVICE -> TriggerKeyDevice.Any
                else -> TriggerKeyDevice.External(entity.deviceId, "TODO")//TODO
            },
            clickType = when (entity.clickType) {
                TriggerEntity.SHORT_PRESS -> ClickType.SHORT_PRESS
                TriggerEntity.LONG_PRESS -> ClickType.LONG_PRESS
                TriggerEntity.DOUBLE_PRESS -> ClickType.DOUBLE_PRESS
                else -> ClickType.SHORT_PRESS
            }
        )
    }

    fun toEntity(key: TriggerKey): TriggerEntity.KeyEntity {
        val deviceId = when (key.device) {
            TriggerKeyDevice.Any -> TriggerEntity.KeyEntity.DEVICE_ID_ANY_DEVICE
            is TriggerKeyDevice.External -> key.device.descriptor
            TriggerKeyDevice.Internal -> TriggerEntity.KeyEntity.DEVICE_ID_THIS_DEVICE
        }

        val clickType = when (key.clickType) {
            ClickType.SHORT_PRESS -> TriggerEntity.SHORT_PRESS
            ClickType.LONG_PRESS -> TriggerEntity.LONG_PRESS
            ClickType.DOUBLE_PRESS -> TriggerEntity.DOUBLE_PRESS
        }

        var flags = 0

        if (!key.consumeKeyEvent) {
            flags = flags.withFlag(TriggerEntity.KeyEntity.FLAG_DO_NOT_CONSUME_KEY_EVENT)
        }

        return TriggerEntity.KeyEntity(
            keyCode = key.keyCode,
            deviceId = deviceId,
            clickType = clickType,
            flags = flags,
            uid = key.uid
        )
    }
}