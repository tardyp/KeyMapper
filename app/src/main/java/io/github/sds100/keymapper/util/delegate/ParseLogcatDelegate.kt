package io.github.sds100.keymapper.util.delegate

import io.github.sds100.keymapper.R
import io.github.sds100.keymapper.util.Shell
import kotlinx.coroutines.*
import splitties.toast.toast
import timber.log.Timber
import java.io.IOException

/**
 * Created by sds100 on 21/06/2020.
 */

class ParseLogcatDelegate(val onMessage: suspend () -> Unit) {

    private var mJob: Job? = null

    /**
     * @return whether it successfully started listening.
     */
    fun startListening(scope: CoroutineScope): Boolean {
        try {
            mJob = scope.launch(Dispatchers.IO) {

                try {
                    Shell.run("/system/bin/logcat", "-c")

//TODO generalise this
                    val inputStream = Shell.getShellCommandStdOut(
                        "/system/bin/logcat", "-s", "Elmyra/ElmyraService", "-v", "raw")
                    var line: String?

                    while (inputStream.bufferedReader().readLine().also { line = it } != null && isActive) {
                        line ?: continue

                        if (line == "Triggering LaunchOpa [mIsGestureEnabled -> true; mIsOpaEnabled -> true]") {
                            onMessage.invoke()
                        }
                    }

                    inputStream.close()

                } catch (e: IOException) {
                    withContext(Dispatchers.Main) {
                        toast(R.string.toast_io_exception_shrug)
                    }
                }
            }

        } catch (e: Exception) {
            mJob?.cancel()
            return false
        }

        return true
    }

    fun stopListening() {
        mJob?.cancel()
    }
}