package io.github.sds100.keymapper.ui

data class TriggerFromOtherAppsListItem(
    override val id: String,
    val isEnabled: Boolean,
    val keymapUid: String,
    val label: String,
    val areLauncherShortcutsSupported: Boolean
) : ListItem