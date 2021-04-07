package io.github.sds100.keymapper.framework.adapters

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.core.graphics.drawable.toBitmap
import io.github.sds100.keymapper.domain.shortcuts.AppShortcutInfo
import io.github.sds100.keymapper.domain.utils.State
import io.github.sds100.keymapper.ui.activity.LaunchKeymapShortcutActivity
import io.github.sds100.keymapper.util.result.FixableError
import io.github.sds100.keymapper.util.result.Result
import io.github.sds100.keymapper.util.result.success
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import java.util.*

/**
 * Created by sds100 on 20/03/2021.
 */
class AndroidAppShortcutAdapter(context: Context) : AppShortcutAdapter {
    private val ctx = context.applicationContext

    override val installedAppShortcuts: Flow<State<List<AppShortcutInfo>>> = flow {
        emit(State.Loading)

        val shortcutIntent = Intent(Intent.ACTION_CREATE_SHORTCUT)

        val shortcuts = ctx.packageManager.queryIntentActivities(shortcutIntent, 0)
            .map {
                val activityInfo = it.activityInfo
                AppShortcutInfo(
                    packageName = activityInfo.packageName,
                    activityName = activityInfo.name
                )
            }

        emit(State.Data(shortcuts))
    }

    override val areLauncherShortcutsSupported: Boolean
        get() = ShortcutManagerCompat.isRequestPinShortcutSupported(ctx)

    override fun createLauncherShortcut(icon: Drawable, label: String, intentAction: String, intentExtras: Bundle) {
        ShortcutInfoCompat.Builder(ctx, UUID.randomUUID().toString()).apply {
            setIcon(IconCompat.createWithBitmap(icon.toBitmap()))
            setShortLabel(label)

            Intent(ctx, LaunchKeymapShortcutActivity::class.java).apply {
                action = intentAction

                putExtras(intentExtras)

                setIntent(this)
            }

            ShortcutManagerCompat.requestPinShortcut(ctx, this.build(), null)
        }
    }

    override fun getShortcutName(info: AppShortcutInfo): Result<String> {
        try {
            return ctx.packageManager
                .getActivityInfo(ComponentName(info.packageName, info.activityName), 0)
                .loadLabel(ctx.packageManager)
                .toString()
                .success()
        } catch (e: PackageManager.NameNotFoundException) {
            return FixableError.AppNotFound(info.packageName)
        }
    }

    override fun getShortcutIcon(info: AppShortcutInfo): Result<Drawable> {
        try {
            return ctx.packageManager
                .getActivityInfo(ComponentName(info.packageName, info.activityName), 0)
                .loadIcon(ctx.packageManager)
                .success()
        } catch (e: PackageManager.NameNotFoundException) {
            return FixableError.AppNotFound(info.packageName)
        }
    }
}