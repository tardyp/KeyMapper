package io.github.sds100.keymapper.packages

import android.graphics.drawable.Drawable
import io.github.sds100.keymapper.domain.shortcuts.AppShortcutInfo
import io.github.sds100.keymapper.domain.utils.State
import io.github.sds100.keymapper.framework.adapters.AppShortcutAdapter
import io.github.sds100.keymapper.util.result.Result
import kotlinx.coroutines.flow.StateFlow

/**
 * Created by sds100 on 04/04/2021.
 */

class DisplayAppShortcutsUseCaseImpl(
    private val appShortcutAdapter: AppShortcutAdapter,
) : DisplayAppShortcutsUseCase {
    override val shortcuts: StateFlow<State<List<AppShortcutInfo>>> = appShortcutAdapter.installedAppShortcuts

    override fun getShortcutName(appShortcutInfo: AppShortcutInfo): Result<String> {
        return appShortcutAdapter.getShortcutName(appShortcutInfo)
    }

    override fun getShortcutIcon(appShortcutInfo: AppShortcutInfo): Result<Drawable> {
        return appShortcutAdapter.getShortcutIcon(appShortcutInfo)
    }
}

interface DisplayAppShortcutsUseCase {
    val shortcuts: StateFlow<State<List<AppShortcutInfo>>>

    fun getShortcutName(appShortcutInfo: AppShortcutInfo): Result<String>
    fun getShortcutIcon(appShortcutInfo: AppShortcutInfo): Result<Drawable>
}