package io.github.sds100.keymapper.framework.adapters

import android.graphics.drawable.Drawable
import android.os.Bundle
import io.github.sds100.keymapper.domain.shortcuts.AppShortcutInfo
import io.github.sds100.keymapper.domain.utils.State
import io.github.sds100.keymapper.util.result.Result
import kotlinx.coroutines.flow.StateFlow

/**
 * Created by sds100 on 20/03/2021.
 */

interface AppShortcutAdapter {
    val installedAppShortcuts: StateFlow<State<List<AppShortcutInfo>>>
    val areLauncherShortcutsSupported: Boolean

    fun createLauncherShortcut(
        icon: Drawable,
        label: String,
        intentAction: String,
        intentExtras: Bundle
    )

    fun getShortcutName(info: AppShortcutInfo): Result<String>
    fun getShortcutIcon(info: AppShortcutInfo): Result<Drawable>
}