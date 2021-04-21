package io.github.sds100.keymapper.system.inputmethod

import io.github.sds100.keymapper.util.Result
import kotlinx.coroutines.flow.Flow

/**
 * Created by sds100 on 14/02/2021.
 */
interface InputMethodAdapter {
    val isUserInputRequiredToChangeIme: Flow<Boolean>

    fun showImePicker(fromForeground: Boolean): Result<Unit>
    fun enableIme(imeId: String): Result<Unit>
    suspend fun chooseIme(imeId: String, fromForeground: Boolean): Result<ImeInfo>

    fun getInfoById(imeId: String): Result<ImeInfo>
    fun getInfoByPackageName(packageName: String): Result<ImeInfo>

    /**
     * The last used input method is first.
     */
    val inputMethodHistory: Flow<List<ImeInfo>>
    val inputMethods: Flow<List<ImeInfo>>
    val chosenIme: Flow<ImeInfo>
}