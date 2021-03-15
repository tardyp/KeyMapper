package io.github.sds100.keymapper.ui.actions

import android.view.KeyEvent
import io.github.sds100.keymapper.R
import io.github.sds100.keymapper.domain.actions.*
import io.github.sds100.keymapper.framework.adapters.AppInfoAdapter
import io.github.sds100.keymapper.framework.adapters.ResourceProvider
import io.github.sds100.keymapper.util.KeyEventUtils
import io.github.sds100.keymapper.util.SystemActionUtils
import io.github.sds100.keymapper.util.result.*
import splitties.bitflags.hasFlag

/**
 * Created by sds100 on 22/02/2021.
 */

abstract class BaseActionListItemMapper<A : Action>(
    private val getActionError: GetActionErrorUseCase,
    private val appInfoAdapter: AppInfoAdapter,
    resourceProvider: ResourceProvider
) : ResourceProvider by resourceProvider, ActionListItemMapper<A> {

    override fun map(action: A, error: Error?): ActionListItemModel {
        TODO("Not yet implemented")
    }

    private fun getTitle(action: ActionData): Result<String> = when (action) {
        is OpenAppAction ->
            appInfoAdapter.getAppName(action.packageName) then { appName ->
                Success(getString(R.string.description_open_app, appName))
            }

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

        is SimpleSystemAction -> {
            val resId = SystemActionUtils.TITLE_MAP[action.systemActionId]
                ?: error("Unable to find system action id title res for ${action.systemActionId}")

            Success(getString(resId))
        }

        is VolumeSystemAction -> {
            val resId = SystemActionUtils.TITLE_MAP[action.systemActionId]
                ?: error("Unable to find system action id title res for ${action.systemActionId}")

            if (action.showVolumeUi) {

                val midDot = getString(R.string.middot)
                "${getString(resId)} $midDot ${getString(R.string.flag_show_volume_dialog)}".success()
            } else {
                getString(resId).success()
            }
        }

        is CorruptAction -> CorruptActionError
        is SystemAction -> TODO()
    }

    abstract fun getOptionStrings(action: A): List<String>
}

interface ActionListItemMapper<A : Action> {
    fun map(action: A, error: Error?): ActionListItemModel
}