package io.github.sds100.keymapper.ui.fragment.keymap

import io.github.sds100.keymapper.ui.mappings.keymap.TriggerKeyLinkType

/**
 * Created by sds100 on 27/03/2020.
 */
data class TriggerKeyListItemModel(
    val id: String,
    val keyCode: Int,
    val name: String,

    /**
     * null if should be hidden
     */
    val clickTypeString: String? = null,

    val extraInfo: String?,

    val linkType: TriggerKeyLinkType,

    val isDragDropEnabled: Boolean
)