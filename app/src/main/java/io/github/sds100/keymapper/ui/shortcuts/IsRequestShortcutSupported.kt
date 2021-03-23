package io.github.sds100.keymapper.ui.shortcuts

import io.github.sds100.keymapper.framework.adapters.LauncherShortcutAdapter

/**
 * Created by sds100 on 23/03/2021.
 */

class IsRequestShortcutSupportedImpl(private val adapter: LauncherShortcutAdapter) :
    IsRequestShortcutSupported {
    override fun invoke(): Boolean {
        return adapter.isSupported
    }

}

interface IsRequestShortcutSupported {
    operator fun invoke(): Boolean
}