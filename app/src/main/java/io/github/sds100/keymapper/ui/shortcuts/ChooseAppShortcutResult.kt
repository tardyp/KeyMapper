package io.github.sds100.keymapper.ui.shortcuts

import kotlinx.serialization.Serializable

/**
 * Created by sds100 on 23/03/2021.
 */
@Serializable
data class ChooseAppShortcutResult(
    val packageName: String?,
    val shortcutName: String,
    val uri: String
)
