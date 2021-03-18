package io.github.sds100.keymapper.ui.mappings.keymap

import io.github.sds100.keymapper.R
import io.github.sds100.keymapper.domain.devices.ShowDeviceInfoUseCase
import io.github.sds100.keymapper.domain.mappings.keymap.trigger.TriggerKey
import io.github.sds100.keymapper.domain.mappings.keymap.trigger.TriggerKeyDevice
import io.github.sds100.keymapper.domain.mappings.keymap.trigger.TriggerMode
import io.github.sds100.keymapper.domain.utils.ClickType
import io.github.sds100.keymapper.framework.adapters.ResourceProvider
import io.github.sds100.keymapper.ui.fragment.keymap.TriggerKeyListItemModel
import io.github.sds100.keymapper.util.KeyEventUtils
import io.github.sds100.keymapper.util.result.getFullMessage
import io.github.sds100.keymapper.util.result.handle
import kotlinx.coroutines.runBlocking

/**
 * Created by sds100 on 26/02/2021.
 */

class TriggerKeyListItemMapperImpl(
    private val resourceProvider: ResourceProvider,
    private val showDeviceInfoUseCase: ShowDeviceInfoUseCase
) : TriggerKeyListItemMapper, ResourceProvider by resourceProvider {

    override fun map(keys: List<TriggerKey>, mode: TriggerMode): List<TriggerKeyListItemModel> =
        keys.mapIndexed { index, key ->
            val extraInfo = buildString {
                append(getDeviceName(key.device))

                if (!key.consumeKeyEvent) {
                    val midDot = getString(R.string.middot)
                    append(" $midDot ${getString(R.string.flag_dont_override_default_action)}")
                }
            }

            val clickTypeString = when (key.clickType) {
                ClickType.SHORT_PRESS -> null
                ClickType.LONG_PRESS -> getString(R.string.clicktype_long_press)
                ClickType.DOUBLE_PRESS -> getString(R.string.clicktype_double_press)
            }

            val linkDrawable = when {
                mode is TriggerMode.Parallel && index < keys.lastIndex -> TriggerKeyLinkType.PLUS
                mode is TriggerMode.Sequence && index < keys.lastIndex -> TriggerKeyLinkType.ARROW
                else -> TriggerKeyLinkType.HIDDEN
            }

            TriggerKeyListItemModel(
                id = key.uid,
                keyCode = key.keyCode,
                name = KeyEventUtils.keycodeToString(key.keyCode),
                clickTypeString = clickTypeString,
                extraInfo = extraInfo,
                linkType = linkDrawable,
                isDragDropEnabled = keys.size > 1
            )
        }

    private fun getDeviceName(device: TriggerKeyDevice): String =
        when (device) {
            is TriggerKeyDevice.Internal -> getString(R.string.this_device)
            is TriggerKeyDevice.Any -> getString(R.string.any_device)
            is TriggerKeyDevice.External -> runBlocking {
                showDeviceInfoUseCase.getDeviceName(device.descriptor)
                    .handle(
                        onSuccess = { it },
                        onError = { it.getFullMessage(resourceProvider) }
                    )
            }
        }

}

interface TriggerKeyListItemMapper {
    fun map(keys: List<TriggerKey>, mode: TriggerMode): List<TriggerKeyListItemModel>
}