package io.github.sds100.keymapper.ui

data class TriggerFromOtherAppsListItem(
    override val id: String,
    val isEnabled: Boolean,
    val keyMapUid: String,
    val label: String,
    val showCreateLauncherShortcutButton: Boolean
) : ListItem