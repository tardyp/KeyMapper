package io.github.sds100.keymapper.framework.adapters

import android.graphics.drawable.Drawable
import io.github.sds100.keymapper.domain.shortcuts.AppShortcutInfo
import kotlinx.coroutines.flow.Flow

/**
 * Created by sds100 on 23/03/2021.
 */
interface AppShortcutUiAdapter {
    fun getName(info: AppShortcutInfo): Flow<String>
    fun getIcon(info: AppShortcutInfo): Flow<Drawable>
}