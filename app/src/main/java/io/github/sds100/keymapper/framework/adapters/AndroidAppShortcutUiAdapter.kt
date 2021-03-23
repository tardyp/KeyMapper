package io.github.sds100.keymapper.framework.adapters

import android.content.ComponentName
import android.content.Context
import android.graphics.drawable.Drawable
import io.github.sds100.keymapper.domain.shortcuts.AppShortcutInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Created by sds100 on 23/03/2021.
 */
class AndroidAppShortcutUiAdapter(
    context: Context
) : AppShortcutUiAdapter {
    private val ctx = context.applicationContext

    override fun getName(info: AppShortcutInfo): Flow<String> = flow {
        ctx.packageManager
            .getActivityInfo(ComponentName(info.packageName, info.activityName), 0)
            .loadLabel(ctx.packageManager)
            .let { emit(it.toString()) }
    }

    override fun getIcon(info: AppShortcutInfo): Flow<Drawable> = flow {
        ctx.packageManager
            .getActivityInfo(ComponentName(info.packageName, info.activityName), 0)
            .loadIcon(ctx.packageManager)
            .let { emit(it) }
    }
}