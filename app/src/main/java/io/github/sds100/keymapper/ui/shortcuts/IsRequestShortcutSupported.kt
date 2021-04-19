package io.github.sds100.keymapper.ui.shortcuts

import io.github.sds100.keymapper.domain.adapter.AppShortcutAdapter

/**
 * Created by sds100 on 23/03/2021.
 */

class IsRequestShortcutSupportedImpl(private val adapter: AppShortcutAdapter) :
    IsRequestShortcutSupported {
    override fun invoke(): Boolean {
        return adapter.areLauncherShortcutsSupported
    }

}

interface IsRequestShortcutSupported {
    operator fun invoke(): Boolean
}