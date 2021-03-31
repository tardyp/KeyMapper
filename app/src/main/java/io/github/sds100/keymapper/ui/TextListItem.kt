package io.github.sds100.keymapper.ui

/**
 * Created by sds100 on 31/03/2021.
 */

sealed class TextListItem {
    data class Success(val id: String, val text: String) : TextListItem()
    data class Error(val id: String, val text: String) : TextListItem()
}