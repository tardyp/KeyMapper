package io.github.sds100.keymapper.files

import android.content.Context
import android.net.Uri
import io.github.sds100.keymapper.util.result.Error
import io.github.sds100.keymapper.util.result.Result
import io.github.sds100.keymapper.util.result.Success
import java.io.InputStream
import java.io.OutputStream

/**
 * Created by sds100 on 13/04/2021.
 */
class AndroidFileAdapter(context: Context) : FileAdapter {
    private val ctx = context.applicationContext

    override fun openOutputStream(uriString: String): Result<OutputStream> {
        val uri = Uri.parse(uriString)

        return try {
            val outputStream = ctx.contentResolver.openOutputStream(uri)!!

            Success(outputStream)
        } catch (e: Exception) {
            when (e) {
                is SecurityException -> Error.FileAccessDenied
                else -> Error.GenericError(e)
            }
        }
    }

    override fun openInputStream(uriString: String): Result<InputStream> {
        val uri = Uri.parse(uriString)

        return try {
            val inputStream = ctx.contentResolver.openInputStream(uri)!!

            Success(inputStream)
        } catch (e: Exception) {
            when (e) {
                is SecurityException -> Error.FileAccessDenied
                else -> Error.GenericError(e)
            }
        }
    }
}