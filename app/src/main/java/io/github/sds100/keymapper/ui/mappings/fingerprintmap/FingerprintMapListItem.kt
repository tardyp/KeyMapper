package io.github.sds100.keymapper.ui.mappings.fingerprintmap

import io.github.sds100.keymapper.domain.mappings.fingerprintmap.FingerprintMapId
import io.github.sds100.keymapper.ui.ChipUi

/**
 * Created by sds100 on 08/11/20.
 */

data class FingerprintMapListItem(
    val id: FingerprintMapId,
    val header: String,
    val chipList: List<ChipUi>,
    val optionsDescription: String,
    val isEnabled: Boolean,
    val extraInfo: String
){
    val hasOptions: Boolean
        get() = optionsDescription.isNotBlank()
}