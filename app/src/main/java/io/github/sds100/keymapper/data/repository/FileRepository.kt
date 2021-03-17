package io.github.sds100.keymapper.data.repository

import android.content.Context
import io.github.sds100.keymapper.util.FileUtils
import io.github.sds100.keymapper.util.NetworkUtils
import io.github.sds100.keymapper.util.result.*
import java.io.File
import java.net.URL

/**
 * Created by sds100 on 04/04/2020.
 */
class FileRepository(private val context: Context) {

    suspend fun getFile(url: String): Result<String> {
        val fileName = extractFileName(url)
        val path = FileUtils.getPathToFileInAppData(context, fileName)

        return NetworkUtils.downloadFile(context, url, path).otherwise {
            if (it is Error.DownloadFailed) {
                val file = File(path)

                if (file.exists() && file.readText().isNotBlank()) {
                    Success(file)
                } else {
                    Error.FileNotCached
                }
            } else {
                it
            }
        }.then { Success(it.readText()) }
    }

    /**
     * Extracts the file name from the url
     */
    private fun extractFileName(fileUrl: String): String {
        return File(URL(fileUrl).path.toString()).name
    }
}