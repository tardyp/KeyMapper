package io.github.sds100.keymapper.util

import io.github.sds100.keymapper.util.result.Result
import java.io.OutputStream

/**
 * Created by sds100 on 23/01/21.
 */

//TODO rename as FileAdapter
interface IContentResolver {
    fun openOutputStream(uriString: String): Result<OutputStream>
}