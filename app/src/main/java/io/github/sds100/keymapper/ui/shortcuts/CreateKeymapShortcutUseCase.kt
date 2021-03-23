package io.github.sds100.keymapper.ui.shortcuts

import androidx.core.os.bundleOf
import io.github.sds100.keymapper.R
import io.github.sds100.keymapper.domain.mappings.keymap.KeymapAction
import io.github.sds100.keymapper.framework.adapters.LauncherShortcutAdapter
import io.github.sds100.keymapper.framework.adapters.ResourceProvider
import io.github.sds100.keymapper.service.MyAccessibilityService
import io.github.sds100.keymapper.ui.actions.ActionUiHelper
import io.github.sds100.keymapper.util.result.onSuccess
import io.github.sds100.keymapper.util.result.valueOrNull

/**
 * Created by sds100 on 23/03/2021.
 */

class CreateKeymapShortcutUseCaseImpl(
    private val adapter: LauncherShortcutAdapter,
    private val resourceProvider: ResourceProvider,
    private val actionUiHelper: ActionUiHelper<KeymapAction>
) : CreateKeymapShortcutUseCase {
    override fun createForSingleAction(keymapUid: String, action: KeymapAction) {
        if (!adapter.isSupported) return

        val icon = actionUiHelper.getIcon(action.data).valueOrNull()?.drawable
            ?: resourceProvider.getDrawable(R.mipmap.ic_launcher_round)

        actionUiHelper.getTitle(action.data).onSuccess { shortcutLabel ->
            adapter.create(
                icon = icon,
                label = shortcutLabel,
                intentAction = MyAccessibilityService.ACTION_TRIGGER_KEYMAP_BY_UID,
                bundleOf(MyAccessibilityService.EXTRA_KEYMAP_UID to keymapUid)
            )
        }
    }

    override fun createForMultipleActions(keymapUid: String, shortcutLabel: String) {
        if (!adapter.isSupported) return

        val icon = resourceProvider.getDrawable(R.mipmap.ic_launcher_round)

        adapter.create(
            icon = icon,
            label = shortcutLabel,
            intentAction = MyAccessibilityService.ACTION_TRIGGER_KEYMAP_BY_UID,
            bundleOf(MyAccessibilityService.EXTRA_KEYMAP_UID to keymapUid)
        )
    }
}

interface CreateKeymapShortcutUseCase {
    fun createForSingleAction(
        keymapUid: String,
        action: KeymapAction
    )

    fun createForMultipleActions(
        keymapUid: String,
        shortcutLabel: String
    )
}