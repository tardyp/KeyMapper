package io.github.sds100.keymapper.domain.adapter

import io.github.sds100.keymapper.domain.ime.ImeInfo
import io.github.sds100.keymapper.util.result.Result
import kotlinx.coroutines.flow.Flow

/**
 * Created by sds100 on 14/02/2021.
 */
interface InputMethodAdapter {
    fun showImePicker(fromForeground: Boolean)

    fun isImeEnabledById(imeId: String): Boolean
    fun isImeEnabledByPackageName(packageName: String): Boolean
    fun enableImeById(imeId: String)
    fun enableImeByPackageName(packageName: String)

    fun isImeChosenById(imeId: String): Boolean
    fun isImeChosenByPackageName(packageName: String): Boolean
    fun chooseImeById(imeId: String)
    fun chooseImeByPackageName(packageName: String)

    fun getLabel(imeId: String): Result<String>

    fun getImeHistory(): List<String>

    val enabledInputMethods: Flow<List<ImeInfo>>
    val chosenIme: Flow<ImeInfo>
}