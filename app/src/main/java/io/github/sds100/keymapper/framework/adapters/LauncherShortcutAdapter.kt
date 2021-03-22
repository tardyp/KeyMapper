package io.github.sds100.keymapper.framework.adapters

import android.content.Intent
import android.graphics.drawable.Drawable

/**
 * Created by sds100 on 20/03/2021.
 */
interface LauncherShortcutAdapter {
    val isSupported: Boolean

    fun create(
        icon: Drawable,
        label: String,
        intent: Intent
    )
}