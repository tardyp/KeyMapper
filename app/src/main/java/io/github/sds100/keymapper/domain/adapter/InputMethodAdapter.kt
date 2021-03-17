package io.github.sds100.keymapper.domain.adapter

import io.github.sds100.keymapper.util.result.Result
import kotlinx.coroutines.flow.StateFlow

/**
 * Created by sds100 on 14/02/2021.
 */
interface InputMethodAdapter {
    fun showImePicker(fromForeground: Boolean)

    fun isImeEnabled(imeId: String):Boolean
    fun enableIme(imeId: String)

    fun isImeChosen(imeId: String):Boolean
    fun chooseIme(imeId: String)

    fun getLabel(imeId: String): Result<String>

    fun getImeHistory(): List<String>

    val chosenImePackageName: StateFlow<String?>
}