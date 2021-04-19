package io.github.sds100.keymapper.ui.shortcuts

import androidx.core.os.bundleOf
import io.github.sds100.keymapper.R
import io.github.sds100.keymapper.domain.mappings.keymap.KeyMapAction
import io.github.sds100.keymapper.domain.adapter.AppShortcutAdapter
import io.github.sds100.keymapper.framework.adapters.ResourceProvider
import io.github.sds100.keymapper.mappings.keymaps.DisplayKeyMapUseCase
import io.github.sds100.keymapper.service.MyAccessibilityService
import io.github.sds100.keymapper.ui.mappings.keymap.KeyMapActionUiHelper

/**
 * Created by sds100 on 23/03/2021.
 */

class CreateKeyMapShortcutUseCaseImpl(
    private val adapter: AppShortcutAdapter,
    displayKeyMap: DisplayKeyMapUseCase,
    resourceProvider: ResourceProvider
) : CreateKeyMapShortcutUseCase, ResourceProvider by resourceProvider {
    private val actionUiHelper by lazy { KeyMapActionUiHelper(displayKeyMap, resourceProvider) }

    override val isSupported: Boolean
        get() = adapter.areLauncherShortcutsSupported

    override fun createForSingleAction(keyMapUid: String, action: KeyMapAction) {
        if (!adapter.areLauncherShortcutsSupported) return

        val icon = actionUiHelper.getIcon(action.data)?.drawable
            ?: getDrawable(R.mipmap.ic_launcher_round)

        adapter.createLauncherShortcut(
            icon = icon,
            label = actionUiHelper.getTitle(action.data),
            intentAction = MyAccessibilityService.ACTION_TRIGGER_KEYMAP_BY_UID,
            bundleOf(MyAccessibilityService.EXTRA_KEYMAP_UID to keyMapUid)
        )
    }

    override fun createForMultipleActions(keyMapUid: String, shortcutLabel: String) {
        if (!adapter.areLauncherShortcutsSupported) return

        adapter.createLauncherShortcut(
            icon = getDrawable(R.mipmap.ic_launcher_round),
            label = shortcutLabel,
            intentAction = MyAccessibilityService.ACTION_TRIGGER_KEYMAP_BY_UID,
            bundleOf(MyAccessibilityService.EXTRA_KEYMAP_UID to keyMapUid)
        )
    }
}

interface CreateKeyMapShortcutUseCase {
    val isSupported: Boolean

    fun createForSingleAction(
        keyMapUid: String,
        action: KeyMapAction
    )

    fun createForMultipleActions(
        keyMapUid: String,
        shortcutLabel: String
    )
}