package io.github.sds100.keymapper.system.root

import io.github.sds100.keymapper.data.Keys
import io.github.sds100.keymapper.data.repositories.PreferenceRepository
import io.github.sds100.keymapper.system.Shell
import io.github.sds100.keymapper.util.Error
import io.github.sds100.keymapper.util.Result
import io.github.sds100.keymapper.util.Success
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Created by sds100 on 21/04/2021.
 */

class SuAdapterImpl(preferenceRepository: PreferenceRepository) : SuAdapter {
    private var process: Process? = null

    override val isGranted: Flow<Boolean> = preferenceRepository.get(Keys.hasRootPermission).map {
        it ?: false
    }

    override fun execute(command: String, block: Boolean): Result<*> {
        try {
            if (block) {
                //Don't use the long running su process because that will block the thread indefinitely
                Shell.run("su", "-c", command, waitFor = true)

            } else {
                if (process == null) {
                    process = RootUtils.getSuProcess()
                }

                with(process!!.outputStream.bufferedWriter()) {
                    write("$command\n")
                    flush()
                }
            }

            return Success(Unit)
        } catch (e: Exception) {
            return Error.Exception(e)
        }
    }
}

interface SuAdapter {
    val isGranted: Flow<Boolean>
    fun execute(command: String, block: Boolean = false): Result<*>
}