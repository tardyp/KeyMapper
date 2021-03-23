package io.github.sds100.keymapper.framework.adapters

import android.graphics.drawable.Drawable
import android.os.Bundle

/**
 * Created by sds100 on 20/03/2021.
 */
interface LauncherShortcutAdapter {
    val isSupported: Boolean

    fun create(
        icon: Drawable,
        label: String,
        intentAction: String,
        intentExtras: Bundle
    )
}