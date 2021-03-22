package io.github.sds100.keymapper.framework.adapters

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.core.graphics.drawable.toBitmap
import java.util.*

/**
 * Created by sds100 on 20/03/2021.
 */
class LauncherShortcutAdapterImpl(context: Context) : LauncherShortcutAdapter {
    private val ctx = context.applicationContext

    override val isSupported: Boolean
        get() = ShortcutManagerCompat.isRequestPinShortcutSupported(ctx)

    override fun create(icon: Drawable, label: String, intent: Intent) {
        ShortcutInfoCompat.Builder(ctx, UUID.randomUUID().toString()).apply {
            setIcon(IconCompat.createWithBitmap(icon.toBitmap()))
            setShortLabel(label)
            setIntent(intent)

            build()
        }
    }
}