package io.github.sds100.keymapper.util.ui

import io.github.sds100.keymapper.util.result.Error

/**
 * Created by sds100 on 18/03/2021.
 */
sealed class ChipUi {
    abstract val id: String

    data class Normal(
        override val id: String,
        val text: String,
        val icon: IconInfo?
    ) : ChipUi()

    data class Error(
        override val id: String,
        val text: String,
        val error: io.github.sds100.keymapper.util.result.Error
    ) : ChipUi()

    data class Transparent(override val id: String, val text: String) : ChipUi()
}