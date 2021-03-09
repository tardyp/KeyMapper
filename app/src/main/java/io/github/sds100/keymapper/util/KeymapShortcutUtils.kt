package io.github.sds100.keymapper.util

import android.content.Context
import android.content.Intent
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.LifecycleOwner
import io.github.sds100.keymapper.R
import io.github.sds100.keymapper.data.model.ActionEntity
import io.github.sds100.keymapper.data.model.DeviceInfoEntity
import io.github.sds100.keymapper.service.MyAccessibilityService
import io.github.sds100.keymapper.ui.activity.LaunchKeymapShortcutActivity
import io.github.sds100.keymapper.util.result.valueOrNull
import java.util.*

/**
 * Created by sds100 on 21/01/21.
 */
object KeymapShortcutUtils {
    suspend fun createShortcut(
        ctx: Context,
        lifecycleOwner: LifecycleOwner,
        uuid: String,
        actionList: List<ActionEntity>,
        deviceInfoList: List<DeviceInfoEntity>,
        showDeviceDescriptors: Boolean
    ): ShortcutInfoCompat = ShortcutInfoCompat.Builder(ctx, UUID.randomUUID().toString()).apply {

        val icon = createShortcutIcon(ctx, actionList)
        val shortcutLabel = createShortcutLabel(
            ctx,
            lifecycleOwner,
            actionList,
            deviceInfoList,
            showDeviceDescriptors
        )

        setIcon(icon)
        setShortLabel(shortcutLabel)

        Intent(ctx, LaunchKeymapShortcutActivity::class.java).apply {
            action = MyAccessibilityService.ACTION_TRIGGER_KEYMAP_BY_UID
            putExtra(MyAccessibilityService.EXTRA_KEYMAP_UID, uuid)

            setIntent(this)
        }
    }.build()

    private fun createShortcutIcon(ctx: Context, actionList: List<ActionEntity>): IconCompat {
        if (actionList.size == 1) {
            val action = actionList[0]

            action.getIcon(ctx).valueOrNull()?.let {
                val bitmap = it.toBitmap()

                return IconCompat.createWithBitmap(bitmap)
            }
        }

        return IconCompat.createWithResource(ctx, R.mipmap.ic_launcher_round)
    }

    private suspend fun createShortcutLabel(
        ctx: Context,
        lifecycleOwner: LifecycleOwner,
        actionList: List<ActionEntity>,
        deviceInfoList: List<DeviceInfoEntity>,
        showDeviceDescriptors: Boolean
    ): String {
        if (actionList.size == 1) {
            val action = actionList[0]

            action
                .getTitle(ctx, deviceInfoList, showDeviceDescriptors)
                .valueOrNull()
                ?.let {
                    return it
                }
        }

        return ctx.editTextStringAlertDialog(
            lifecycleOwner,
            ctx.str(R.string.hint_shortcut_name),
            allowEmpty = false
        )
    }
}